package com.salessystem.service;

import com.salessystem.dto.SupplierRequestDTO;
import com.salessystem.dto.SupplierResponseDTO;
import com.salessystem.entity.Supplier;
import com.salessystem.exception.DuplicateSupplierDocumentException;
import com.salessystem.exception.ResourceNotFoundException;
import com.salessystem.exception.SupplierDeletionException;
import com.salessystem.mapper.SupplierMapper;
import com.salessystem.repository.SupplierRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupplierService {

    private static final int PAGE_SIZE = 10;

    private final SupplierRepository repository;
    private final SupplierMapper mapper;

    public SupplierService(SupplierRepository repository, SupplierMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public Page<SupplierResponseDTO> list(String term, int page) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(Sort.Direction.ASC, "name"));
        Page<Supplier> result;
        if (term == null || term.isBlank()) {
            result = repository.findAll(pageable);
        } else {
            // A search term with no digits would otherwise pass an empty string as the document
            // filter, and "document LIKE '%%'" matches every non-null document — same class of
            // bug already fixed in ClientService.list, avoided here from the start.
            String digitsOnly = term.replaceAll("[^0-9]", "");
            String documentFilter = digitsOnly.isEmpty() ? null : digitsOnly;
            result = repository.findByNameContainingIgnoreCaseOrDocumentContaining(term, documentFilter, pageable);
        }
        return result.map(mapper::toResponseDto);
    }

    /** Unpaginated, for the product form's supplier picker. */
    public List<SupplierResponseDTO> listAllForSelect() {
        return mapper.toResponseDtoList(repository.findAll(Sort.by(Sort.Direction.ASC, "name")));
    }

    /** Returns the supplier as an editable form payload, used to prefill the edit page. */
    public SupplierRequestDTO findFormById(Long id) {
        return mapper.toRequestDto(findEntityById(id));
    }

    public SupplierResponseDTO findResponseById(Long id) {
        return mapper.toResponseDto(findEntityById(id));
    }

    public SupplierResponseDTO create(SupplierRequestDTO dto) {
        return saveAndHandleDuplicateDocument(mapper.toEntity(dto));
    }

    public SupplierResponseDTO update(Long id, SupplierRequestDTO dto) {
        Supplier supplier = findEntityById(id);
        mapper.updateEntityFromDto(dto, supplier);
        return saveAndHandleDuplicateDocument(supplier);
    }

    /** Same shape as ClientService.saveAndHandleDuplicateCpf: the DB unique constraint is the real guard. */
    private SupplierResponseDTO saveAndHandleDuplicateDocument(Supplier supplier) {
        try {
            return mapper.toResponseDto(repository.save(supplier));
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateSupplierDocumentException("Já existe um fornecedor cadastrado com este CPF/CNPJ");
        }
    }

    /** Once Product.supplier exists, a supplier with linked products can't be deleted (FK constraint). */
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Fornecedor não encontrado (id " + id + ")");
        }
        try {
            repository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new SupplierDeletionException("Não é possível excluir um fornecedor com produtos vinculados");
        }
    }

    private Supplier findEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fornecedor não encontrado (id " + id + ")"));
    }
}
