package pandas.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/categories")
    public String list() {
        return "redirect:/categories/0";
    }

    @GetMapping("/categories/{id}")
    public String get(@PathVariable("id") long id, Model model) {
        Category category = categoryService.getCategory(id);
        model.addAttribute("category", category);
        model.addAttribute("breadcrumbs", categoryService.breadcrumbs(category));
        return "CategoryView";
    }

    @GetMapping("/categories/{id}/edit")
    public String edit(@PathVariable("id") long id, Model model) {
        Category category = categoryService.getCategory(id);
        model.addAttribute("category", category);
        return "CategoryEdit";
    }

    @PostMapping("/categories/{id}/edit")
    public String update(@PathVariable long id, @RequestParam("name") String name,
                       @RequestParam("description") String description) {
        Category category = categoryService.getCategory(id);
        category.setName(name);
        category.setDescription(description);
        categoryService.save(category);
        return "redirect:/categories/" + id;
    }

    @GetMapping("/categories/new")
    public String newForm(@RequestParam("type") String type,
                          @RequestParam("parentId") long parentId,
                          Model model) {
        Category category = type.equals("Subject") ? new Subject() : new Collection();
        model.addAttribute("category", category);
        model.addAttribute("parentId", parentId);
        return "CategoryEdit";
    }
}
