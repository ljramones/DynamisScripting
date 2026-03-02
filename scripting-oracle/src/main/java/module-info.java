module org.dynamisscripting.oracle {
    requires static com.github.spotbugs.annotations;
    requires dynamis.event;
    requires org.dynamisscripting.api;
    requires org.dynamisscripting.spi;
    requires org.dynamisscripting.canon;
    requires org.dynamisscripting.dsl;

    exports org.dynamisscripting.oracle;
}
