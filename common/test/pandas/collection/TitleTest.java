package pandas.collection;

import org.junit.jupiter.api.Test;
import pandas.agency.Agency;
import pandas.agency.User;
import pandas.core.Organisation;
import pandas.gather.GatherMethod;
import pandas.gather.Instance;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class TitleTest {
    @Test
    void firstGatherDateIsTheEarliestInstanceDate() {
        Title title = new Title();
        Instant first = Instant.parse("2024-01-02T03:04:05Z");
        Instant second = Instant.parse("2025-02-03T04:05:06Z");

        title.getInstances().add(new Instance(title, second, GatherMethod.HERITRIX));
        title.getInstances().add(new Instance(title, first, GatherMethod.HERITRIX));

        assertEquals(first, title.getFirstGatherDate());
    }

    @Test
    void firstGatherDateIsNullWithoutInstances() {
        assertNull(new Title().getFirstGatherDate());
    }

    @Test
    void selectingANominatedTitleTransfersOwnershipToTheSelector() {
        User nominator = user("nominator", agency(1L));
        User selector = user("selector", agency(2L));
        Title title = new Title(nominator, Instant.parse("2024-01-01T00:00:00Z"), "Nominated new title");
        title.changeStatus(Status.NOMINATED, null, nominator, Instant.parse("2024-01-01T00:00:00Z"));

        Instant selectedAt = Instant.parse("2024-02-01T00:00:00Z");
        title.changeStatus(Status.SELECTED, null, selector, selectedAt);

        assertSame(selector, title.getOwner());
        assertSame(selector.getAgency(), title.getAgency());
        assertSame(nominator, title.getNominator());
        assertEquals(2, title.getOwnerHistories().size());
        var history = title.getOwnerHistories().get(1);
        assertSame(selector, history.getUser());
        assertSame(selector, history.getTransferrer());
        assertEquals(selectedAt, history.getDate());
    }

    @Test
    void otherStatusChangesDoNotTransferOwnership() {
        User nominator = user("nominator", agency(1L));
        User other = user("other", agency(2L));
        Title title = new Title(nominator, Instant.parse("2024-01-01T00:00:00Z"), "Nominated new title");
        title.changeStatus(Status.NOMINATED, null, nominator, Instant.now());

        title.changeStatus(Status.REJECTED, null, other, Instant.now());
        title.changeStatus(Status.SELECTED, null, other, Instant.now());

        assertSame(nominator, title.getOwner());
        assertEquals(1, title.getOwnerHistories().size());
    }

    private static Agency agency(long id) {
        Agency agency = new Agency();
        agency.setId(id);
        agency.setOrganisation(new Organisation());
        return agency;
    }

    private static User user(String userid, Agency agency) {
        User user = new User(agency);
        user.setUserid(userid);
        return user;
    }
}
