package org.dynamisengine.scripting.percept;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.scripting.api.CanonLog;
import org.dynamisengine.scripting.api.value.CanonEvent;
import org.dynamisengine.scripting.api.value.CanonTime;
import org.dynamisengine.scripting.api.value.Percept;
import org.dynamisengine.scripting.canon.DefaultCanonLog;
import org.dynamisengine.scripting.spi.PerceptFilter;
import org.junit.jupiter.api.Test;

class DefaultPerceptBusTest {
    static Percept testPercept(String type, double fidelity, long commitId) {
        return Percept.of(EntityId.of(1L), type, "payload", fidelity, commitId);
    }

    @Test
    void subscribeAndDeliverReceivesPercept() {
        DefaultPerceptBus bus = busWithLog(new DefaultCanonLog());
        List<Percept> received = new ArrayList<>();

        bus.subscribe(EntityId.of(1L), received::add);
        bus.deliver(testPercept("collision", 1.0D, 1L));

        assertEquals(1, received.size());
    }

    @Test
    void unsubscribeStopsDelivery() {
        DefaultPerceptBus bus = busWithLog(new DefaultCanonLog());
        List<Percept> received = new ArrayList<>();
        EntityId agentId = EntityId.of(1L);

        bus.subscribe(agentId, received::add);
        bus.unsubscribe(agentId);
        bus.deliver(testPercept("collision", 1.0D, 1L));

        assertTrue(received.isEmpty());
    }

    @Test
    void typeFilterBlocksUnsubscribedType() {
        DefaultPerceptBus bus = busWithLog(new DefaultCanonLog());
        List<Percept> received = new ArrayList<>();
        EntityId agentId = EntityId.of(1L);
        bus.subscribeWithPolicy(
                agentId,
                received::add,
                PerceptDeliveryPolicy.of(agentId, Set.of("collision"), 0.0D, true));

        bus.deliver(testPercept("rumour", 1.0D, 1L));

        assertTrue(received.isEmpty());
    }

    @Test
    void belowMinimumFidelityWithheld() {
        DefaultPerceptBus bus = busWithLog(new DefaultCanonLog());
        List<Percept> received = new ArrayList<>();
        EntityId agentId = EntityId.of(1L);
        bus.subscribeWithPolicy(
                agentId,
                received::add,
                PerceptDeliveryPolicy.of(agentId, Set.of(), 0.8D, true));

        bus.deliver(testPercept("collision", 0.5D, 1L));

        assertTrue(received.isEmpty());
    }

    @Test
    void shouldDeliverFalseFilterWithholdsPercept() {
        DefaultPerceptBus bus = busWithLog(new DefaultCanonLog());
        List<Percept> received = new ArrayList<>();
        bus.subscribe(EntityId.of(1L), received::add);
        bus.registerFilter(new PerceptFilter() {
            @Override
            public String filterId() {
                return "blocker";
            }

            @Override
            public boolean shouldDeliver(Percept percept, EntityId agentId, CanonLog canonLog) {
                return false;
            }

            @Override
            public Percept degrade(Percept percept, EntityId agentId, CanonLog canonLog) {
                return percept;
            }
        });

        bus.deliver(testPercept("collision", 1.0D, 1L));

        assertTrue(received.isEmpty());
    }

    @Test
    void degradeFilterLowersDeliveredFidelity() {
        DefaultPerceptBus bus = busWithLog(new DefaultCanonLog());
        List<Percept> received = new ArrayList<>();
        bus.subscribe(EntityId.of(1L), received::add);
        bus.registerFilter(new PerceptFilter() {
            @Override
            public String filterId() {
                return "degrade";
            }

            @Override
            public boolean shouldDeliver(Percept percept, EntityId agentId, CanonLog canonLog) {
                return true;
            }

            @Override
            public Percept degrade(Percept percept, EntityId agentId, CanonLog canonLog) {
                return Percept.of(percept.agentId(), percept.perceptType(), percept.payload(), 0.5D, percept.sourceCommitId());
            }
        });

        bus.deliver(testPercept("collision", 1.0D, 1L));

        assertEquals(0.5D, received.get(0).fidelity());
    }

    @Test
    void multipleFiltersAppliedInPriorityOrder() {
        DefaultPerceptBus bus = busWithLog(new DefaultCanonLog());
        List<String> order = new ArrayList<>();
        bus.subscribe(EntityId.of(1L), percept -> { });

        bus.registerFilter(new OrderedFilter("second", 2, order));
        bus.registerFilter(new OrderedFilter("first", 1, order));

        bus.deliver(testPercept("collision", 1.0D, 1L));

        assertEquals(List.of("first", "second"), order);
    }

    @Test
    void subscriberCountReflectsSubscribeAndUnsubscribe() {
        DefaultPerceptBus bus = busWithLog(new DefaultCanonLog());
        EntityId agent = EntityId.of(1L);
        bus.subscribe(agent, percept -> { });
        assertEquals(1, bus.subscriberCount());

        bus.unsubscribe(agent);
        assertEquals(0, bus.subscriberCount());
    }

    @Test
    void traceProvenanceReturnsChainWithSourceCommitId() {
        DefaultPerceptBus bus = busWithLog(new DefaultCanonLog());
        bus.registerFilter(new PerceptFilter() {
            @Override
            public String filterId() {
                return "degrade";
            }

            @Override
            public boolean shouldDeliver(Percept percept, EntityId agentId, CanonLog canonLog) {
                return true;
            }

            @Override
            public Percept degrade(Percept percept, EntityId agentId, CanonLog canonLog) {
                return Percept.of(percept.agentId(), percept.perceptType(), percept.payload(), 0.9D, percept.sourceCommitId());
            }
        });

        PerceptProvenanceChain chain = bus.traceProvenance(testPercept("collision", 1.0D, 42L), EntityId.of(1L));

        assertNotNull(chain);
        assertEquals(42L, chain.sourceCommitId());
    }

    @Test
    void deliverNeverAppendsToCanonLog() {
        CanonLog spyLog = new CanonLog() {
            private final DefaultCanonLog delegate = new DefaultCanonLog();

            @Override
            public void append(CanonEvent event) {
                throw new RuntimeException("append should not be called");
            }

            @Override
            public List<CanonEvent> query(CanonTime from, CanonTime to) {
                return delegate.query(from, to);
            }

            @Override
            public List<CanonEvent> queryByCausalLink(String causalLink) {
                return delegate.queryByCausalLink(causalLink);
            }

            @Override
            public Optional<CanonEvent> findByCommitId(long commitId) {
                return delegate.findByCommitId(commitId);
            }

            @Override
            public CanonLog fork(long atCommitId) {
                return delegate.fork(atCommitId);
            }

            @Override
            public void replay(long fromCommitId, Consumer<CanonEvent> handler) {
                delegate.replay(fromCommitId, handler);
            }

            @Override
            public long latestCommitId() {
                return delegate.latestCommitId();
            }

            @Override
            public CanonTime latestCanonTime() {
                return delegate.latestCanonTime();
            }
        };

        DefaultPerceptBus bus = busWithLog(spyLog);
        AtomicBoolean called = new AtomicBoolean(false);
        bus.subscribe(EntityId.of(1L), percept -> called.set(true));

        bus.deliver(testPercept("collision", 1.0D, 1L));

        assertTrue(called.get());
    }

    private static DefaultPerceptBus busWithLog(CanonLog log) {
        return new DefaultPerceptBus(new FidelityModel(), new PerceptDownsampler(), log, 100);
    }

    private static final class OrderedFilter implements PerceptFilter {
        private final String id;
        private final int priority;
        private final List<String> order;

        private OrderedFilter(String id, int priority, List<String> order) {
            this.id = id;
            this.priority = priority;
            this.order = order;
        }

        @Override
        public String filterId() {
            return id;
        }

        @Override
        public boolean shouldDeliver(Percept percept, EntityId agentId, CanonLog canonLog) {
            return true;
        }

        @Override
        public Percept degrade(Percept percept, EntityId agentId, CanonLog canonLog) {
            order.add(id);
            return percept;
        }

        @Override
        public int priority() {
            return priority;
        }
    }
}
