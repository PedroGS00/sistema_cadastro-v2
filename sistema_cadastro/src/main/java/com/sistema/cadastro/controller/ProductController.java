package com.sistema.cadastro.controller;

import com.sistema.cadastro.dto.ProductDTO;
import com.sistema.cadastro.entity.Product;
import com.sistema.cadastro.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Produtos", description = "Endpoints para gerenciamento de produtos")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar novo produto", description = "Cria um novo produto com validaÃ§Ã£o de SKU, preÃ§o e estoque")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Produto criado com sucesso",
                    content = @Content(schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "400", description = "Dados invÃ¡lidos"),
            @ApiResponse(responseCode = "409", description = "SKU jÃ¡ cadastrado")
    })
    public ResponseEntity<Product> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        Product createdProduct = productService.createProduct(productDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar produto por ID", description = "Retorna os dados de um produto especÃ­fico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produto encontrado",
                    content = @Content(schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "404", description = "Produto nÃ£o encontrado")
    })
    public ResponseEntity<Product> getProductById(
            @Parameter(description = "ID do produto", required = true)
            @PathVariable Long id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/sku/{sku}")
    @Operation(summary = "Buscar produto por SKU", description = "Retorna os dados de um produto especÃ­fico pelo SKU")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produto encontrado",
                    content = @Content(schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "404", description = "Produto nÃ£o encontrado")
    })
    public ResponseEntity<Product> getProductBySku(
            @Parameter(description = "SKU do produto", required = true)
            @PathVariable String sku) {
        Product product = productService.getProductBySku(sku);
        return ResponseEntity.ok(product);
    }

    @GetMapping
    @Operation(summary = "Listar produtos", description = "Retorna uma lista paginada de produtos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de produtos recuperada com sucesso",
                    content = @Content(schema = @Schema(implementation = Page.class)))
    })
    public ResponseEntity<Page<Product>> getAllProducts(
            @Parameter(description = "ParÃ¢metros de paginaÃ§Ã£o")
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Product> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/active")
    @Operation(summary = "Listar produtos ativos", description = "Retorna uma lista paginada de produtos ativos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de produtos recuperada com sucesso",
                    content = @Content(schema = @Schema(implementation = Page.class)))
    })
    public ResponseEntity<Page<Product>> getActiveProducts(
            @Parameter(description = "ParÃ¢metros de paginaÃ§Ã£o")
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Product> products = productService.getActiveProducts(pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar produtos com filtros", description = "Retorna uma lista paginada de produtos com base em filtros avanÃ§ados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de produtos recuperada com sucesso",
                    content = @Content(schema = @Schema(implementation = Page.class)))
    })
    public ResponseEntity<Page<Product>> getProductsByFilters(
            @Parameter(description = "Nome do produto (parcial)")
            @RequestParam(required = false) String name,
            @Parameter(description = "SKU do produto (parcial)")
            @RequestParam(required = false) String sku,
            @Parameter(description = "Categoria do produto")
            @RequestParam(required = false) String category,
            @Parameter(description = "Marca do produto")
            @RequestParam(required = false) String brand,
            @Parameter(description = "PreÃ§o mÃ­nimo")
            @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "PreÃ§o mÃ¡ximo")
            @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "Estoque mÃ­nimo")
            @RequestParam(required = false) Integer minStock,
            @Parameter(description = "Estoque mÃ¡ximo")
            @RequestParam(required = false) Integer maxStock,
            @Parameter(description = "Status ativo")
            @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "ParÃ¢metros de paginaÃ§Ã£o")
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Product> products = productService.getProductsByFilters(
                name, sku, category, brand, minPrice, maxPrice, minStock, maxStock, isActive, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Buscar produtos por categoria", description = "Retorna uma lista paginada de produtos de uma categoria especÃ­fica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de produtos recuperada com sucesso",
                    content = @Content(schema = @Schema(implementation = Page.class)))
    })
    public ResponseEntity<Page<Product>> getProductsByCategory(
            @Parameter(description = "Categoria do produto", required = true)
            @PathVariable String category,
            @Parameter(description = "ParÃ¢metros de paginaÃ§Ã£o")
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Product> products = productService.getProductsByCategory(category, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/brand/{brand}")
    @Operation(summary = "Buscar produtos por marca", description = "Retorna uma lista paginada de produtos de uma marca especÃ­fica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de produtos recuperada com sucesso",
                    content = @Content(schema = @Schema(implementation = Page.class)))
    })
    public ResponseEntity<Page<Product>> getProductsByBrand(
            @Parameter(description = "Marca do produto", required = true)
            @PathVariable String brand,
            @Parameter(description = "ParÃ¢metros de paginaÃ§Ã£o")
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Product> products = productService.getProductsByBrand(brand, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Buscar produtos com baixo estoque", description = "Retorna produtos com estoque abaixo do limite (padrÃ£o: 10 unidades)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de produtos recuperada com sucesso",
                    content = @Content(schema = @Schema(implementation = Page.class)))
    })
    public ResponseEntity<Page<Product>> getLowStockProducts(
            @Parameter(description = "Limite de estoque (padrÃ£o: 10)")
            @RequestParam(defaultValue = "10") Integer threshold,
            @Parameter(description = "ParÃ¢metros de paginaÃ§Ã£o")
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Product> products = productService.getLowStockProducts(threshold, pageable);
        return ResponseEntity.ok(products);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar produto", description = "Atualiza os dados de um produto existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "400", description = "Dados invÃ¡lidos"),
            @ApiResponse(responseCode = "404", description = "Produto nÃ£o encontrado"),
            @ApiResponse(responseCode = "409", description = "SKU jÃ¡ cadastrado para outro produto")
    })
    public ResponseEntity<Product> updateProduct(
            @Parameter(description = "ID do produto", required = true)
            @PathVariable Long id,
            @Valid @RequestBody ProductDTO productDTO) {
        Product updatedProduct = productService.updateProduct(id, productDTO);
        return ResponseEntity.ok(updatedProduct);
    }

    @PatchMapping("/{id}/stock")
    @Operation(summary = "Atualizar estoque do produto", description = "Atualiza o estoque de um produto especÃ­fico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estoque atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "400", description = "Quantidade invÃ¡lida"),
            @ApiResponse(responseCode = "404", description = "Produto nÃ£o encontrado")
    })
    public ResponseEntity<Product> updateStock(
            @Parameter(description = "ID do produto", required = true)
            @PathVariable Long id,
            @Parameter(description = "Quantidade a adicionar (positivo) ou remover (negativo)", required = true)
            @RequestParam Integer quantity) {
        Product updatedProduct = productService.updateStock(id, quantity);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deletar produto", description = "Remove um produto do sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Produto deletado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Produto nÃ£o encontrado")
    })
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "ID do produto", required = true)
            @PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats/active")
    @Operation(summary = "EstatÃ­sticas - Produtos ativos", description = "Retorna a quantidade de produtos ativos")
    @ApiResponse(responseCode = "200", description = "EstatÃ­stica recuperada com sucesso")
    public ResponseEntity<Long> countActiveProducts() {
        long count = productService.countActiveProducts();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/stats/inactive")
    @Operation(summary = "EstatÃ­sticas - Produtos inativos", description = "Retorna a quantidade de produtos inativos")
    @ApiResponse(responseCode = "200", description = "EstatÃ­stica recuperada com sucesso")
    public ResponseEntity<Long> countInactiveProducts() {
        long count = productService.countInactiveProducts();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/stats/total-stock")
    @Operation(summary = "EstatÃ­sticas - Estoque total", description = "Retorna a quantidade total de produtos em estoque")
    @ApiResponse(responseCode = "200", description = "EstatÃ­stica recuperada com sucesso")
    public ResponseEntity<Long> getTotalStock() {
        long totalStock = productService.getTotalStock();
        return ResponseEntity.ok(totalStock);
    }

    @GetMapping("/stats/category/{category}")
    @Operation(summary = "EstatÃ­sticas - Produtos por categoria", description = "Retorna a quantidade de produtos em uma categoria especÃ­fica")
    @ApiResponse(responseCode = "200", description = "EstatÃ­stica recuperada com sucesso")
    public ResponseEntity<Long> countProductsByCategory(
            @Parameter(description = "Categoria do produto", required = true)
            @PathVariable String category) {
        long count = productService.countProductsByCategory(category);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/stats/brand/{brand}")
    @Operation(summary = "EstatÃ­sticas - Produtos por marca", description = "Retorna a quantidade de produtos de uma marca especÃ­fica")
    @ApiResponse(responseCode = "200", description = "EstatÃ­stica recuperada com sucesso")
    public ResponseEntity<Long> countProductsByBrand(
            @Parameter(description = "Marca do produto", required = true)
            @PathVariable String brand) {
        long count = productService.countProductsByBrand(brand);
        return ResponseEntity.ok(count);
    }
}
