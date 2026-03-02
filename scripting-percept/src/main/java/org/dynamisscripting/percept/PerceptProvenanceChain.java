package org.dynamisscripting.percept;

public record PerceptProvenanceChain(
        long sourceCommitId,
        String derivationSteps,
        double originalFidelity,
        double deliveredFidelity,
        long computedDelayTicks) {

    public static PerceptProvenanceChain of(
            long sourceCommitId,
            String derivationSteps,
            double originalFidelity,
            double deliveredFidelity,
            long computedDelayTicks) {
        return new PerceptProvenanceChain(
                sourceCommitId,
                derivationSteps,
                originalFidelity,
                deliveredFidelity,
                computedDelayTicks);
    }
}
