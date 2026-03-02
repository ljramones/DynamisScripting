package org.dynamisscripting.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.dynamis.event.EventBus;
import org.dynamis.event.EventBusBuilder;
import org.dynamisscripting.api.value.WorldEvent;
import org.dynamisscripting.canon.CanonTimekeeper;
import org.dynamisscripting.canon.DefaultCanonLog;
import org.dynamisscripting.chronicler.ArchetypeInstantiator;
import org.dynamisscripting.chronicler.ChroniclerScheduler;
import org.dynamisscripting.chronicler.DefaultChronicler;
import org.dynamisscripting.chronicler.QuestGraph;
import org.dynamisscripting.chronicler.TriggerEvaluator;
import org.dynamisscripting.chronicler.WorldEventEmitter;
import org.dynamisscripting.dsl.DslCompiler;
import org.dynamisscripting.dsl.PredicateDsl;
import org.dynamisscripting.dsl.RewriteDsl;
import org.dynamisscripting.oracle.BudgetLedger;
import org.dynamisscripting.oracle.CommitPhase;
import org.dynamisscripting.oracle.DefaultWorldOracle;
import org.dynamisscripting.oracle.RuleRegistry;
import org.dynamisscripting.oracle.ShapePhase;
import org.dynamisscripting.oracle.ValidatePhase;
import org.dynamisscripting.percept.DefaultPerceptBus;
import org.dynamisscripting.percept.FidelityModel;
import org.dynamisscripting.percept.PerceptDownsampler;
import org.dynamisscripting.society.SocietyProfile;
import org.dynamisscripting.society.SocietyRegistry;
import org.dynamisscripting.spi.ArbitrationRule;
import org.dynamisscripting.spi.CanonDimensionProvider;
import org.dynamisscripting.spi.ChroniclerNodeArchetype;
import org.dynamisscripting.spi.IntentInterceptor;
import org.dynamisscripting.spi.PerceptFilter;

public final class RuntimeBuilder {
    private RuntimeConfiguration configuration;
    private final List<CanonDimensionProvider> dimensions;
    private final List<ArbitrationRule> arbitrationRules;
    private final List<ChroniclerNodeArchetype> archetypes;
    private final List<SocietyProfile> societyProfiles;
    private final List<PerceptFilter> perceptFilters;
    private final List<IntentInterceptor> interceptors;

    private RuntimeBuilder() {
        this.configuration = RuntimeConfiguration.defaults();
        this.dimensions = new ArrayList<>();
        this.arbitrationRules = new ArrayList<>();
        this.archetypes = new ArrayList<>();
        this.societyProfiles = new ArrayList<>();
        this.perceptFilters = new ArrayList<>();
        this.interceptors = new ArrayList<>();
    }

    public static RuntimeBuilder create() {
        return new RuntimeBuilder();
    }

    public RuntimeBuilder withConfiguration(RuntimeConfiguration config) {
        this.configuration = requireNonNull(config, "configuration");
        return this;
    }

    public RuntimeBuilder withDimension(CanonDimensionProvider dimension) {
        this.dimensions.add(requireNonNull(dimension, "dimension"));
        return this;
    }

    public RuntimeBuilder withArbitrationRule(ArbitrationRule rule) {
        this.arbitrationRules.add(requireNonNull(rule, "rule"));
        return this;
    }

    public RuntimeBuilder withArchetype(ChroniclerNodeArchetype archetype) {
        this.archetypes.add(requireNonNull(archetype, "archetype"));
        return this;
    }

    public RuntimeBuilder withSocietyProfile(SocietyProfile profile) {
        this.societyProfiles.add(requireNonNull(profile, "profile"));
        return this;
    }

    public RuntimeBuilder withPerceptFilter(PerceptFilter filter) {
        this.perceptFilters.add(requireNonNull(filter, "filter"));
        return this;
    }

    public RuntimeBuilder withInterceptor(IntentInterceptor interceptor) {
        this.interceptors.add(requireNonNull(interceptor, "interceptor"));
        return this;
    }

    public ScriptingRuntime build() {
        if (configuration == null) {
            throw new RuntimeException("build", "configuration must not be null");
        }
        if (dimensions.isEmpty()) {
            throw new RuntimeException("build", "at least one canonical dimension is required");
        }

        DefaultCanonLog canonLog = new DefaultCanonLog();
        CanonTimekeeper timekeeper = new CanonTimekeeper();
        AtomicLong commitIdCounter = new AtomicLong(1L);
        EventBus eventBus = EventBusBuilder.create().async(2).build();
        IntentBusImpl intentBus = new IntentBusImpl(eventBus);

        RuleRegistry ruleRegistry = new RuleRegistry();
        for (ArbitrationRule rule : arbitrationRules) {
            ruleRegistry.register(rule);
        }

        BudgetLedger budgetLedger = new BudgetLedger();
        ValidatePhase validatePhase = new ValidatePhase(ruleRegistry, budgetLedger);
        ShapePhase shapePhase = new ShapePhase(ruleRegistry);
        CommitPhase commitPhase = new CommitPhase(
                canonLog,
                timekeeper,
                commitIdCounter,
                eventBus,
                CanonLogEvent::new);

        DefaultWorldOracle oracle = new DefaultWorldOracle(
                validatePhase,
                shapePhase,
                commitPhase,
                List.copyOf(interceptors),
                canonLog);

        DslCompiler dslCompiler = new DslCompiler();
        PredicateDsl predicateDsl = new PredicateDsl(dslCompiler);
        new RewriteDsl(dslCompiler);

        TriggerEvaluator triggerEvaluator = new TriggerEvaluator(predicateDsl, canonLog);
        QuestGraph questGraph = new QuestGraph();
        ArchetypeInstantiator archetypeInstantiator = new ArchetypeInstantiator(List.copyOf(archetypes));
        ChroniclerScheduler scheduler = new ChroniclerScheduler(
                questGraph,
                triggerEvaluator,
                configuration.maxNodeActivationsPerTick());
        WorldEventEmitter emitter = new WorldEventEmitter();
        DefaultChronicler chronicler = new DefaultChronicler(
                questGraph,
                triggerEvaluator,
                scheduler,
                archetypeInstantiator,
                emitter);

        // Runtime default wiring: Chronicler proposals are forwarded to Oracle arbitration.
        chronicler.registerWorldEventListener((WorldEvent event) -> oracle.commitWorldEvent(event));

        FidelityModel fidelityModel = new FidelityModel();
        PerceptDownsampler downsampler = new PerceptDownsampler();
        DefaultPerceptBus perceptBus = new DefaultPerceptBus(
                fidelityModel,
                downsampler,
                canonLog,
                configuration.perceptStormThreshold());
        for (PerceptFilter filter : perceptFilters) {
            perceptBus.registerFilter(filter);
        }

        SocietyRegistry societyRegistry = new SocietyRegistry();
        for (SocietyProfile societyProfile : societyProfiles) {
            societyRegistry.register(societyProfile);
        }

        DegradationMonitor degradationMonitor = new DegradationMonitor(configuration.degradationThresholds());
        RuntimeTick runtimeTick = new RuntimeTick(
                timekeeper,
                chronicler,
                oracle,
                perceptBus,
                degradationMonitor,
                configuration);

        return new ScriptingRuntime(
                canonLog,
                timekeeper,
                degradationMonitor,
                runtimeTick,
                dslCompiler,
                List.copyOf(dimensions),
                commitIdCounter,
                eventBus,
                intentBus);
    }

    private static <T> T requireNonNull(T value, String field) {
        if (value == null) {
            throw new RuntimeException("RuntimeBuilder", field + " must not be null");
        }
        return value;
    }
}
