module org.dynamisengine.scripting.runtime {
    requires dynamis.event;
    requires static com.github.spotbugs.annotations;
    requires org.dynamisengine.scripting.api;
    requires org.dynamisengine.scripting.spi;
    requires org.dynamisengine.scripting.dsl;
    requires org.dynamisengine.scripting.canon;
    requires org.dynamisengine.scripting.oracle;
    requires org.dynamisengine.scripting.chronicler;
    requires org.dynamisengine.scripting.percept;
    requires org.dynamisengine.scripting.society;
    requires org.dynamisengine.scripting.economy;

    exports org.dynamisengine.scripting.runtime;
}
