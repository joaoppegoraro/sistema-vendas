package com.salessystem.dto;

import java.time.LocalDate;

/**
 * Read-only representation of a client, returned by the service layer for display.
 */
public class ClientResponseDTO {

    private Long id;
    private String name;
    private String cpf;
    private String formattedCpf;
    private String email;
    private String phone;
    private String address;
    private LocalDate birthDate;
    private String formattedBirthDate;
    private String whatsAppUrl;
    private LocalDate registrationDate;

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

    public String getFormattedCpf() {
        return formattedCpf;
    }

    public void setFormattedCpf(String formattedCpf) {
        this.formattedCpf = formattedCpf;
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

    public String getFormattedBirthDate() {
        return formattedBirthDate;
    }

    public void setFormattedBirthDate(String formattedBirthDate) {
        this.formattedBirthDate = formattedBirthDate;
    }

    public String getWhatsAppUrl() {
        return whatsAppUrl;
    }

    public void setWhatsAppUrl(String whatsAppUrl) {
        this.whatsAppUrl = whatsAppUrl;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }
}
