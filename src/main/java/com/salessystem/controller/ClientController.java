package com.salessystem.controller;

import com.salessystem.dto.ClientRequestDTO;
import com.salessystem.dto.ClientResponseDTO;
import com.salessystem.exception.ClientDeletionException;
import com.salessystem.exception.DuplicateCpfException;
import com.salessystem.exception.ResourceNotFoundException;
import com.salessystem.service.ClientService;
import com.salessystem.service.SaleService;
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
@RequestMapping("/clients")
public class ClientController {

    private final ClientService service;
    private final SaleService saleService;

    public ClientController(ClientService service, SaleService saleService) {
        this.service = service;
        this.saleService = saleService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String search,
                        @RequestParam(defaultValue = "0") int page,
                        Model model) {
        Page<ClientResponseDTO> clientPage = service.list(search, page);
        model.addAttribute("clientPage", clientPage);
        model.addAttribute("clients", clientPage.getContent());
        model.addAttribute("search", search);
        return "clients/list";
    }

    @GetMapping("/{id}")
    public String profile(@PathVariable Long id,
                           @RequestParam(defaultValue = "0") int page,
                           Model model) {
        model.addAttribute("client", service.findResponseById(id));
        var purchasePage = saleService.findByClient(id, page);
        model.addAttribute("purchasePage", purchasePage);
        model.addAttribute("purchases", purchasePage.getContent());
        return "clients/profile";
    }

    @GetMapping("/new")
    public String newClient(Model model) {
        model.addAttribute("client", new ClientRequestDTO());
        return "clients/form";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("client", service.findFormById(id));
        return "clients/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("client") ClientRequestDTO client,
                          BindingResult result,
                          RedirectAttributes redirectAttributes,
                          Model model) {
        if (result.hasErrors()) {
            return "clients/form";
        }
        try {
            service.create(client);
            redirectAttributes.addFlashAttribute("success", "Cliente cadastrado com sucesso!");
            return "redirect:/clients";
        } catch (DuplicateCpfException e) {
            model.addAttribute("error", e.getMessage());
            return "clients/form";
        }
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                          @Valid @ModelAttribute("client") ClientRequestDTO client,
                          BindingResult result,
                          RedirectAttributes redirectAttributes,
                          Model model) {
        if (result.hasErrors()) {
            return "clients/form";
        }
        try {
            service.update(id, client);
            redirectAttributes.addFlashAttribute("success", "Cliente atualizado com sucesso!");
            return "redirect:/clients";
        } catch (DuplicateCpfException e) {
            model.addAttribute("error", e.getMessage());
            return "clients/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            service.delete(id);
            redirectAttributes.addFlashAttribute("success", "Cliente removido com sucesso!");
        } catch (ResourceNotFoundException | ClientDeletionException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/clients";
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleNotFound(ResourceNotFoundException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        return "redirect:/clients";
    }
}
