package org.dynamisscripting.oracle;

import java.util.List;
import org.dynamis.core.entity.EntityId;
import org.dynamis.core.exception.DynamisException;
import org.dynamisscripting.api.value.CanonTime;

public record OracleExplainReport(
        String intentType,
        EntityId agentId,
        String phase,
        String outcome,
        String reasonCode,
        String explanation,
        List<String> appliedRuleIds,
        CanonTime canonTime) {

    public OracleExplainReport {
        requireNonBlank(intentType, "intentType");
        requireNonNull(agentId, "agentId");
        requireNonBlank(phase, "phase");
        requireNonBlank(outcome, "outcome");
        requireNonBlank(reasonCode, "reasonCode");
        requireNonNull(explanation, "explanation");
        requireNonNull(appliedRuleIds, "appliedRuleIds");
        appliedRuleIds = List.copyOf(appliedRuleIds);
        requireNonNull(canonTime, "canonTime");
    }

    public static OracleExplainReport of(
            String intentType,
            EntityId agentId,
            String phase,
            String outcome,
            String reasonCode,
            String explanation,
            List<String> appliedRuleIds,
            CanonTime canonTime) {
        return new OracleExplainReport(
                intentType,
                agentId,
                phase,
                outcome,
                reasonCode,
                explanation,
                appliedRuleIds,
                canonTime);
    }

    private static <T> T requireNonNull(T value, String field) {
        if (value == null) {
            throw new DynamisException(field + " must not be null");
        }
        return value;
    }

    private static String requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new DynamisException(field + " must not be null or blank");
        }
        return value;
    }
}
