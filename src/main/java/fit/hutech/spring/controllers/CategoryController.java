package fit.hutech.spring.controllers;

import fit.hutech.spring.entities.Category;
import fit.hutech.spring.services.BookService;
import fit.hutech.spring.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryservice;
    private BookService bookService;
    @GetMapping
    public String viewAllCategory(Model model) {
        List<Category> listCategory = categoryservice.listAll();
        model.addAttribute("categories",listCategory);
        return "category/index";

    }


    @GetMapping("/create")
    public String showNewCategoryPage(Model model) {
        Category category = new Category();
        model.addAttribute("category",category);
        model.addAttribute("books",categoryservice.listAll());
        return "category/create";
    }
    @PostMapping("/save")
    public String saveCategory(@ModelAttribute("category")Category category) {
        categoryservice.save(category);
        return "redirect:/categories";

    }

    @GetMapping("/edit/{id}")
    public String showEditCAtegoryPage(@PathVariable("id")Long id, Model model) {
        Category category = categoryservice.get(id);
        if(category==null) {
            return "notfound";
        } else {
            model.addAttribute("categories", categoryservice.listAll());
            model.addAttribute("category",category);
            return "category/edit";
        }
    }

    @GetMapping("/delete/{id}")
    public String deletecategory(@PathVariable("id")Long id) {
        Category category = categoryservice.get(id);
        if(category==null) {
            return "notfound";
        } else {
            categoryservice.delete(id);
            return "redirect:/categories";
        }
    }
}
