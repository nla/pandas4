package pandas.admin.collection;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import pandas.admin.core.NotFoundException;

@Controller
public class ThumbnailController {
    private final ThumbnailRepository thumbnailRepository;

    public ThumbnailController(ThumbnailRepository thumbnailRepository) {
        this.thumbnailRepository = thumbnailRepository;
    }

    @GetMapping("/thumbnails/{id}/edit")
    public String edit(@PathVariable("id") long id, Model model) {
        Thumbnail thumbnail = thumbnailRepository.findById(id).orElseThrow(NotFoundException::new);
        model.addAttribute("thumbnail", thumbnail);
        return "ThumbnailEdit";
    }
}
