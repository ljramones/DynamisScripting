package org.dynamisengine.scripting.percept;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.scripting.api.CanonLog;
import org.dynamisengine.scripting.api.PerceptBus;
import org.dynamisengine.scripting.api.value.Percept;
import org.dynamisengine.scripting.spi.PerceptFilter;

public final class DefaultPerceptBus implements PerceptBus {
    private static final Comparator<PerceptFilter> FILTER_ORDER =
            Comparator.comparingInt(PerceptFilter::priority).thenComparing(PerceptFilter::filterId);

    private final ConcurrentHashMap<EntityId, PerceptDeliveryPolicy> subscriptions;
    private final ConcurrentHashMap<EntityId, Consumer<Percept>> listeners;
    private final FidelityModel fidelityModel;
    private final PerceptDownsampler downsampler;
    private final List<PerceptFilter> filters;
    private final int stormThreshold;
    private final CanonLog canonLog;

    @SuppressFBWarnings(
            value = {"EI_EXPOSE_REP2"},
            justification = "PerceptBus intentionally shares CanonLog as a read-only dependency")
    public DefaultPerceptBus(
            FidelityModel fidelityModel,
            PerceptDownsampler downsampler,
            CanonLog canonLog,
            int stormThreshold) {
        if (fidelityModel == null || downsampler == null || canonLog == null) {
            throw new PerceptException("constructor", "fidelityModel, downsampler, and canonLog must not be null");
        }
        if (stormThreshold < 0) {
            throw new PerceptException("constructor", "stormThreshold must be >= 0");
        }

        this.subscriptions = new ConcurrentHashMap<>();
        this.listeners = new ConcurrentHashMap<>();
        this.fidelityModel = fidelityModel;
        this.downsampler = downsampler;
        this.filters = new ArrayList<>();
        this.stormThreshold = stormThreshold;
        this.canonLog = canonLog;
    }

    @Override
    public void subscribe(EntityId agentId, Consumer<Percept> listener) {
        subscribeWithPolicy(
                agentId,
                listener,
                PerceptDeliveryPolicy.of(agentId, Set.of(), 0.0D, true));
    }

    public void subscribeWithPolicy(EntityId agentId, Consumer<Percept> listener, PerceptDeliveryPolicy policy) {
        if (agentId == null || listener == null || policy == null) {
            throw new PerceptException("subscribeWithPolicy", "agentId, listener, and policy must not be null");
        }
        subscriptions.put(agentId, policy);
        listeners.put(agentId, listener);
    }

    @Override
    public void unsubscribe(EntityId agentId) {
        if (agentId == null) {
            throw new PerceptException("unsubscribe", "agentId must not be null");
        }
        subscriptions.remove(agentId);
        listeners.remove(agentId);
    }

    @Override
    public int subscriberCount() {
        return subscriptions.size();
    }

    @Override
    public void deliver(Percept percept) {
        if (percept == null) {
            throw new PerceptException("deliver", "percept must not be null");
        }

        // PerceptBus is a view layer — it never introduces new canonical facts
        for (var entry : subscriptions.entrySet()) {
            EntityId agentId = entry.getKey();
            PerceptDeliveryPolicy policy = entry.getValue();
            Consumer<Percept> listener = listeners.get(agentId);
            if (listener == null) {
                continue;
            }
            if (!policy.acceptsType(percept.perceptType())) {
                continue;
            }

            Percept processed = percept;
            boolean shouldDeliver = true;
            for (PerceptFilter filter : filters) {
                if (!filter.shouldDeliver(processed, agentId, canonLog)) {
                    shouldDeliver = false;
                    break;
                }
                processed = filter.degrade(processed, agentId, canonLog);
                if (processed == null) {
                    shouldDeliver = false;
                    break;
                }
            }
            if (!shouldDeliver) {
                continue;
            }
            if (processed.fidelity() < policy.minimumFidelity()) {
                continue;
            }
            if (!policy.acceptDownsampled() && processed.fidelity() < 1.0D) {
                continue;
            }

            List<Percept> selected = downsampler.isEventStorm(subscriberCount(), stormThreshold)
                    ? downsampler.downsample(List.of(processed), 1)
                    : List.of(processed);
            if (selected.isEmpty()) {
                continue;
            }
            listener.accept(selected.getFirst());
        }
    }

    public void registerFilter(PerceptFilter filter) {
        if (filter == null) {
            throw new PerceptException("registerFilter", "filter must not be null");
        }
        filters.add(filter);
        filters.sort(FILTER_ORDER);
    }

    public PerceptProvenanceChain traceProvenance(Percept percept, EntityId agentId) {
        if (percept == null || agentId == null) {
            throw new PerceptException("traceProvenance", "percept and agentId must not be null");
        }

        double original = percept.fidelity();
        Percept current = percept;
        StringBuilder steps = new StringBuilder("start=").append(original);
        for (PerceptFilter filter : filters) {
            if (!filter.shouldDeliver(current, agentId, canonLog)) {
                steps.append(" -> blockedBy=").append(filter.filterId());
                break;
            }
            Percept degraded = filter.degrade(current, agentId, canonLog);
            if (degraded == null) {
                steps.append(" -> nullFrom=").append(filter.filterId());
                break;
            }
            steps.append(" -> ").append(filter.filterId()).append(":").append(degraded.fidelity());
            current = degraded;
        }
        double normalizedDelivered = fidelityModel.computeFidelity(
                (1.0D - current.fidelity()) * FidelityModel.MAX_DISTANCE,
                false,
                false,
                true);

        return PerceptProvenanceChain.of(
                percept.sourceCommitId(),
                steps.toString(),
                original,
                normalizedDelivered,
                0L);
    }
}
