package pandas.gather;

import jakarta.persistence.*;
import org.hibernate.search.engine.backend.types.Aggregable;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;

import java.util.Arrays;
import java.util.Map;

import static java.util.stream.Collectors.toUnmodifiableMap;

public enum State {
    ARCHIVED(1, "has been archived"),
    AWAIT_GATHER(2, "is awaiting gathering"),
    CHECKED(3, "has been problem checked"),
    CHECKING(4, "is being problem checking"),
    CREATION(5, "is being created"),
    DELETED(6, "has been deleted"),
    DELETING(7, "is being deleted"),
    GATHER_PAUSE(8, "is paused during gathering"),
    GATHER_PROCESS(9, "is post-processing"),
    GATHERED(10, "has been gathered"),
    GATHERING(12, "is gathering"),
    ARCHIVING(13, "is archiving"),
    FAILED(14, "is in a failed state");

    private static final Map<Integer, State> byId = Arrays.stream(State.values())
            .collect(toUnmodifiableMap(State::id, state -> state));

    private final int id;
    private final String wording;

    @GenericField(aggregable = Aggregable.YES)
    private final String stateName = camelCase(name());

    State(int id, String wording) {
        this.id = id;
        this.wording = wording;
    }

    public int id() {
        return id;
    }

    private String camelCase(String name) {
        String[] parts = name.split("_");
        StringBuilder result = new StringBuilder(parts[0].toLowerCase());
        for (int i = 1; i < parts.length; i++) {
            result.append(parts[i].substring(0, 1).toUpperCase())
                    .append(parts[i].substring(1).toLowerCase());
        }
        return result.toString();
    }


    public boolean isDeletedOrDeleting() {
        return this == DELETED || this == DELETING;
    }

    public boolean isFailed() {
        return this == FAILED;
    }

    public boolean isArchived() {
        return this == ARCHIVED;
    }

    public boolean isArchivedOrArchiving() {
        return this == ARCHIVING || isArchived();
    }

    public boolean canBeRetried() {
        return switch (this) {
            case ARCHIVING, DELETING, GATHER_PROCESS, GATHERING -> true;
            default -> false;
        };
    }

    public boolean isGatheringOrCreation() {
        return this == GATHERING || this == CREATION;
    }

    public boolean isGathered() {
        return this == GATHERED;
    }

    public boolean isGathering() {
        return this == GATHERING;
    }

    public boolean isCreation() {
        return this == CREATION;
    }

    public String getStateName() {
        return stateName;
    }

    public String getWording() {
        return wording;
    }

    @Converter(autoApply = true)
    public static class JPAConverter implements AttributeConverter<State, Integer> {
        @Override
        public Integer convertToDatabaseColumn(State state) {
            return state == null ? null : state.id;
        }

        @Override
        public State convertToEntityAttribute(Integer id) {
            if (id == null) return null;
            State state = byId.get(id);
            if (state == null) throw new IllegalArgumentException("No State found for id: " + id);
            return state;
        }
    }
}
