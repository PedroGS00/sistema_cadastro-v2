package com.sistema.cadastro.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductDTO {

    private Long id;

    @NotBlank(message = "Nome do produto Ã© obrigatÃ³rio")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    private String name;

    @Size(max = 500, message = "DescriÃ§Ã£o deve ter no mÃ¡ximo 500 caracteres")
    private String description;

    @NotBlank(message = "SKU Ã© obrigatÃ³rio")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "SKU deve conter apenas letras maiÃºsculas, nÃºmeros e hÃ­fens")
    @Size(min = 3, max = 50, message = "SKU deve ter entre 3 e 50 caracteres")
    private String sku;

    @NotNull(message = "PreÃ§o Ã© obrigatÃ³rio")
    @DecimalMin(value = "0.01", message = "PreÃ§o deve ser maior que zero")
    @Digits(integer = 10, fraction = 2, message = "PreÃ§o deve ter no mÃ¡ximo 10 dÃ­gitos inteiros e 2 decimais")
    private BigDecimal price;

    @NotNull(message = "Estoque Ã© obrigatÃ³rio")
    @Min(value = 0, message = "Estoque nÃ£o pode ser negativo")
    @Max(value = 999999, message = "Estoque deve ter no mÃ¡ximo 6 dÃ­gitos")
    private Integer stock;

    @DecimalMin(value = "0.01", message = "Peso deve ser maior que zero")
    @Digits(integer = 8, fraction = 3, message = "Peso deve ter no mÃ¡ximo 8 dÃ­gitos inteiros e 3 decimais")
    private BigDecimal weight;

    @Size(max = 50, message = "Categoria deve ter no mÃ¡ximo 50 caracteres")
    private String category;

    @Size(max = 50, message = "Marca deve ter no mÃ¡ximo 50 caracteres")
    private String brand;

    @Size(max = 255, message = "URL da imagem deve ter no mÃ¡ximo 255 caracteres")
    private String imageUrl;

    @Builder.Default
    private Boolean isActive = true;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getSku() { return sku; }
    public BigDecimal getPrice() { return price; }
    public Integer getStock() { return stock; }
    public BigDecimal getWeight() { return weight; }
    public String getCategory() { return category; }
    public String getBrand() { return brand; }
    public String getImageUrl() { return imageUrl; }
    public Boolean getIsActive() { return isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
