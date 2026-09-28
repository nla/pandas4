package pandas.collection;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import pandas.agency.Agency;
import pandas.agency.User;
import pandas.core.Organisation;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransferNotifierTest {
    private final JavaMailSender mailSender = mock(JavaMailSender.class);

    @SuppressWarnings("unchecked")
    private TransferNotifier notifier(JavaMailSender sender, String from) {
        ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(sender);
        return new TransferNotifier(provider, from, "https://pandas.example.org/");
    }

    private static User user(long id, String userid, String nameGiven, String nameFamily, String email, Agency agency) {
        User user = new User(agency);
        user.setId(id);
        user.setUserid(userid);
        user.setNameGiven(nameGiven);
        user.setNameFamily(nameFamily);
        user.setEmail(email);
        return user;
    }

    private static Agency agency() {
        return agency(1L);
    }

    private static Agency agency(long id) {
        Organisation organisation = new Organisation();
        organisation.setName("Test Library");
        Agency agency = new Agency();
        agency.setId(id);
        agency.setOrganisation(organisation);
        return agency;
    }

    private static TitleTransferredEvent transfer(User from, User to, String note) {
        Title title = new Title(from, Instant.now());
        title.setId(42L);
        title.setPi(12345L);
        title.setName("Example Website");
        OwnerHistory ownerHistory = title.transferOwnership(to.getAgency(), to, note, from, Instant.now());
        assertNotNull(ownerHistory);
        return new TitleTransferredEvent(ownerHistory);
    }

    @Test
    void sendsNotificationToNewOwner() {
        Agency agency = agency();
        User alice = user(1, "alice", "Alice", "Adams", "alice@example.org", agency);
        User bob = user(2, "bob", "Bob", "Brown", "bob@example.org", agency);

        notifier(mailSender, "pandas@example.org").onTitleTransferred(transfer(alice, bob, "Please check the schedule."));

        var captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage message = captor.getValue();
        assertEquals("pandas@example.org", message.getFrom());
        assertArrayEquals(new String[]{"bob@example.org"}, message.getTo());
        assertEquals("alice@example.org", message.getReplyTo());
        assertEquals("PANDAS: Example Website has been transferred to you", message.getSubject());
        String body = message.getText();
        assertTrue(body.startsWith("Hi Bob,"), body);
        assertTrue(body.contains("Alice Adams has transferred"), body);
        assertTrue(body.contains("Example Website (nla.arc-12345)"), body);
        assertTrue(body.contains("https://pandas.example.org/titles/42"), body);
        assertTrue(body.contains("Agency: Test Library"), body);
        assertTrue(body.contains("Please check the schedule."), body);
    }

    @Test
    void mentionsMissingNote() {
        Agency agency = agency();
        User alice = user(1, "alice", "Alice", "Adams", "alice@example.org", agency);
        User bob = user(2, "bob", "Bob", "Brown", "bob@example.org", agency);

        notifier(mailSender, "pandas@example.org").onTitleTransferred(transfer(alice, bob, "  "));

        var captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertTrue(captor.getValue().getText().contains("No transfer note was given."));
    }

    @Test
    void skipsSelfTransfer() {
        Agency agency = agency();
        Agency otherAgency = agency(2L);
        User alice = user(1, "alice", "Alice", "Adams", "alice@example.org", agency);
        Title title = new Title(alice, Instant.now());
        title.setName("Example Website");
        // same owner, different agency, so a history entry is still created
        OwnerHistory ownerHistory = title.transferOwnership(otherAgency, alice, "note", alice, Instant.now());
        assertNotNull(ownerHistory);

        notifier(mailSender, "pandas@example.org").onTitleTransferred(new TitleTransferredEvent(ownerHistory));

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void skipsRecipientWithoutEmail() {
        Agency agency = agency();
        User alice = user(1, "alice", "Alice", "Adams", "alice@example.org", agency);
        User bob = user(2, "bob", "Bob", "Brown", null, agency);

        notifier(mailSender, "pandas@example.org").onTitleTransferred(transfer(alice, bob, "note"));

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void skipsWhenFromAddressOrSenderMissing() {
        Agency agency = agency();
        User alice = user(1, "alice", "Alice", "Adams", "alice@example.org", agency);
        User bob = user(2, "bob", "Bob", "Brown", "bob@example.org", agency);

        notifier(mailSender, "").onTitleTransferred(transfer(alice, bob, "note"));
        notifier(null, "pandas@example.org").onTitleTransferred(transfer(alice, bob, "note"));

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void swallowsSendFailures() {
        Agency agency = agency();
        User alice = user(1, "alice", "Alice", "Adams", "alice@example.org", agency);
        User bob = user(2, "bob", "Bob", "Brown", "bob@example.org", agency);
        doThrow(new org.springframework.mail.MailSendException("boom")).when(mailSender).send(any(SimpleMailMessage.class));

        assertDoesNotThrow(() -> notifier(mailSender, "pandas@example.org")
                .onTitleTransferred(transfer(alice, bob, "note")));
    }
}
