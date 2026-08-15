package com.salessystem.service;

import com.salessystem.dto.ClientRequestDTO;
import com.salessystem.dto.ClientResponseDTO;
import com.salessystem.entity.Client;
import com.salessystem.exception.ClientDeletionException;
import com.salessystem.exception.DuplicateCpfException;
import com.salessystem.exception.ResourceNotFoundException;
import com.salessystem.mapper.ClientMapper;
import com.salessystem.repository.ClientRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientService {

    private static final int PAGE_SIZE = 10;

    private final ClientRepository repository;
    private final ClientMapper mapper;

    public ClientService(ClientRepository repository, ClientMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public Page<ClientResponseDTO> list(String term, int page) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(Sort.Direction.ASC, "name"));
        Page<Client> result;
        if (term == null || term.isBlank()) {
            result = repository.findAll(pageable);
        } else {
            // A search term with no digits (e.g. a name) would otherwise pass an empty string
            // as the CPF filter, and "cpf LIKE '%%'" matches every non-null CPF — silently
            // turning the OR into an always-true condition and returning every client.
            String digitsOnly = term.replaceAll("[^0-9]", "");
            String cpfFilter = digitsOnly.isEmpty() ? null : digitsOnly;
            result = repository.findByNameContainingIgnoreCaseOrCpfContaining(term, cpfFilter, pageable);
        }
        return result.map(mapper::toResponseDto);
    }

    /** Returns the client as an editable form payload, used to prefill the edit page. */
    public ClientRequestDTO findFormById(Long id) {
        return mapper.toRequestDto(findEntityById(id));
    }

    /** Unpaginated, for the PDV's client picker. */
    public List<ClientResponseDTO> listAllForSelect() {
        return mapper.toResponseDtoList(repository.findAll(Sort.by(Sort.Direction.ASC, "name")));
    }

    public ClientResponseDTO findResponseById(Long id) {
        return mapper.toResponseDto(findEntityById(id));
    }

    public ClientResponseDTO create(ClientRequestDTO dto) {
        validateCpfNotDuplicated(normalizeCpf(dto.getCpf()), null);
        Client client = mapper.toEntity(dto);
        return saveAndHandleDuplicateCpf(client);
    }

    public ClientResponseDTO update(Long id, ClientRequestDTO dto) {
        validateCpfNotDuplicated(normalizeCpf(dto.getCpf()), id);
        Client client = findEntityById(id);
        mapper.updateEntityFromDto(dto, client);
        return saveAndHandleDuplicateCpf(client);
    }

    /**
     * The pre-check in validateCpfNotDuplicated can't see a duplicate inserted by a
     * concurrent request (e.g. a double form submission), so the database's unique
     * constraint on cpf is the real guard; this turns that low-level violation into
     * the same friendly exception the pre-check throws.
     */
    private ClientResponseDTO saveAndHandleDuplicateCpf(Client client) {
        try {
            return mapper.toResponseDto(repository.save(client));
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateCpfException("Já existe um cliente cadastrado com este CPF");
        }
    }

    /**
     * Once Sale.client exists, a client with purchase history can't be deleted at the
     * database level (FK constraint). Same shape as saveAndHandleDuplicateCpf: let the
     * constraint be the real guard and turn its violation into a friendly message.
     */
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Cliente não encontrado (id " + id + ")");
        }
        try {
            repository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new ClientDeletionException("Não é possível excluir um cliente com vendas registradas");
        }
    }

    private Client findEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado (id " + id + ")"));
    }

    private void validateCpfNotDuplicated(String cpf, Long currentId) {
        repository.findByCpf(cpf).ifPresent(existingClient -> {
            boolean belongsToAnotherRecord = currentId == null || !existingClient.getId().equals(currentId);
            if (belongsToAnotherRecord) {
                throw new DuplicateCpfException("Já existe um cliente cadastrado com este CPF");
            }
        });
    }

    private String normalizeCpf(String cpf) {
        return cpf == null ? null : cpf.replaceAll("[^0-9]", "");
    }
}
