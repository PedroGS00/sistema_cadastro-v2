package com.sistema.cadastro.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome Ã© obrigatÃ³rio")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Email Ã© obrigatÃ³rio")
    @Email(message = "Email deve ser vÃ¡lido")
    @Size(max = 100, message = "Email deve ter no mÃ¡ximo 100 caracteres")
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @NotBlank(message = "CPF Ã© obrigatÃ³rio")
    @Pattern(regexp = "^\\d{11}$", message = "CPF deve conter apenas nÃºmeros e ter 11 dÃ­gitos")
    @Column(name = "cpf", nullable = false, unique = true, length = 11)
    private String cpf;

    @NotBlank(message = "CEP Ã© obrigatÃ³rio")
    @Pattern(regexp = "^\\d{8}$", message = "CEP deve conter apenas nÃºmeros e ter 8 dÃ­gitos")
    @Column(name = "cep", nullable = false, length = 8)
    private String cep;

    @Size(max = 200, message = "EndereÃ§o deve ter no mÃ¡ximo 200 caracteres")
    @Column(name = "address", length = 200)
    private String address;

    @Size(max = 100, message = "Cidade deve ter no mÃ¡ximo 100 caracteres")
    @Column(name = "city", length = 100)
    private String city;

    @Size(max = 2, message = "Estado deve ter 2 caracteres")
    @Column(name = "state", length = 2)
    private String state;

    @Size(max = 100, message = "Bairro deve ter no mÃ¡ximo 100 caracteres")
    @Column(name = "neighborhood", length = 100)
    private String neighborhood;

    @Size(max = 50, message = "Complemento deve ter no mÃ¡ximo 50 caracteres")
    @Column(name = "complement", length = 50)
    private String complement;

    @Size(max = 10, message = "NÃºmero deve ter no mÃ¡ximo 10 caracteres")
    @Column(name = "number", length = 10)
    private String number;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void validate() {
        if (cpf != null && !isValidCpf(cpf)) {
            throw new IllegalArgumentException("CPF invÃ¡lido");
        }
    }

    private boolean isValidCpf(String cpf) {

        cpf = cpf.replaceAll("\\D", "");

        if (cpf.length() != 11) {
            return false;
        }

        boolean allDigitsEqual = true;
        for (int i = 1; i < cpf.length(); i++) {
            if (cpf.charAt(i) != cpf.charAt(0)) {
                allDigitsEqual = false;
                break;
            }
        }
        if (allDigitsEqual) {
            return false;
        }

        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += (cpf.charAt(i) - '0') * (10 - i);
        }
        int firstDigit = 11 - (sum % 11);
        if (firstDigit >= 10) {
            firstDigit = 0;
        }

        sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += (cpf.charAt(i) - '0') * (11 - i);
        }
        int secondDigit = 11 - (sum % 11);
        if (secondDigit >= 10) {
            secondDigit = 0;
        }

        return cpf.charAt(9) - '0' == firstDigit && cpf.charAt(10) - '0' == secondDigit;
    }

    public Long getId() {
        return this.id;
    }

}
