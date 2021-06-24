package pandas.collection;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import pandas.core.NotFoundException;
import pandas.gather.InstanceThumbnail;
import pandas.gather.InstanceThumbnailRepository;

@Controller
public class ThumbnailController {
    private final ThumbnailProcessor thumbnailProcessor;
    private final ThumbnailRepository thumbnailRepository;
    private final InstanceThumbnailRepository instanceThumbnailRepository;

    public ThumbnailController(ThumbnailProcessor thumbnailProcessor, ThumbnailRepository thumbnailRepository, InstanceThumbnailRepository instanceThumbnailRepository) {
        this.thumbnailProcessor = thumbnailProcessor;
        this.thumbnailRepository = thumbnailRepository;
        this.instanceThumbnailRepository = instanceThumbnailRepository;
    }

    @GetMapping("/thumbnails/{id}/edit")
    @PreAuthorize("hasAuthority('PRIV_SYSADMIN')")
    public String edit(@PathVariable("id") long id, Model model) {
        Thumbnail thumbnail = thumbnailRepository.findById(id).orElseThrow(NotFoundException::new);
        model.addAttribute("thumbnail", thumbnail);
        return "ThumbnailEdit";
    }

    @GetMapping("/thumbnails/generate")
    @PreAuthorize("hasAuthority('PRIV_SYSADMIN')")
    @ResponseBody
    public String generate() {
        thumbnailProcessor.run();
        return "OK";
    }

    @GetMapping("/titles/{titleId}/thumbnail/image")
    public ResponseEntity<byte[]> forTitle(@PathVariable("titleId") long titleId, WebRequest request) {
        var instanceThumbnails = instanceThumbnailRepository.findForTitleId(titleId, PageRequest.of(0, 1));
        if (!instanceThumbnails.isEmpty()) {
            InstanceThumbnail thumbnail = instanceThumbnails.get(0);
            return ResponseEntity.status(200)
                    .cacheControl(CacheControl.noCache())
                    .lastModified(thumbnail.getLastModifiedDate())
                    .contentType(MediaType.parseMediaType(thumbnail.getContentType()))
                    .body(thumbnail.getData());
        }
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
