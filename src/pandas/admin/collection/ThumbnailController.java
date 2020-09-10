package pandas.admin.collection;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import pandas.admin.core.NotFoundException;

import java.util.Date;

@Controller
public class ThumbnailController {
    private final ThumbnailRepository thumbnailRepository;
    private final JobLauncher jobLauncher;
    private final Job thumbnailJob;

    public ThumbnailController(ThumbnailRepository thumbnailRepository, JobLauncher jobLauncher, @Qualifier("thumbnailJob") Job thumbnailJob) {
        this.thumbnailRepository = thumbnailRepository;
        this.jobLauncher = jobLauncher;
        this.thumbnailJob = thumbnailJob;
    }

    @GetMapping("/thumbnails/{id}/edit")
    public String edit(@PathVariable("id") long id, Model model) {
        Thumbnail thumbnail = thumbnailRepository.findById(id).orElseThrow(NotFoundException::new);
        model.addAttribute("thumbnail", thumbnail);
        return "ThumbnailEdit";
    }

    @GetMapping("/thumbnails/generate")
    @ResponseBody
    public String generate() throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        generateThumbnails();
        return "OK";
    }

    @Scheduled(fixedRate = 24 * 60 * 60 * 1000, initialDelay = 0)
    public void generateThumbnails() throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException {
        jobLauncher.run(thumbnailJob, new JobParametersBuilder().addDate("launch", new Date()).toJobParameters());
    }

    @GetMapping("/titles/{titleId}/thumbnail/image")
    public ResponseEntity<byte[]> forTitle(@PathVariable("titleId") long titleId, WebRequest request) {
        Thumbnail thumbnail = thumbnailRepository.findFirstByTitleId(titleId);
        if (thumbnail == null) throw new NotFoundException("titleId = " + titleId);
        if (request.checkNotModified(thumbnail.getLastModifiedDate().toEpochMilli())) {
            return ResponseEntity.status(304).build();
        }
        return ResponseEntity.status(200)
                .cacheControl(CacheControl.noCache())
                .lastModified(thumbnail.getLastModifiedDate())
                .contentType(MediaType.parseMediaType(thumbnail.getContentType()))
                .body(thumbnail.getData());
    }
}
