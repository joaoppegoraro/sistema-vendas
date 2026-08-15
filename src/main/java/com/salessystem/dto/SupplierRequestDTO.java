package com.salessystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Payload used for both creating and editing a supplier via the web form.
 * The id is only populated when prefilling the edit form; it is ignored on create.
 */
public class SupplierRequestDTO {

    private Long id;

    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 3, max = 120, message = "O nome deve ter entre 3 e 120 caracteres")
    private String name;

    @NotBlank(message = "O CPF ou CNPJ é obrigatório")
    private String document;

    @Email(message = "Informe um e-mail válido")
    private String email;

    @Pattern(regexp = "^$|^\\(?\\d{2}\\)?[\\s-]?\\d{4,5}-?\\d{4}$", message = "Informe um telefone válido, ex: (54) 99999-0000")
    private String phone;

    @Size(max = 200, message = "O endereço deve ter no máximo 200 caracteres")
    private String address;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
