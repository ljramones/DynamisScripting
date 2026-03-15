module org.dynamisengine.scripting.chronicler {
    requires static com.github.spotbugs.annotations;
    requires java.logging;
    requires org.dynamisengine.scripting.api;
    requires org.dynamisengine.scripting.spi;
    requires org.dynamisengine.scripting.canon;
    requires org.dynamisengine.scripting.dsl;

    exports org.dynamisengine.scripting.chronicler;
}
