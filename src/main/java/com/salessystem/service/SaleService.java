package com.salessystem.service;

import com.salessystem.dto.CartItemDTO;
import com.salessystem.dto.SaleRequestDTO;
import com.salessystem.dto.SaleResponseDTO;
import com.salessystem.entity.Client;
import com.salessystem.entity.ProductVariant;
import com.salessystem.entity.Sale;
import com.salessystem.entity.SaleItem;
import com.salessystem.entity.SaleStatus;
import com.salessystem.exception.InsufficientStockException;
import com.salessystem.exception.InvalidSaleStateException;
import com.salessystem.exception.ResourceNotFoundException;
import com.salessystem.mapper.SaleMapper;
import com.salessystem.repository.ClientRepository;
import com.salessystem.repository.ProductVariantRepository;
import com.salessystem.repository.SaleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
public class SaleService {

    private static final int PAGE_SIZE = 10;

    private final SaleRepository repository;
    private final ClientRepository clientRepository;
    private final ProductVariantRepository variantRepository;
    private final StockService stockService;
    private final SaleMapper mapper;

    public SaleService(SaleRepository repository,
                        ClientRepository clientRepository,
                        ProductVariantRepository variantRepository,
                        StockService stockService,
                        SaleMapper mapper) {
        this.repository = repository;
        this.clientRepository = clientRepository;
        this.variantRepository = variantRepository;
        this.stockService = stockService;
        this.mapper = mapper;
    }

    /** Looks up a variant and snapshots its current price/cost into a cart line, validating available stock. */
    @Transactional(readOnly = true)
    public CartItemDTO buildCartItem(Long variantId, int quantity) {
        if (quantity <= 0) {
            throw new InsufficientStockException("Informe uma quantidade válida");
        }
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variação não encontrada (id " + variantId + ")"));
        if (quantity > variant.getAvailableQuantity()) {
            throw new InsufficientStockException("Não há estoque disponível suficiente para essa quantidade");
        }

        CartItemDTO item = new CartItemDTO();
        item.setVariantId(variant.getId());
        item.setProductName(variant.getProduct().getName());
        item.setVariantLabel(buildVariantLabel(variant.getSize(), variant.getColor()));
        item.setQuantity(quantity);
        item.setUnitPrice(variant.getProduct().getSalePrice());
        item.setUnitCost(variant.getProduct().getCost());
        return item;
    }

    /**
     * Re-validates each line's stock against the live database (the session cart can be
     * stale) and decrements it via StockService, so the same negative-stock guard and
     * optimistic-lock race protection used by the quick-adjust buttons applies here too.
     * Any failure rolls back the whole sale — no partial stock decrements.
     */
    @Transactional
    public SaleResponseDTO finalizeSale(SaleRequestDTO dto, List<CartItemDTO> cart) {
        if (cart == null || cart.isEmpty()) {
            throw new InvalidSaleStateException("Adicione ao menos um item à venda antes de finalizar");
        }

        Sale sale = new Sale();
        if (dto.getClientId() != null) {
            sale.setClient(findClient(dto.getClientId()));
        }
        sale.setPaymentMethod(dto.getPaymentMethod());
        sale.setDiscount(dto.getDiscount() == null ? BigDecimal.ZERO : dto.getDiscount());
        sale.setSurcharge(dto.getSurcharge() == null ? BigDecimal.ZERO : dto.getSurcharge());

        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItemDTO cartItem : cart) {
            stockService.decreaseStock(cartItem.getVariantId(), cartItem.getQuantity());

            SaleItem item = new SaleItem();
            item.setSale(sale);
            item.setProductVariant(variantRepository.getReferenceById(cartItem.getVariantId()));
            item.setQuantity(cartItem.getQuantity());
            item.setUnitPrice(cartItem.getUnitPrice());
            item.setUnitCost(cartItem.getUnitCost());
            sale.getItems().add(item);

            subtotal = subtotal.add(cartItem.getSubtotal());
        }

        BigDecimal total = subtotal.subtract(sale.getDiscount()).add(sale.getSurcharge());
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidSaleStateException("O desconto não pode deixar o total da venda negativo");
        }
        sale.setTotal(total);

        return mapper.toResponseDto(repository.save(sale));
    }

    /** Restores every line's stock and marks the sale cancelled, in one transaction. */
    @Transactional
    public void cancel(Long id) {
        Sale sale = findEntityById(id);
        if (sale.getStatus() == SaleStatus.CANCELLED) {
            throw new InvalidSaleStateException("Esta venda já está cancelada");
        }
        for (SaleItem item : sale.getItems()) {
            stockService.increaseStock(item.getProductVariant().getId(), item.getQuantity());
        }
        sale.setStatus(SaleStatus.CANCELLED);
        repository.save(sale);
    }

    /** date takes priority over month when both are provided; either narrows the list to that range. */
    @Transactional(readOnly = true)
    public Page<SaleResponseDTO> list(int page, LocalDate date, YearMonth month) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(Sort.Direction.DESC, "saleDate"));
        Page<Sale> result;
        if (date != null) {
            result = repository.findBySaleDateBetweenOrderBySaleDateDesc(date.atStartOfDay(), date.plusDays(1).atStartOfDay(), pageable);
        } else if (month != null) {
            LocalDateTime start = month.atDay(1).atStartOfDay();
            result = repository.findBySaleDateBetweenOrderBySaleDateDesc(start, month.plusMonths(1).atDay(1).atStartOfDay(), pageable);
        } else {
            result = repository.findAllByOrderBySaleDateDesc(pageable);
        }
        return result.map(mapper::toResponseDto);
    }

    @Transactional(readOnly = true)
    public Page<SaleResponseDTO> findByClient(Long clientId, int page) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(Sort.Direction.DESC, "saleDate"));
        return repository.findByClientIdOrderBySaleDateDesc(clientId, pageable).map(mapper::toResponseDto);
    }

    @Transactional(readOnly = true)
    public SaleResponseDTO findById(Long id) {
        return mapper.toResponseDto(findEntityById(id));
    }

    private Client findClient(Long clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado (id " + clientId + ")"));
    }

    private Sale findEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venda não encontrada (id " + id + ")"));
    }

    private String buildVariantLabel(String size, String color) {
        boolean hasSize = size != null && !size.isBlank();
        boolean hasColor = color != null && !color.isBlank();
        if (hasSize && hasColor) {
            return size + " - " + color;
        }
        if (hasSize) {
            return size;
        }
        if (hasColor) {
            return color;
        }
        return "Único";
    }
}
