package com.salessystem.service;

import com.salessystem.dto.ProductRequestDTO;
import com.salessystem.dto.ProductResponseDTO;
import com.salessystem.dto.ProductVariantRequestDTO;
import com.salessystem.dto.ProductVariantResponseDTO;
import com.salessystem.entity.Product;
import com.salessystem.entity.ProductVariant;
import com.salessystem.entity.Supplier;
import com.salessystem.exception.DuplicateVariantException;
import com.salessystem.exception.ResourceNotFoundException;
import com.salessystem.mapper.ProductMapper;
import com.salessystem.repository.ProductRepository;
import com.salessystem.repository.ProductVariantRepository;
import com.salessystem.repository.SaleItemRepository;
import com.salessystem.repository.SupplierRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Set;

@Service
public class ProductService {

    private static final int PAGE_SIZE = 10;
    private static final Set<String> ALLOWED_PHOTO_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");

    private final ProductRepository repository;
    private final ProductVariantRepository variantRepository;
    private final SaleItemRepository saleItemRepository;
    private final SupplierRepository supplierRepository;
    private final ProductMapper mapper;

    public ProductService(ProductRepository repository,
                           ProductVariantRepository variantRepository,
                           SaleItemRepository saleItemRepository,
                           SupplierRepository supplierRepository,
                           ProductMapper mapper) {
        this.repository = repository;
        this.variantRepository = variantRepository;
        this.saleItemRepository = saleItemRepository;
        this.supplierRepository = supplierRepository;
        this.mapper = mapper;
    }

    /**
     * Reads with variants must stay inside the transaction: Product.variants is LAZY and
     * open-in-view is off, so the Hibernate session that could load it is otherwise already
     * closed by the time the mapper touches the collection.
     */
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> list(String search, String category, boolean lowStockOnly, int page) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(Sort.Direction.ASC, "name"));
        // Building the "%term%" pattern here (instead of via LOWER(CONCAT(...)) in JPQL) avoids a
        // Postgres quirk: with a nullable bind parameter reused inside CONCAT/lower(), its type
        // inference can resolve to bytea instead of text, breaking lower() at runtime.
        String searchPattern = (search == null || search.isBlank()) ? null : "%" + search.toLowerCase() + "%";
        String normalizedCategory = (category == null || category.isBlank()) ? null : category;
        Page<Product> result = repository.search(searchPattern, normalizedCategory, lowStockOnly, pageable);
        return result.map(mapper::toResponseDto);
    }

    public ProductRequestDTO findFormById(Long id) {
        return mapper.toRequestDto(findEntityById(id));
    }

    /** Active variants with stock available to sell, for the PDV's item picker. */
    @Transactional(readOnly = true)
    public List<ProductVariantResponseDTO> listSellableVariants() {
        return variantRepository.findSellable().stream().map(mapper::toVariantResponseDto).toList();
    }

    @Transactional(readOnly = true)
    public ProductResponseDTO findResponseById(Long id) {
        return mapper.toResponseDto(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public ProductPhoto findPhotoById(Long id) {
        Product product = findEntityById(id);
        if (product.getPhotoData() == null) {
            throw new ResourceNotFoundException("Produto não tem foto cadastrada (id " + id + ")");
        }
        return new ProductPhoto(product.getPhotoData(), product.getPhotoContentType());
    }

    public record ProductPhoto(byte[] data, String contentType) {
    }

    @Transactional
    public ProductResponseDTO create(ProductRequestDTO dto, MultipartFile photo) {
        Product product = mapper.toEntity(dto);
        applyPhoto(product, photo);
        product.setSupplier(resolveSupplier(dto.getSupplierId()));

        boolean hasNoVariation = !dto.isVariesBySize() && !dto.isVariesByColor();
        if (hasNoVariation) {
            ProductVariant variant = new ProductVariant();
            variant.setProduct(product);
            variant.setStockQuantity(dto.getInitialStock());
            product.getVariants().add(variant);
        }

        return mapper.toResponseDto(repository.save(product));
    }

    @Transactional
    public ProductResponseDTO update(Long id, ProductRequestDTO dto, MultipartFile photo) {
        Product product = findEntityById(id);
        mapper.updateBasicFields(dto, product);
        product.setSupplier(resolveSupplier(dto.getSupplierId()));

        if (photo != null && !photo.isEmpty()) {
            applyPhoto(product, photo);
        }

        return mapper.toResponseDto(repository.save(product));
    }

    private void applyPhoto(Product product, MultipartFile photo) {
        if (photo == null || photo.isEmpty()) {
            return;
        }
        String contentType = photo.getContentType();
        if (contentType == null || !ALLOWED_PHOTO_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Envie uma imagem em formato JPEG, PNG, WEBP ou GIF");
        }
        try {
            product.setPhotoData(photo.getBytes());
            product.setPhotoContentType(contentType);
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao salvar a foto do produto", e);
        }
    }

    public void activate(Long id) {
        Product product = findEntityById(id);
        product.setActive(true);
        repository.save(product);
    }

    public void deactivate(Long id) {
        Product product = findEntityById(id);
        product.setActive(false);
        repository.save(product);
    }

    public void addVariant(Long productId, ProductVariantRequestDTO dto) {
        Product product = findEntityById(productId);
        String size = normalizeBlank(dto.getSize());
        String color = normalizeBlank(dto.getColor());

        if (product.isVariesBySize() && size == null) {
            throw new DuplicateVariantException("Informe o tamanho desta variação");
        }
        if (product.isVariesByColor() && color == null) {
            throw new DuplicateVariantException("Informe a cor desta variação");
        }
        if (variantRepository.existsByProductIdAndSizeAndColor(productId, size, color)) {
            throw new DuplicateVariantException("Essa combinação de tamanho/cor já existe para este produto");
        }

        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setSize(size);
        variant.setColor(color);
        variant.setStockQuantity(dto.getInitialStock());
        variantRepository.save(variant);
    }

    public void removeVariant(Long productId, Long variantId) {
        ProductVariant variant = findVariantById(variantId);
        if (!variant.getProduct().getId().equals(productId)) {
            throw new ResourceNotFoundException("Variação não encontrada para este produto");
        }
        if (variant.getStockQuantity() > 0 || variant.getReservedQuantity() > 0) {
            throw new DuplicateVariantException("Só é possível remover uma variação sem estoque ou reserva");
        }
        if (saleItemRepository.existsByProductVariantId(variantId)) {
            throw new DuplicateVariantException("Esta variação já tem vendas registradas e não pode ser removida");
        }
        variantRepository.delete(variant);
    }

    /** Products linked to a given supplier, for that supplier's profile page. */
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> listBySupplier(Long supplierId) {
        return repository.findBySupplierIdOrderByName(supplierId).stream()
                .map(mapper::toResponseDto)
                .toList();
    }

    private Supplier resolveSupplier(Long supplierId) {
        if (supplierId == null) {
            return null;
        }
        return supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Fornecedor não encontrado (id " + supplierId + ")"));
    }

    private String normalizeBlank(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private Product findEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado (id " + id + ")"));
    }

    private ProductVariant findVariantById(Long id) {
        return variantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Variação não encontrada (id " + id + ")"));
    }
}
