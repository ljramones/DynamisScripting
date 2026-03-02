module org.dynamisscripting.chronicler {
    requires static com.github.spotbugs.annotations;
    requires java.logging;
    requires org.dynamisscripting.api;
    requires org.dynamisscripting.spi;
    requires org.dynamisscripting.canon;
    requires org.dynamisscripting.dsl;

    exports org.dynamisscripting.chronicler;
}
