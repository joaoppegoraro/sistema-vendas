package com.salessystem.repository;

import com.salessystem.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByCpf(String cpf);

    Page<Client> findByNameContainingIgnoreCaseOrCpfContaining(String name, String cpf, Pageable pageable);
}
