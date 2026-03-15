package org.dynamisengine.scripting.canon;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import org.dynamisengine.scripting.api.CanonLog;
import org.dynamisengine.scripting.api.value.CanonEvent;
import org.dynamisengine.scripting.api.value.CanonTime;

public final class CanonLogQuery {
    private CanonLogQuery() {
    }

    public static int count(CanonLog log, CanonTime from, CanonTime to, Predicate<CanonEvent> filter) {
        requireNonNull(log, "log");
        requireNonNull(from, "from");
        requireNonNull(to, "to");
        requireNonNull(filter, "filter");

        int total = 0;
        for (CanonEvent event : log.query(from, to)) {
            if (filter.test(event)) {
                total++;
            }
        }
        return total;
    }

    public static List<CanonEvent> filterByCausalLink(CanonLog log, String causalLinkPrefix) {
        requireNonNull(log, "log");
        if (causalLinkPrefix == null || causalLinkPrefix.isBlank()) {
            throw new CanonLogException("filterByCausalLink", "causalLinkPrefix must not be null or empty");
        }

        List<CanonEvent> events = log.query(CanonTime.ZERO, log.latestCanonTime());
        return events.stream()
                .filter(event -> event.causalLink().startsWith(causalLinkPrefix))
                .toList();
    }

    public static Optional<CanonEvent> latestMatching(CanonLog log, Predicate<CanonEvent> filter) {
        requireNonNull(log, "log");
        requireNonNull(filter, "filter");

        List<CanonEvent> events = log.query(CanonTime.ZERO, log.latestCanonTime());
        for (int i = events.size() - 1; i >= 0; i--) {
            CanonEvent event = events.get(i);
            if (filter.test(event)) {
                return Optional.of(event);
            }
        }
        return Optional.empty();
    }

    private static <T> T requireNonNull(T value, String field) {
        if (value == null) {
            throw new CanonLogException("query", field + " must not be null");
        }
        return value;
    }
}
