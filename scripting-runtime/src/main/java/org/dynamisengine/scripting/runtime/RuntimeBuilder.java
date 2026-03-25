package org.dynamisengine.scripting.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.dynamisengine.event.EventBus;
import org.dynamisengine.event.EventBusBuilder;
import org.dynamisengine.scripting.api.value.WorldEvent;
import org.dynamisengine.scripting.canon.CanonTimekeeper;
import org.dynamisengine.scripting.canon.DefaultCanonLog;
import org.dynamisengine.scripting.chronicler.ArchetypeInstantiator;
import org.dynamisengine.scripting.chronicler.ChroniclerScheduler;
import org.dynamisengine.scripting.chronicler.DefaultChronicler;
import org.dynamisengine.scripting.chronicler.GraphLoader;
import org.dynamisengine.scripting.chronicler.QuestGraph;
import org.dynamisengine.scripting.chronicler.StoryNode;
import org.dynamisengine.scripting.chronicler.TriggerEvaluator;
import org.dynamisengine.scripting.chronicler.WorldEventEmitter;
import org.dynamisengine.scripting.dsl.DslCompiler;
import org.dynamisengine.scripting.dsl.PredicateDsl;
import org.dynamisengine.scripting.dsl.RewriteDsl;
import org.dynamisengine.scripting.oracle.BudgetLedger;
import org.dynamisengine.scripting.oracle.CommitPhase;
import org.dynamisengine.scripting.oracle.DefaultWorldOracle;
import org.dynamisengine.scripting.oracle.RuleRegistry;
import org.dynamisengine.scripting.oracle.ShapePhase;
import org.dynamisengine.scripting.oracle.ValidatePhase;
import org.dynamisengine.scripting.percept.DefaultPerceptBus;
import org.dynamisengine.scripting.percept.FidelityModel;
import org.dynamisengine.scripting.percept.PerceptDownsampler;
import org.dynamisengine.scripting.society.SocietyProfile;
import org.dynamisengine.scripting.society.SocietyRegistry;
import org.dynamisengine.scripting.spi.ArbitrationRule;
import org.dynamisengine.scripting.spi.CanonDimensionProvider;
import org.dynamisengine.scripting.spi.ChroniclerNodeArchetype;
import org.dynamisengine.scripting.spi.IntentInterceptor;
import org.dynamisengine.scripting.spi.PerceptFilter;

public final class RuntimeBuilder {
    private RuntimeConfiguration configuration;
    private final List<CanonDimensionProvider> dimensions;
    private final List<ArbitrationRule> arbitrationRules;
    private final List<ChroniclerNodeArchetype> archetypes;
    private final List<StoryNode> storyNodes;
    private final List<SocietyProfile> societyProfiles;
    private final List<PerceptFilter> perceptFilters;
    private final List<IntentInterceptor> interceptors;

    private RuntimeBuilder() {
        this.configuration = RuntimeConfiguration.defaults();
        this.dimensions = new ArrayList<>();
        this.arbitrationRules = new ArrayList<>();
        this.archetypes = new ArrayList<>();
        this.storyNodes = new ArrayList<>();
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

    public RuntimeBuilder withStoryNode(StoryNode storyNode) {
        this.storyNodes.add(requireNonNull(storyNode, "storyNode"));
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
                event -> eventBus.publish(new CanonLogEvent(event)));

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
        QuestGraph questGraph = storyNodes.isEmpty()
                ? new QuestGraph()
                : GraphLoader.buildManually(List.copyOf(storyNodes));
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
                configuration,
                dslCompiler);

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
