package org.dynamisscripting.chronicler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.dynamisscripting.api.CanonLog;
import org.dynamisscripting.api.value.CanonEvent;
import org.dynamisscripting.api.value.CanonTime;
import org.dynamisscripting.api.value.WorldEvent;
import org.dynamisscripting.canon.DefaultCanonLog;
import org.dynamisscripting.dsl.DslCompiler;
import org.dynamisscripting.dsl.PredicateDsl;
import org.dynamisscripting.spi.ChroniclerNodeArchetype;
import org.junit.jupiter.api.Test;

class DefaultChroniclerTest {
    @Test
    void tickWithTriggeredNodeEmitsWorldEvent() {
        QuestGraph graph = GraphLoader.buildManually(List.of(StoryNode.of("n1", "authored", "canonTime > 0", List.of(), 10, false, 0)));
        TriggerEvaluator evaluator = new TriggerEvaluator(new PredicateDsl(new DslCompiler()), new DefaultCanonLog());
        ChroniclerScheduler scheduler = new ChroniclerScheduler(graph, evaluator, 10);
        ArchetypeInstantiator instantiator = new ArchetypeInstantiator(List.of());
        WorldEventEmitter emitter = new WorldEventEmitter();
        DefaultChronicler chronicler = new DefaultChronicler(graph, evaluator, scheduler, instantiator, emitter);

        List<WorldEvent> emitted = new ArrayList<>();
        chronicler.registerWorldEventListener(emitted::add);

        chronicler.tick(CanonTime.of(5, 0));

        assertEquals(1, emitted.size());
        assertEquals("n1", emitted.get(0).nodeId());
    }

    @Test
    void tickWithNoTriggeredNodesEmitsNothing() {
        QuestGraph graph = GraphLoader.buildManually(List.of(StoryNode.of("n1", "authored", "canonTime > 100", List.of(), 10, false, 0)));
        TriggerEvaluator evaluator = new TriggerEvaluator(new PredicateDsl(new DslCompiler()), new DefaultCanonLog());
        ChroniclerScheduler scheduler = new ChroniclerScheduler(graph, evaluator, 10);
        ArchetypeInstantiator instantiator = new ArchetypeInstantiator(List.of());
        WorldEventEmitter emitter = new WorldEventEmitter();
        DefaultChronicler chronicler = new DefaultChronicler(graph, evaluator, scheduler, instantiator, emitter);

        List<WorldEvent> emitted = new ArrayList<>();
        chronicler.registerWorldEventListener(emitted::add);

        chronicler.tick(CanonTime.of(5, 0));

        assertTrue(emitted.isEmpty());
    }

    @Test
    void tickWithInstantiableArchetypeEmitsArchetypeWorldEvent() {
        QuestGraph graph = GraphLoader.buildManually(List.of());
        DefaultCanonLog canonLog = new DefaultCanonLog();
        TriggerEvaluator evaluator = new TriggerEvaluator(new PredicateDsl(new DslCompiler()), canonLog);
        ChroniclerScheduler scheduler = new ChroniclerScheduler(graph, evaluator, 10);
        ArchetypeInstantiator instantiator = new ArchetypeInstantiator(List.of(new ChroniclerNodeArchetype() {
            @Override
            public String archetypeId() {
                return "a";
            }

            @Override
            public String archetypeName() {
                return "a";
            }

            @Override
            public boolean canInstantiate(CanonLog canonLog, CanonTime currentTime) {
                return true;
            }

            @Override
            public WorldEvent instantiate(CanonLog canonLog, CanonTime currentTime) {
                return WorldEvent.of("a-node", "a", Map.of(), 1, currentTime);
            }
        }));
        WorldEventEmitter emitter = new WorldEventEmitter();
        DefaultChronicler chronicler = new DefaultChronicler(graph, evaluator, scheduler, instantiator, emitter);

        List<WorldEvent> emitted = new ArrayList<>();
        chronicler.registerWorldEventListener(emitted::add);

        chronicler.tick(CanonTime.of(5, 0));

        assertEquals(1, emitted.size());
        assertEquals("a-node", emitted.get(0).nodeId());
    }

    @Test
    void chroniclerNeverAppendsDirectlyToCanonLog() {
        QuestGraph graph = GraphLoader.buildManually(List.of(StoryNode.of("n1", "authored", "canonTime > 0", List.of(), 1, false, 0)));
        CanonLog appendFailingLog = new CanonLog() {
            private final DefaultCanonLog delegate = new DefaultCanonLog();

            @Override
            public void append(CanonEvent event) {
                throw new RuntimeException("append should not be called by Chronicler");
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

        TriggerEvaluator evaluator = new TriggerEvaluator(new PredicateDsl(new DslCompiler()), appendFailingLog);
        ChroniclerScheduler scheduler = new ChroniclerScheduler(graph, evaluator, 10);
        ArchetypeInstantiator instantiator = new ArchetypeInstantiator(List.of());
        WorldEventEmitter emitter = new WorldEventEmitter();
        DefaultChronicler chronicler = new DefaultChronicler(graph, evaluator, scheduler, instantiator, emitter);
        chronicler.registerWorldEventListener(event -> { });

        chronicler.tick(CanonTime.of(5, 0));
    }

    @Test
    void registerWorldEventListenerWiresCorrectly() {
        QuestGraph graph = GraphLoader.buildManually(List.of(StoryNode.of("n1", "authored", "canonTime > 0", List.of(), 1, false, 0)));
        TriggerEvaluator evaluator = new TriggerEvaluator(new PredicateDsl(new DslCompiler()), new DefaultCanonLog());
        ChroniclerScheduler scheduler = new ChroniclerScheduler(graph, evaluator, 10);
        ArchetypeInstantiator instantiator = new ArchetypeInstantiator(List.of());
        WorldEventEmitter emitter = new WorldEventEmitter();
        DefaultChronicler chronicler = new DefaultChronicler(graph, evaluator, scheduler, instantiator, emitter);

        AtomicBoolean called = new AtomicBoolean(false);
        chronicler.registerWorldEventListener(event -> called.set(true));

        chronicler.tick(CanonTime.of(5, 0));

        assertTrue(called.get());
    }
}
