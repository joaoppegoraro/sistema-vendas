package com.salessystem.dto;

import com.salessystem.validation.CPF;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Payload used for both creating and editing a client via the web form.
 * The id is only populated when prefilling the edit form; it is ignored on create.
 */
public class ClientRequestDTO {

    private Long id;

    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 3, max = 120, message = "O nome deve ter entre 3 e 120 caracteres")
    private String name;

    @NotBlank(message = "O CPF é obrigatório")
    @CPF(message = "Informe um CPF válido")
    private String cpf;

    @Email(message = "Informe um e-mail válido")
    private String email;

    @Pattern(regexp = "^$|^\\(?\\d{2}\\)?[\\s-]?\\d{4,5}-?\\d{4}$", message = "Informe um telefone válido, ex: (54) 99999-0000")
    private String phone;

    @Size(max = 200, message = "O endereço deve ter no máximo 200 caracteres")
    private String address;

    @PastOrPresent(message = "A data de nascimento não pode ser no futuro")
    private LocalDate birthDate;

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

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
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

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }
}
