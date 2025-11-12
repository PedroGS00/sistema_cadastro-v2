package com.sistema.cadastro.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@EntityListeners(AuditingEntityListener.class)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome do produto Ã© obrigatÃ³rio")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Size(max = 500, message = "DescriÃ§Ã£o deve ter no mÃ¡ximo 500 caracteres")
    @Column(name = "description", length = 500)
    private String description;

    @NotBlank(message = "SKU Ã© obrigatÃ³rio")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "SKU deve conter apenas letras maiÃºsculas, nÃºmeros e hÃ­fens")
    @Size(min = 3, max = 50, message = "SKU deve ter entre 3 e 50 caracteres")
    @Column(name = "sku", nullable = false, unique = true, length = 50)
    private String sku;

    @NotNull(message = "PreÃ§o Ã© obrigatÃ³rio")
    @DecimalMin(value = "0.01", message = "PreÃ§o deve ser maior que zero")
    @Digits(integer = 10, fraction = 2, message = "PreÃ§o deve ter no mÃ¡ximo 10 dÃ­gitos inteiros e 2 decimais")
    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @NotNull(message = "Estoque Ã© obrigatÃ³rio")
    @Min(value = 0, message = "Estoque nÃ£o pode ser negativo")
    @Max(value = 999999, message = "Estoque deve ter no mÃ¡ximo 6 dÃ­gitos")
    @Column(name = "stock", nullable = false)
    private Integer stock;

    @DecimalMin(value = "0.01", message = "Peso deve ser maior que zero")
    @Digits(integer = 8, fraction = 3, message = "Peso deve ter no mÃ¡ximo 8 dÃ­gitos inteiros e 3 decimais")
    @Column(name = "weight", precision = 11, scale = 3)
    private BigDecimal weight;

    @Size(max = 50, message = "Categoria deve ter no mÃ¡ximo 50 caracteres")
    @Column(name = "category", length = 50)
    private String category;

    @Size(max = 50, message = "Marca deve ter no mÃ¡ximo 50 caracteres")
    @Column(name = "brand", length = 50)
    private String brand;

    @Size(max = 255, message = "URL da imagem deve ter no mÃ¡ximo 255 caracteres")
    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void validate() {
        if (price != null && price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("PreÃ§o deve ser maior que zero");
        }
        
        if (stock != null && stock < 0) {
            throw new IllegalArgumentException("Estoque nÃ£o pode ser negativo");
        }
        
        if (weight != null && weight.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Peso deve ser maior que zero");
        }
    }

    public void decreaseStock(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }
        
        if (this.stock < quantity) {
            throw new IllegalStateException("Estoque insuficiente. DisponÃ­vel: " + this.stock + ", Solicitado: " + quantity);
        }
        
        this.stock -= quantity;
    }

    public void increaseStock(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }
        
        this.stock += quantity;
    }

    public boolean isInStock() {
        return this.stock > 0 && this.isActive;
    }

    public boolean hasLowStock() {
        return this.stock <= 10 && this.stock > 0;
    }
}
