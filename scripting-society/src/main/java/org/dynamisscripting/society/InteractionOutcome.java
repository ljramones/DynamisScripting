package org.dynamisscripting.society;

public record InteractionOutcome(
        String societyAId,
        String societyBId,
        InteractionMode mode,
        double strength,
        String dominantDimensionId,
        String explanation) {

    public InteractionOutcome {
        if (societyAId == null || societyAId.isBlank()) {
            throw new SocietyException("InteractionOutcome", "societyAId must not be null or blank");
        }
        if (societyBId == null || societyBId.isBlank()) {
            throw new SocietyException("InteractionOutcome", "societyBId must not be null or blank");
        }
        if (mode == null) {
            throw new SocietyException("InteractionOutcome", "mode must not be null");
        }
        if (strength < 0.0D || strength > 1.0D) {
            throw new SocietyException("InteractionOutcome", "strength must be in range [0.0, 1.0]");
        }
        if (dominantDimensionId == null || dominantDimensionId.isBlank()) {
            throw new SocietyException("InteractionOutcome", "dominantDimensionId must not be null or blank");
        }
        if (explanation == null || explanation.isBlank()) {
            throw new SocietyException("InteractionOutcome", "explanation must not be null or blank");
        }
    }

    public static InteractionOutcome of(
            String societyAId,
            String societyBId,
            InteractionMode mode,
            double strength,
            String dominantDimensionId,
            String explanation) {
        return new InteractionOutcome(societyAId, societyBId, mode, strength, dominantDimensionId, explanation);
    }
}
