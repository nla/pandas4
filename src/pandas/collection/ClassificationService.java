package pandas.collection;

import org.springframework.stereotype.Service;

import java.util.List;

import static pandas.Utils.sortBy;

@Service
public class ClassificationService {
    private final CollectionRepository collectionRepository;
    private final SubjectRepository subjectRepository;

    public ClassificationService(CollectionRepository collectionRepository, SubjectRepository subjectRepository) {
        this.collectionRepository = collectionRepository;
        this.subjectRepository = subjectRepository;
    }

    public List<Subject> allSubjects() {
        return sortBy(subjectRepository.findAll(), Subject::getFullName);
    }

    public List<Collection> allCollections() {
        return sortBy(collectionRepository.findAll(), Collection::getFullName);
    }
}
