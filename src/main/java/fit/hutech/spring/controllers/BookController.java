package fit.hutech.spring.controllers;

import fit.hutech.spring.entities.Book;
import fit.hutech.spring.daos.Item;
import fit.hutech.spring.services.BookService;
import fit.hutech.spring.services.CartService;
import fit.hutech.spring.services.CategoryService;
import fit.hutech.spring.utils.FileUploadUtil;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;

    private final CategoryService categoryService;

    private final CartService cartService;
    private static final int PAGE_SIZE = 10;

    @GetMapping
    public String showAllBooks(
            @RequestParam(defaultValue = "0") Integer pageNo,
            Model model
    ) {
        int pageSize = 10;
        String sortBy = "id";
        List<Book> books = bookService.getAllBooks(pageNo, pageSize, sortBy);
        long totalBooks = bookService.getAllBooksCount();
        long totalPages = (totalBooks + pageSize - 1) / pageSize;

        // Tính toán giới hạn trang trên trang hiện tại
        int startBookIndex = pageNo * pageSize + 1;
        int endBookIndex = Math.min(startBookIndex + pageSize - 1, (int) totalBooks);

        model.addAttribute("books", books);
        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startBookIndex", startBookIndex);
        model.addAttribute("endBookIndex", endBookIndex);
        model.addAttribute("categories", categoryService.getAllCategories());

        return "book/list";
    }

    @GetMapping("/new")
    public String showNewBookPage(Model model) {
        Book book = new Book();
        model.addAttribute("book", book);
        model.addAttribute("categories", categoryService.listAll());
        return "book/create";
    }

    @GetMapping("/add")
    public String addBookForm(@NotNull Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "book/add";
    }

    @PostMapping("/add")
    public String addBook(
            @Valid @ModelAttribute("book") Book book,
            @NotNull BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            var errors = bindingResult.getAllErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toArray(String[]::new);
            model.addAttribute("errors", errors);
            model.addAttribute("categories", categoryService.getAllCategories());
            return "book/add";
        }
        bookService.addBook(book);
        return "redirect:/books";
    }

    @PostMapping("/save")
    public String saveBook(@ModelAttribute("book") Book book, @RequestParam("image") MultipartFile multipartFile) throws IOException {
        String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
        book.setPhotourl(fileName);
        Book saveBook = bookService.save(book);
        if (!multipartFile.getOriginalFilename().isBlank()) {
            String uploadDir = "photos/" + saveBook.getId();
            FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);
        }
        return "redirect:/books";
    }

//    @PostMapping("/add")
//    public String addBook(
//            @Valid @ModelAttribute("book") Book book,
//            @NotNull BindingResult bindingResult,
//            @RequestParam("image") MultipartFile multipartFile,
//            Model model) throws IOException {
//
//        if (bindingResult.hasErrors()) {
//            var errors = bindingResult.getAllErrors()
//                    .stream()
//                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
//                    .toArray(String[]::new);
//            model.addAttribute("errors", errors);
//            model.addAttribute("categories", categoryService.getAllCategories());
//            return "book/add";
//        }
//
//        String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
//        book.setPhotourl(fileName);
//        Book saveBook = bookService.save(book);
//        if (!multipartFile.getOriginalFilename().isBlank()) {
//            String uploadDir = "photos/" + saveBook.getId();
//            FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);
//        }
//
//        return "redirect:/books";
//    }


    @GetMapping("/edit/{id}")
    public String editBookForm(@NotNull Model model, @PathVariable long id) {
        var book = bookService.getBookById(id);
        model.addAttribute("book", book.orElseThrow(() -> new IllegalArgumentException("Book not found")));
        model.addAttribute("categories", categoryService.getAllCategories());
        return "book/edit";
    }

    @PostMapping("/edit")
    public String editBook(@Valid @ModelAttribute("book") Book book,
                           @NotNull BindingResult bindingResult,
                           @NotNull Model model) {
        if (bindingResult.hasErrors()) {
            var errors = bindingResult.getAllErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toArray(String[]::new);
            model.addAttribute("errors", errors);
            model.addAttribute("categories", categoryService.getAllCategories());
            return "book/edit";
        }
        bookService.updateBook(book);
        return "redirect:/books";
    }

    @GetMapping("/delete/{id}")
    public String deleteBook(@PathVariable long id) {
        bookService.getBookById(id)
                .ifPresentOrElse(
                        book -> bookService.deleteBookById(id),
                        () -> { throw new IllegalArgumentException("Book not found"); }
                );
        return "redirect:/books";
    }



    @GetMapping("/search")
    public String searchBook(
            @NotNull Model model,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") Integer pageNo,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(defaultValue = "id") String sortBy) {
        model.addAttribute("books", bookService.searchBook(keyword));
        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPages",
                bookService
                        .getAllBooks(pageNo, pageSize, sortBy).size() / pageSize);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "book/list";
    }

    @PostMapping("/add-to-cart")
    public String addToCart(HttpSession session,
                            @RequestParam long id,
                            @RequestParam String name,
                            @RequestParam double price,
                            @RequestParam(defaultValue = "1") int quantity) {
        var cart = cartService.getCart(session);
        cart.addItems(new Item(id, name, price, quantity));
        cartService.updateCart(session, cart);
        return "redirect:/books";
    }
}
