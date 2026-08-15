package com.salessystem.controller;

import com.salessystem.dto.SupplierRequestDTO;
import com.salessystem.dto.SupplierResponseDTO;
import com.salessystem.exception.DuplicateSupplierDocumentException;
import com.salessystem.exception.ResourceNotFoundException;
import com.salessystem.exception.SupplierDeletionException;
import com.salessystem.service.ProductService;
import com.salessystem.service.SupplierService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/suppliers")
public class SupplierController {

    private final SupplierService service;
    private final ProductService productService;

    public SupplierController(SupplierService service, ProductService productService) {
        this.service = service;
        this.productService = productService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String search,
                        @RequestParam(defaultValue = "0") int page,
                        Model model) {
        Page<SupplierResponseDTO> supplierPage = service.list(search, page);
        model.addAttribute("supplierPage", supplierPage);
        model.addAttribute("suppliers", supplierPage.getContent());
        model.addAttribute("search", search);
        return "suppliers/list";
    }

    @GetMapping("/{id}")
    public String profile(@PathVariable Long id, Model model) {
        model.addAttribute("supplier", service.findResponseById(id));
        model.addAttribute("products", productService.listBySupplier(id));
        return "suppliers/profile";
    }

    @GetMapping("/new")
    public String newSupplier(Model model) {
        model.addAttribute("supplier", new SupplierRequestDTO());
        return "suppliers/form";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("supplier", service.findFormById(id));
        return "suppliers/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("supplier") SupplierRequestDTO supplier,
                          BindingResult result,
                          RedirectAttributes redirectAttributes,
                          Model model) {
        if (result.hasErrors()) {
            return "suppliers/form";
        }
        try {
            service.create(supplier);
            redirectAttributes.addFlashAttribute("success", "Fornecedor cadastrado com sucesso!");
            return "redirect:/suppliers";
        } catch (DuplicateSupplierDocumentException e) {
            model.addAttribute("error", e.getMessage());
            return "suppliers/form";
        }
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                          @Valid @ModelAttribute("supplier") SupplierRequestDTO supplier,
                          BindingResult result,
                          RedirectAttributes redirectAttributes,
                          Model model) {
        if (result.hasErrors()) {
            return "suppliers/form";
        }
        try {
            service.update(id, supplier);
            redirectAttributes.addFlashAttribute("success", "Fornecedor atualizado com sucesso!");
            return "redirect:/suppliers";
        } catch (DuplicateSupplierDocumentException e) {
            model.addAttribute("error", e.getMessage());
            return "suppliers/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            service.delete(id);
            redirectAttributes.addFlashAttribute("success", "Fornecedor removido com sucesso!");
        } catch (ResourceNotFoundException | SupplierDeletionException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/suppliers";
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleNotFound(ResourceNotFoundException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        return "redirect:/suppliers";
    }
}
