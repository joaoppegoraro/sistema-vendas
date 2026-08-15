package com.salessystem.controller;

import com.salessystem.dto.CartItemDTO;
import com.salessystem.dto.SaleRequestDTO;
import com.salessystem.exception.InsufficientStockException;
import com.salessystem.exception.InvalidSaleStateException;
import com.salessystem.exception.ResourceNotFoundException;
import com.salessystem.exception.StockConflictException;
import com.salessystem.entity.PaymentMethod;
import com.salessystem.mapper.SaleMapper;
import com.salessystem.service.ClientService;
import com.salessystem.service.ProductService;
import com.salessystem.service.SaleService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/sales")
public class SaleController {

    private static final String CART_SESSION_KEY = "cart";

    private final SaleService saleService;
    private final ProductService productService;
    private final ClientService clientService;
    private final SaleMapper saleMapper;

    public SaleController(SaleService saleService, ProductService productService,
                           ClientService clientService, SaleMapper saleMapper) {
        this.saleService = saleService;
        this.productService = productService;
        this.clientService = clientService;
        this.saleMapper = saleMapper;
    }

    @GetMapping("/new")
    public String newSale(HttpSession session, Model model) {
        model.addAttribute("clients", clientService.listAllForSelect());
        model.addAttribute("sellableVariants", productService.listSellableVariants());
        model.addAttribute("paymentMethods", PaymentMethod.values());
        addCartAttributes(model, cart(session));
        return "sales/new";
    }

    @PostMapping("/cart/items")
    public String addCartItem(@RequestParam Long variantId,
                               @RequestParam(defaultValue = "1") int quantity,
                               HttpSession session,
                               Model model) {
        List<CartItemDTO> cart = cart(session);
        try {
            CartItemDTO existing = cart.stream().filter(i -> i.getVariantId().equals(variantId)).findFirst().orElse(null);
            int totalQuantity = quantity + (existing == null ? 0 : existing.getQuantity());
            CartItemDTO item = saleService.buildCartItem(variantId, totalQuantity);
            if (existing != null) {
                cart.remove(existing);
            }
            cart.add(item);
        } catch (InsufficientStockException | ResourceNotFoundException e) {
            model.addAttribute("cartError", e.getMessage());
        }
        addCartAttributes(model, cart);
        return "sales/new :: cartSection";
    }

    @PostMapping("/cart/items/{variantId}/remove")
    public String removeCartItem(@PathVariable Long variantId, HttpSession session, Model model) {
        List<CartItemDTO> cart = cart(session);
        cart.removeIf(item -> item.getVariantId().equals(variantId));
        addCartAttributes(model, cart);
        return "sales/new :: cartSection";
    }

    private void addCartAttributes(Model model, List<CartItemDTO> cart) {
        BigDecimal subtotal = cart.stream().map(CartItemDTO::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("cart", cart);
        model.addAttribute("cartSubtotal", subtotal);
        model.addAttribute("cartSubtotalFormatted", saleMapper.formatCurrency(subtotal));
    }

    @PostMapping
    public String finalizeSale(@Valid @ModelAttribute("sale") SaleRequestDTO sale,
                                BindingResult result,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Selecione uma forma de pagamento válida");
            return "redirect:/sales/new";
        }
        try {
            var response = saleService.finalizeSale(sale, cart(session));
            session.removeAttribute(CART_SESSION_KEY);
            redirectAttributes.addFlashAttribute("success", "Venda registrada com sucesso!");
            return "redirect:/sales/" + response.getId();
        } catch (InvalidSaleStateException | InsufficientStockException | StockConflictException | ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/sales/new";
        }
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
                        Model model) {
        var salePage = saleService.list(page, date, month);
        model.addAttribute("salePage", salePage);
        model.addAttribute("sales", salePage.getContent());
        model.addAttribute("date", date);
        model.addAttribute("month", month);
        return "sales/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("sale", saleService.findById(id));
        return "sales/detail";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            saleService.cancel(id);
            redirectAttributes.addFlashAttribute("success", "Venda cancelada e estoque restaurado!");
        } catch (InvalidSaleStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/sales/" + id;
    }

    @SuppressWarnings("unchecked")
    private List<CartItemDTO> cart(HttpSession session) {
        List<CartItemDTO> cart = (List<CartItemDTO>) session.getAttribute(CART_SESSION_KEY);
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute(CART_SESSION_KEY, cart);
        }
        return cart;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleNotFound(ResourceNotFoundException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        return "redirect:/sales";
    }
}
