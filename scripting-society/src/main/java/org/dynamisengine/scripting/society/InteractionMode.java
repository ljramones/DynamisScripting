package org.dynamisengine.scripting.society;

public enum InteractionMode {
    ALIGNMENT("Vectors point in similar directions; cooperation is natural"),
    OPPOSITION("Vectors point in opposite directions; structural friction"),
    ORTHOGONALITY("Vectors are weakly related; incomprehension dominates");

    private final String description;

    InteractionMode(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }
}
