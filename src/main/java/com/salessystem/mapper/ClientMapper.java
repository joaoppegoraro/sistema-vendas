package com.salessystem.mapper;

import com.salessystem.dto.ClientRequestDTO;
import com.salessystem.dto.ClientResponseDTO;
import com.salessystem.entity.Client;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class ClientMapper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public Client toEntity(ClientRequestDTO dto) {
        Client client = new Client();
        applyDto(dto, client);
        return client;
    }

    public void updateEntityFromDto(ClientRequestDTO dto, Client client) {
        applyDto(dto, client);
    }

    public ClientResponseDTO toResponseDto(Client client) {
        ClientResponseDTO dto = new ClientResponseDTO();
        dto.setId(client.getId());
        dto.setName(client.getName());
        dto.setCpf(client.getCpf());
        dto.setFormattedCpf(formatCpf(client.getCpf()));
        dto.setEmail(client.getEmail());
        dto.setPhone(client.getPhone());
        dto.setAddress(client.getAddress());
        dto.setBirthDate(client.getBirthDate());
        dto.setFormattedBirthDate(client.getBirthDate() == null ? null : client.getBirthDate().format(DATE_FORMATTER));
        dto.setWhatsAppUrl(buildWhatsAppUrl(client.getPhone()));
        dto.setRegistrationDate(client.getRegistrationDate());
        return dto;
    }

    private String buildWhatsAppUrl(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }
        String digits = phone.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return null;
        }
        return "https://wa.me/55" + digits;
    }

    public List<ClientResponseDTO> toResponseDtoList(List<Client> clients) {
        return clients.stream().map(this::toResponseDto).toList();
    }

    public ClientRequestDTO toRequestDto(Client client) {
        ClientRequestDTO dto = new ClientRequestDTO();
        dto.setId(client.getId());
        dto.setName(client.getName());
        dto.setCpf(client.getCpf());
        dto.setEmail(client.getEmail());
        dto.setPhone(client.getPhone());
        dto.setAddress(client.getAddress());
        dto.setBirthDate(client.getBirthDate());
        return dto;
    }

    private void applyDto(ClientRequestDTO dto, Client client) {
        client.setName(dto.getName());
        client.setCpf(normalizeCpf(dto.getCpf()));
        client.setEmail(dto.getEmail());
        client.setPhone(dto.getPhone());
        client.setAddress(dto.getAddress());
        client.setBirthDate(dto.getBirthDate());
    }

    private String normalizeCpf(String cpf) {
        return cpf == null ? null : cpf.replaceAll("[^0-9]", "");
    }

    private String formatCpf(String cpf) {
        if (cpf == null || cpf.length() != 11) {
            return cpf;
        }
        return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-" + cpf.substring(9, 11);
    }
}
