package org.dynamisscripting.society;

import java.util.List;
import org.dynamisscripting.spi.SocietyVectorDimension;

public final class InteractionFunction {
    private final List<SocietyVectorDimension> dimensions;

    public InteractionFunction(List<SocietyVectorDimension> dimensions) {
        if (dimensions == null) {
            throw new SocietyException("InteractionFunction", "dimensions must not be null");
        }
        for (SocietyVectorDimension dimension : dimensions) {
            if (dimension == null) {
                throw new SocietyException("InteractionFunction", "dimensions must not contain null entries");
            }
        }
        this.dimensions = List.copyOf(dimensions);
    }

    public InteractionOutcome compute(SocietyProfile profileA, SocietyProfile profileB) {
        if (profileA == null || profileB == null) {
            throw new SocietyException("compute", "profiles must not be null");
        }

        if (dimensions.isEmpty()) {
            String explanation = profileA.societyId()
                    + " vs "
                    + profileB.societyId()
                    + ": "
                    + InteractionMode.ORTHOGONALITY
                    + " (strength=0.0, dominant=none)";
            return InteractionOutcome.of(
                    profileA.societyId(),
                    profileB.societyId(),
                    InteractionMode.ORTHOGONALITY,
                    0.0D,
                    "none",
                    explanation);
        }

        double contributionSum = 0.0D;
        String dominantDimensionId = "none";
        double dominantContributionMagnitude = -1.0D;

        for (SocietyVectorDimension dimension : dimensions) {
            String dimensionId = dimension.dimensionId();
            double contribution;
            if (!profileA.hasDimension(dimensionId) || !profileB.hasDimension(dimensionId)) {
                contribution = 0.0D;
            } else {
                contribution = dimension.computeInteractionContribution(
                        profileA.weightFor(dimensionId),
                        profileB.weightFor(dimensionId));
            }
            contributionSum += contribution;

            double absContribution = Math.abs(contribution);
            if (absContribution > dominantContributionMagnitude) {
                dominantContributionMagnitude = absContribution;
                dominantDimensionId = dimensionId;
            }
        }

        double averageContribution = contributionSum / dimensions.size();
        InteractionMode mode;
        if (averageContribution >= 0.3D) {
            mode = InteractionMode.ALIGNMENT;
        } else if (averageContribution <= -0.3D) {
            mode = InteractionMode.OPPOSITION;
        } else {
            mode = InteractionMode.ORTHOGONALITY;
        }

        double strength = Math.abs(averageContribution);
        String explanation = profileA.societyId()
                + " vs "
                + profileB.societyId()
                + ": "
                + mode
                + " (strength="
                + strength
                + ", dominant="
                + dominantDimensionId
                + ")";

        return InteractionOutcome.of(
                profileA.societyId(),
                profileB.societyId(),
                mode,
                strength,
                dominantDimensionId,
                explanation);
    }
}
