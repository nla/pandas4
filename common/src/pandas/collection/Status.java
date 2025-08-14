package pandas.collection;

import jakarta.persistence.*;

import java.util.*;

import static java.util.stream.Collectors.toUnmodifiableMap;

public enum Status {
    NOMINATED(1),
    REJECTED(2),
    SELECTED(3),
    MONITORED(4),
    PERMISSION_REQUESTED(5),
    PERMISSION_DENIED(6),
    PERMISSION_GRANTED(7),
    PERMISSION_IMPOSSIBLE(8),
    CEASED(11);

    public static final Map<Status, EnumSet<Status>> allowedTransitions = Map.of(
            NOMINATED, EnumSet.of(SELECTED, MONITORED, REJECTED, CEASED),
            MONITORED, EnumSet.of(SELECTED, REJECTED, CEASED),
            REJECTED, EnumSet.of(NOMINATED, SELECTED, CEASED),
            SELECTED, EnumSet.of(REJECTED, CEASED),
            PERMISSION_REQUESTED, EnumSet.of(REJECTED, CEASED),
            PERMISSION_DENIED, EnumSet.of(REJECTED, CEASED),
            PERMISSION_GRANTED, EnumSet.of(REJECTED, CEASED),
            PERMISSION_IMPOSSIBLE, EnumSet.of(REJECTED, CEASED),
            CEASED, EnumSet.of(SELECTED)
    );

    private static final Map<Integer, Status> byId = Arrays.stream(Status.values())
            .collect(toUnmodifiableMap(Status::id, status -> status));

    private final int id;

    Status(int id) {
        this.id = id;
    }

    public static Iterable<Status> fromIds(Iterable<Long> ids) {
        List<Status> statuses = new ArrayList<>();
        for (Long id : ids) {
            Status status = fromId(id.intValue());
            if (status != null) {
                statuses.add(status);
            }
        }
        return statuses;
    }

    public boolean isTransitionAllowed(Status newStatus) {
        return allowedTransitions.getOrDefault(this, EnumSet.noneOf(Status.class)).contains(newStatus);
    }

    public int id() {
        return this.id;
    }

    public boolean isCeased() {
        return this == CEASED;
    }

    public boolean isRejected() {
        return this == REJECTED;
    }

    public boolean isActive() {
        return !isCeased() && !isRejected() && !isNominated();
    }

    private boolean isNominated() {
        return this == NOMINATED;
    }

    public Set<Status> getAllowedTransitions() {
        return allowedTransitions.getOrDefault(this, EnumSet.noneOf(Status.class));
    }

    public boolean isSelected() {
        return this == SELECTED;
    }

    public boolean isSelectedOrAnyPermission() {
        return switch (this) {
            case SELECTED, PERMISSION_REQUESTED, PERMISSION_GRANTED, PERMISSION_IMPOSSIBLE, PERMISSION_DENIED -> true;
            default -> false;
        };
    }

    public String getName() {
        return name().toLowerCase(Locale.ROOT).replace("_", " ");
    }

    public static Status fromId(int id) {
        return byId.get(id);
    }

    @Converter(autoApply = true)
    public static class JPAConverter implements AttributeConverter<Status, Integer> {
        @Override
        public Integer convertToDatabaseColumn(Status status) {
            return status == null ? null : status.id;
        }

        @Override
        public Status convertToEntityAttribute(Integer id) {
            if (id == null) return null;
            Status status = byId.get(id);
            if (status == null) throw new IllegalArgumentException("No Status found for id: " + id);
            return status;
        }
    }
}
