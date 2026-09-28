package pandas.collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pandas.agency.User;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

/**
 * Emails the new owner of a title when it is transferred to them, including the transferrer's note.
 */
@Component
public class TransferNotifier {
    private static final Logger log = LoggerFactory.getLogger(TransferNotifier.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String from;
    private final String baseUrl;

    public TransferNotifier(ObjectProvider<JavaMailSender> mailSenderProvider,
                            @Value("${pandas.mail.from:}") String from,
                            @Value("${pandas.mail.base-url:}") String baseUrl) {
        this.mailSenderProvider = mailSenderProvider;
        this.from = from;
        this.baseUrl = baseUrl;
    }

    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onTitleTransferred(TitleTransferredEvent event) {
        try {
            JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
            if (mailSender == null || from == null || from.isBlank()) return;
            SimpleMailMessage message = buildMessage(event.ownerHistory());
            if (message == null) return;
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send title transfer notification", e);
        }
    }

    SimpleMailMessage buildMessage(OwnerHistory ownerHistory) {
        User recipient = ownerHistory.getUser();
        User transferrer = ownerHistory.getTransferrer();
        if (recipient == null || isBlank(recipient.getEmail())) return null;
        if (transferrer != null && transferrer.equals(recipient)) return null;

        Title title = ownerHistory.getTitle();
        String transferrerName = transferrer == null ? "Someone" : transferrer.getFullName();

        var body = new StringBuilder();
        body.append("Hi ").append(isBlank(recipient.getNameGiven()) ? recipient.getUserid() : recipient.getNameGiven())
                .append(",\n\n");
        body.append(transferrerName).append(" has transferred the following title to you in PANDAS:\n\n");
        body.append("  ").append(title.getName()).append(" (").append(title.getHumanId()).append(")\n");
        String link = titleLink(title);
        if (link != null) body.append("  ").append(link).append("\n");
        if (ownerHistory.getAgency() != null) {
            body.append("\nAgency: ").append(ownerHistory.getAgency().getName()).append("\n");
        }
        body.append("\n");
        if (isBlank(ownerHistory.getNote())) {
            body.append("No transfer note was given.\n");
        } else {
            body.append("Transfer note:\n\n").append(ownerHistory.getNote().strip()).append("\n");
        }

        var message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(recipient.getEmail());
        if (transferrer != null && !isBlank(transferrer.getEmail())) {
            message.setReplyTo(transferrer.getEmail());
        }
        message.setSubject("PANDAS: " + title.getName() + " has been transferred to you");
        message.setText(body.toString());
        return message;
    }

    private String titleLink(Title title) {
        String base = baseUrl;
        if (isBlank(base)) {
            try {
                base = ServletUriComponentsBuilder.fromCurrentContextPath().toUriString();
            } catch (IllegalStateException e) {
                return null; // not in a request
            }
        }
        return base.replaceAll("/+$", "") + "/titles/" + title.getId();
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
