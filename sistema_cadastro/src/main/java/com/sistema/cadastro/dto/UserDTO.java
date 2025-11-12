package com.sistema.cadastro.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {

    private Long id;

    @NotBlank(message = "Nome Ã© obrigatÃ³rio")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    private String name;

    @NotBlank(message = "Email Ã© obrigatÃ³rio")
    @Email(message = "Email deve ser vÃ¡lido")
    @Size(max = 100, message = "Email deve ter no mÃ¡ximo 100 caracteres")
    private String email;

    @NotBlank(message = "CPF Ã© obrigatÃ³rio")
    @Pattern(regexp = "^\\d{11}$", message = "CPF deve conter apenas nÃºmeros e ter 11 dÃ­gitos")
    private String cpf;

    @NotBlank(message = "CEP Ã© obrigatÃ³rio")
    @Pattern(regexp = "^\\d{8}$", message = "CEP deve conter apenas nÃºmeros e ter 8 dÃ­gitos")
    private String cep;

    private String address;
    private String city;
    private String state;
    private String neighborhood;
    private String complement;
    private String number;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getCpf() { return cpf; }
    public String getCep() { return cep; }
    public String getAddress() { return address; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getNeighborhood() { return neighborhood; }
    public String getComplement() { return complement; }
    public String getNumber() { return number; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
