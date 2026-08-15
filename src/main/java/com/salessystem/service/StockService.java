package com.salessystem.service;

import com.salessystem.entity.ProductVariant;
import com.salessystem.exception.InsufficientStockException;
import com.salessystem.exception.ResourceNotFoundException;
import com.salessystem.exception.StockConflictException;
import com.salessystem.repository.ProductVariantRepository;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Service
public class StockService {

    private final ProductVariantRepository variantRepository;

    public StockService(ProductVariantRepository variantRepository) {
        this.variantRepository = variantRepository;
    }

    public void increaseStock(Long variantId, int amount) {
        ProductVariant variant = findVariant(variantId);
        variant.setStockQuantity(variant.getStockQuantity() + amount);
        saveWithConflictGuard(variant);
    }

    public void decreaseStock(Long variantId, int amount) {
        ProductVariant variant = findVariant(variantId);
        int newQuantity = variant.getStockQuantity() - amount;
        if (newQuantity < variant.getReservedQuantity()) {
            throw new InsufficientStockException("Não é possível reduzir o estoque abaixo da quantidade reservada");
        }
        if (newQuantity < 0) {
            throw new InsufficientStockException("Não há estoque suficiente para essa baixa");
        }
        variant.setStockQuantity(newQuantity);
        saveWithConflictGuard(variant);
    }

    public void reserve(Long variantId, int amount) {
        ProductVariant variant = findVariant(variantId);
        int newReserved = variant.getReservedQuantity() + amount;
        if (newReserved > variant.getStockQuantity()) {
            throw new InsufficientStockException("Não há peças disponíveis suficientes para reservar");
        }
        variant.setReservedQuantity(newReserved);
        saveWithConflictGuard(variant);
    }

    public void release(Long variantId, int amount) {
        ProductVariant variant = findVariant(variantId);
        int newReserved = variant.getReservedQuantity() - amount;
        if (newReserved < 0) {
            throw new InsufficientStockException("Não há reserva suficiente para liberar essa quantidade");
        }
        variant.setReservedQuantity(newReserved);
        saveWithConflictGuard(variant);
    }

    /**
     * Two near-simultaneous requests against the same variant (double-click, two tabs,
     * PDV racing a quick-adjust) can both read the same version before either commits.
     * saveAndFlush forces the optimistic-lock check to happen here, synchronously, so the
     * loser gets a friendly message instead of an unhandled 500 — same defensive shape as
     * ClientService.saveAndHandleDuplicateCpf, just guarding a counter instead of uniqueness.
     */
    private void saveWithConflictGuard(ProductVariant variant) {
        try {
            variantRepository.saveAndFlush(variant);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new StockConflictException("Este item foi alterado em outra operação, atualize a página e tente novamente");
        }
    }

    private ProductVariant findVariant(Long id) {
        return variantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Variação não encontrada (id " + id + ")"));
    }
}
