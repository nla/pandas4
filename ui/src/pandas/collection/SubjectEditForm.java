package pandas.collection;

import org.hibernate.engine.jdbc.BlobProxy;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public record SubjectEditForm(Subject parent, String name, String description, MultipartFile icon, boolean removeIcon) {
    public static SubjectEditForm from(Subject subject) {
        return new SubjectEditForm(subject.getParent(), subject.getName(), subject.getDescription(), null, false);
    }

    public void applyTo(Subject subject) throws IOException {
        subject.setParent(parent);
        subject.setName(name);
        subject.setDescription(description);
        if (icon != null && !icon.isEmpty()) {
            subject.setIcon(BlobProxy.generateProxy(icon.getInputStream(), icon.getSize()));
        } else if (removeIcon) {
            subject.setIcon(null);
        }
    }
}
