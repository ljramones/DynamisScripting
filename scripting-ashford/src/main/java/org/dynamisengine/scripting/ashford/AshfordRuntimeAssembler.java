package org.dynamisengine.scripting.ashford;

import java.util.List;
import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.scripting.api.value.CanonEvent;
import org.dynamisengine.scripting.api.value.CanonTime;
import org.dynamisengine.scripting.chronicler.StoryNode;
import org.dynamisengine.scripting.economy.EconomicsArchetype;
import org.dynamisengine.scripting.economy.EconomicsBudgetRule;
import org.dynamisengine.scripting.economy.EconomicsDimension;
import org.dynamisengine.scripting.economy.FactionFunds;
import org.dynamisengine.scripting.runtime.RuntimeBuilder;
import org.dynamisengine.scripting.runtime.RuntimeConfiguration;
import org.dynamisengine.scripting.runtime.ScriptingRuntime;

public final class AshfordRuntimeAssembler {
    private static final EntityId MERCHANT_LEADER = EntityId.of(1L);
    private static final EntityId WATCH_LEADER = EntityId.of(2L);
    private static final EntityId THIEVES_LEADER = EntityId.of(3L);

    private AshfordRuntimeAssembler() {
    }

    public static ScriptingRuntime assemble() {
        ScriptingRuntime runtime = RuntimeBuilder.create()
                .withConfiguration(RuntimeConfiguration.defaults())
                .withDimension(new EconomicsDimension())
                .withArbitrationRule(new EconomicsBudgetRule(null))
                .withArchetype(new EconomicsArchetype())
                .withStoryNode(StoryNode.of(
                        "ashford.opening",
                        "authored",
                        "canonTime > 0",
                        List.of(),
                        100,
                        false,
                        0L))
                .withStoryNode(StoryNode.of(
                        "ashford.tension_rising",
                        "authored",
                        "canonTime > 10",
                        List.of("ashford.opening"),
                        90,
                        false,
                        0L))
                .withStoryNode(StoryNode.of(
                        "ashford.merchant_contract",
                        "economics.contract_escalation",
                        "canonTime > 2",
                        List.of(),
                        50,
                        true,
                        20L))
                .withStoryNode(StoryNode.of(
                        "ashford.stolen_shipment",
                        "authored",
                        "canonTime > 5",
                        List.of("ashford.merchant_contract"),
                        40,
                        false,
                        0L))
                .build();

        runtime.seedEvent(CanonEvent.of(
                1L,
                CanonTime.ZERO,
                "setup:merchant_guild:funds",
                FactionFunds.of(MERCHANT_LEADER, 50_000.0, 0.0)));
        runtime.seedEvent(CanonEvent.of(
                2L,
                CanonTime.ZERO,
                "setup:city_watch:funds",
                FactionFunds.of(WATCH_LEADER, 20_000.0, 0.0)));
        runtime.seedEvent(CanonEvent.of(
                3L,
                CanonTime.ZERO,
                "setup:thieves_guild:funds",
                FactionFunds.of(THIEVES_LEADER, 15_000.0, 0.0)));

        runtime.registerAgent(MERCHANT_LEADER);
        runtime.registerAgent(WATCH_LEADER);
        runtime.registerAgent(THIEVES_LEADER);

        return runtime;
    }
}
