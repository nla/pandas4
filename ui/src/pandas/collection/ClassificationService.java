package pandas.collection;

import org.springframework.stereotype.Service;

import java.util.List;

import static pandas.core.Utils.sortBy;

@Service
public class ClassificationService {
    private final SubjectRepository subjectRepository;

    public ClassificationService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    public List<Subject> allSubjects() {
        return sortBy(subjectRepository.findAll(), Subject::getFullName);
    }
}
