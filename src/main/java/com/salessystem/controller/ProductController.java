package com.salessystem.controller;

import com.salessystem.dto.ProductRequestDTO;
import com.salessystem.dto.ProductResponseDTO;
import com.salessystem.dto.ProductVariantRequestDTO;
import com.salessystem.exception.DuplicateVariantException;
import com.salessystem.exception.InsufficientStockException;
import com.salessystem.exception.ResourceNotFoundException;
import com.salessystem.exception.StockConflictException;
import com.salessystem.service.CategoryService;
import com.salessystem.service.ProductService;
import com.salessystem.service.StockService;
import com.salessystem.service.SupplierService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService service;
    private final StockService stockService;
    private final CategoryService categoryService;
    private final SupplierService supplierService;
    private final Validator validator;

    public ProductController(ProductService service, StockService stockService,
                              CategoryService categoryService, SupplierService supplierService,
                              Validator validator) {
        this.service = service;
        this.stockService = stockService;
        this.categoryService = categoryService;
        this.supplierService = supplierService;
        this.validator = validator;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String search,
                        @RequestParam(required = false) String category,
                        @RequestParam(defaultValue = "false") boolean lowStockOnly,
                        @RequestParam(defaultValue = "0") int page,
                        Model model) {
        Page<ProductResponseDTO> productPage = service.list(search, category, lowStockOnly, page);
        model.addAttribute("productPage", productPage);
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("search", search);
        model.addAttribute("category", category);
        model.addAttribute("lowStockOnly", lowStockOnly);
        model.addAttribute("categories", categoryService.listAllNames());
        model.addAttribute("suppliers", supplierService.listAllForSelect());
        return "products/list";
    }

    @GetMapping("/new")
    public String newProduct(Model model) {
        model.addAttribute("product", new ProductRequestDTO());
        model.addAttribute("categories", categoryService.listAllNames());
        model.addAttribute("suppliers", supplierService.listAllForSelect());
        return "products/form";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("product", service.findFormById(id));
        model.addAttribute("categories", categoryService.listAllNames());
        model.addAttribute("suppliers", supplierService.listAllForSelect());
        return "products/form";
    }

    @GetMapping("/{id}/photo")
    public ResponseEntity<byte[]> photo(@PathVariable Long id) {
        ProductService.ProductPhoto photo = service.findPhotoById(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(photo.contentType()))
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
                .body(photo.data());
    }

    @PostMapping
    public String create(@ModelAttribute("product") ProductRequestDTO product,
                          @RequestParam(required = false) MultipartFile photo,
                          @RequestParam(required = false) String newCategoryName,
                          RedirectAttributes redirectAttributes,
                          Model model) {
        resolveNewCategory(product, newCategoryName);
        BindingResult result = validate(product);
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.listAllNames());
        model.addAttribute("suppliers", supplierService.listAllForSelect());
            model.addAttribute(BindingResult.MODEL_KEY_PREFIX + "product", result);
            return "products/form";
        }
        service.create(product, photo);
        redirectAttributes.addFlashAttribute("success", "Produto cadastrado com sucesso!");
        return "redirect:/products";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                          @ModelAttribute("product") ProductRequestDTO product,
                          @RequestParam(required = false) MultipartFile photo,
                          @RequestParam(required = false) String newCategoryName,
                          RedirectAttributes redirectAttributes,
                          Model model) {
        resolveNewCategory(product, newCategoryName);
        BindingResult result = validate(product);
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.listAllNames());
        model.addAttribute("suppliers", supplierService.listAllForSelect());
            model.addAttribute(BindingResult.MODEL_KEY_PREFIX + "product", result);
            return "products/form";
        }
        service.update(id, product, photo);
        redirectAttributes.addFlashAttribute("success", "Produto atualizado com sucesso!");
        return "redirect:/products";
    }

    /**
     * category's @NotBlank must be validated AFTER the "new category" text field (if filled)
     * has been resolved into it — a plain @Valid on the method parameter would validate the
     * raw select value first and reject an empty select even when a new category was typed.
     */
    private void resolveNewCategory(ProductRequestDTO product, String newCategoryName) {
        if (newCategoryName != null && !newCategoryName.isBlank()) {
            product.setCategory(categoryService.getOrCreateName(newCategoryName));
        }
    }

    private BindingResult validate(ProductRequestDTO product) {
        DataBinder binder = new DataBinder(product, "product");
        binder.setValidator(validator);
        binder.validate();
        return binder.getBindingResult();
    }

    @PostMapping("/{id}/activate")
    public String activate(@PathVariable Long id, @RequestParam(required = false) String redirect,
                            RedirectAttributes redirectAttributes) {
        service.activate(id);
        redirectAttributes.addFlashAttribute("success", "Produto reativado!");
        return redirectTo(redirect, "/products");
    }

    @PostMapping("/{id}/deactivate")
    public String deactivate(@PathVariable Long id, @RequestParam(required = false) String redirect,
                              RedirectAttributes redirectAttributes) {
        service.deactivate(id);
        redirectAttributes.addFlashAttribute("success", "Produto desativado!");
        return redirectTo(redirect, "/products");
    }

    @GetMapping("/{id}/variants")
    public String variants(@PathVariable Long id, Model model) {
        model.addAttribute("product", service.findResponseById(id));
        model.addAttribute("variant", new ProductVariantRequestDTO());
        return "products/variants";
    }

    @PostMapping("/{id}/variants")
    public String addVariant(@PathVariable Long id,
                              @Valid @ModelAttribute("variant") ProductVariantRequestDTO variant,
                              BindingResult result,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        if (result.hasErrors()) {
            model.addAttribute("product", service.findResponseById(id));
            return "products/variants";
        }
        try {
            service.addVariant(id, variant);
            redirectAttributes.addFlashAttribute("success", "Variação adicionada!");
        } catch (DuplicateVariantException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/products/" + id + "/variants";
    }

    @PostMapping("/{id}/variants/{variantId}/delete")
    public String removeVariant(@PathVariable Long id, @PathVariable Long variantId,
                                 RedirectAttributes redirectAttributes) {
        try {
            service.removeVariant(id, variantId);
            redirectAttributes.addFlashAttribute("success", "Variação removida!");
        } catch (DuplicateVariantException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/products/" + id + "/variants";
    }

    @PostMapping("/{id}/variants/{variantId}/stock/increase")
    public String increaseStock(@PathVariable Long id, @PathVariable Long variantId,
                                 @RequestParam(required = false) String redirect,
                                 RedirectAttributes redirectAttributes) {
        stockService.increaseStock(variantId, 1);
        return redirectTo(redirect, "/products/" + id + "/variants");
    }

    @PostMapping("/{id}/variants/{variantId}/stock/decrease")
    public String decreaseStock(@PathVariable Long id, @PathVariable Long variantId,
                                 @RequestParam(required = false) String redirect,
                                 RedirectAttributes redirectAttributes) {
        try {
            stockService.decreaseStock(variantId, 1);
        } catch (InsufficientStockException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return redirectTo(redirect, "/products/" + id + "/variants");
    }

    @PostMapping("/{id}/variants/{variantId}/reserve")
    public String reserve(@PathVariable Long id, @PathVariable Long variantId,
                           @RequestParam(required = false) String redirect,
                           RedirectAttributes redirectAttributes) {
        try {
            stockService.reserve(variantId, 1);
        } catch (InsufficientStockException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return redirectTo(redirect, "/products/" + id + "/variants");
    }

    @PostMapping("/{id}/variants/{variantId}/release")
    public String release(@PathVariable Long id, @PathVariable Long variantId,
                           @RequestParam(required = false) String redirect,
                           RedirectAttributes redirectAttributes) {
        try {
            stockService.release(variantId, 1);
        } catch (InsufficientStockException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return redirectTo(redirect, "/products/" + id + "/variants");
    }

    private String redirectTo(String redirect, String fallback) {
        return "redirect:" + ((redirect == null || redirect.isBlank()) ? fallback : redirect);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleNotFound(ResourceNotFoundException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        return "redirect:/products";
    }

    @ExceptionHandler(StockConflictException.class)
    public String handleStockConflict(StockConflictException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        return "redirect:/products";
    }
}
