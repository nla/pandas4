package pandas.admin.collection;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class CollectionController {
    private final CategoryService categoryService;

    public CollectionController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/collections")
    public String list() {
        return "redirect:/collections/0";
    }

    @GetMapping("/collections/{id}")
    public String get(@PathVariable("id") long id, Model model) {
        Category category = categoryService.getCategory(id);
        model.addAttribute("category", category);
        model.addAttribute("breadcrumbs", categoryService.breadcrumbs(category));
        return "CollectionView";
    }

    @GetMapping("/collections/{id}/edit")
    public String edit(@PathVariable("id") long id, Model model) {
        Category category = categoryService.getCategory(id);
        model.addAttribute("category", category);
        return "CollectionEdit";
    }

    @PostMapping("/collections/{id}/edit")
    public String update(@PathVariable long id, @RequestParam("name") String name,
                       @RequestParam("description") String description) {
        Category category = categoryService.getCategory(id);
        category.setName(name);
        category.setDescription(description);
        categoryService.save(category);
        return "redirect:/collections/" + id;
    }

    @PostMapping("/collections/{id}/delete")
    public String delete(@PathVariable long id) {
        categoryService.delete(id);
        return "redirect:/collections";
    }

    @GetMapping("/collections/new")
    public String newForm(@RequestParam("type") String type,
                          @RequestParam("parentId") long parentId,
                          Model model) {
        Category category = type.equals("Subject") ? new Subject() : new Collection();
        model.addAttribute("category", category);
        model.addAttribute("parentId", parentId);
        return "CollectionEdit";
    }

    @PostMapping("/collections/new")
    public String create(@RequestParam("type") String type,
                         @RequestParam("parentId") long parentId,
                         @RequestParam("name") String name,
                         @RequestParam("description") String description) {
        Category parent = categoryService.getCategory(parentId);
        Category category = type.equals("Subject") ? new Subject() : new Collection();
        category.setParentCategory(parent);
        category.setName(name);
        category.setDescription(description);
        categoryService.save(category);
        return "redirect:/collections/" + category.getCategoryId();
    }

    @GetMapping("/collections/search")
    public String newForm(@RequestParam("q") String q, Model model) {
        model.addAttribute("results", categoryService.search(q));
        model.addAttribute("q", q);
        return "CollectionSearch";
    }

    @GetMapping("/collections/reindex")
    @ResponseBody
    public String reindex() throws InterruptedException {
        categoryService.reindex();
        return "OK";
    }
}
