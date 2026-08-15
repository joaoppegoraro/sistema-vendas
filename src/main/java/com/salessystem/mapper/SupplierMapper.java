package com.salessystem.mapper;

import com.salessystem.dto.SupplierRequestDTO;
import com.salessystem.dto.SupplierResponseDTO;
import com.salessystem.entity.Supplier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SupplierMapper {

    public Supplier toEntity(SupplierRequestDTO dto) {
        Supplier supplier = new Supplier();
        applyDto(dto, supplier);
        return supplier;
    }

    public void updateEntityFromDto(SupplierRequestDTO dto, Supplier supplier) {
        applyDto(dto, supplier);
    }

    public SupplierResponseDTO toResponseDto(Supplier supplier) {
        SupplierResponseDTO dto = new SupplierResponseDTO();
        dto.setId(supplier.getId());
        dto.setName(supplier.getName());
        dto.setDocument(supplier.getDocument());
        dto.setFormattedDocument(formatDocument(supplier.getDocument()));
        dto.setEmail(supplier.getEmail());
        dto.setPhone(supplier.getPhone());
        dto.setAddress(supplier.getAddress());
        dto.setWhatsAppUrl(buildWhatsAppUrl(supplier.getPhone()));
        dto.setRegistrationDate(supplier.getRegistrationDate());
        return dto;
    }

    public List<SupplierResponseDTO> toResponseDtoList(List<Supplier> suppliers) {
        return suppliers.stream().map(this::toResponseDto).toList();
    }

    public SupplierRequestDTO toRequestDto(Supplier supplier) {
        SupplierRequestDTO dto = new SupplierRequestDTO();
        dto.setId(supplier.getId());
        dto.setName(supplier.getName());
        dto.setDocument(supplier.getDocument());
        dto.setEmail(supplier.getEmail());
        dto.setPhone(supplier.getPhone());
        dto.setAddress(supplier.getAddress());
        return dto;
    }

    private void applyDto(SupplierRequestDTO dto, Supplier supplier) {
        supplier.setName(dto.getName());
        supplier.setDocument(normalizeDocument(dto.getDocument()));
        supplier.setEmail(dto.getEmail());
        supplier.setPhone(dto.getPhone());
        supplier.setAddress(dto.getAddress());
    }

    private String normalizeDocument(String document) {
        return document == null ? null : document.replaceAll("[^0-9]", "");
    }

    private String formatDocument(String document) {
        if (document == null) {
            return null;
        }
        if (document.length() == 11) {
            return document.substring(0, 3) + "." + document.substring(3, 6) + "." + document.substring(6, 9) + "-" + document.substring(9, 11);
        }
        if (document.length() == 14) {
            return document.substring(0, 2) + "." + document.substring(2, 5) + "." + document.substring(5, 8)
                    + "/" + document.substring(8, 12) + "-" + document.substring(12, 14);
        }
        return document;
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
}
