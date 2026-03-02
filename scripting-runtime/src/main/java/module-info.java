module org.dynamisscripting.runtime {
    requires static com.github.spotbugs.annotations;
    requires org.dynamisscripting.api;
    requires org.dynamisscripting.spi;
    requires org.dynamisscripting.dsl;
    requires org.dynamisscripting.canon;
    requires org.dynamisscripting.oracle;
    requires org.dynamisscripting.chronicler;
    requires org.dynamisscripting.percept;
    requires org.dynamisscripting.society;
    requires org.dynamisscripting.economy;

    exports org.dynamisscripting.runtime;
}
