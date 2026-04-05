package com.supplysync.product.service;

import com.supplysync.product.dto.ProductRequest;
import com.supplysync.product.dto.ProductResponse;
import com.supplysync.product.entity.Product;
import com.supplysync.product.exception.InvalidRequestException;
import com.supplysync.product.exception.ProductNotFoundException;
import com.supplysync.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public ProductResponse createProduct(ProductRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new InvalidRequestException("Product name is required");
        }
        if (request.getSku() == null || request.getSku().isBlank()) {
            throw new InvalidRequestException("SKU is required");
        }
        if (request.getPrice() == null || request.getPrice().isBlank()) {
            throw new InvalidRequestException("Price is required");
        }

        productRepository.findBySku(request.getSku())
                .ifPresent(existing -> {
                    throw new InvalidRequestException("Product with SKU '" + request.getSku() + "' already exists");
                });

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .sku(request.getSku())
                .price(new BigDecimal(request.getPrice()))
                .category(request.getCategory())
                .build();

        productRepository.save(product);
        return mapToResponse(product);
    }

    public ProductResponse getProductById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product with id '" + id + "' not found"));
        return mapToResponse(product);
    }

    public Page<ProductResponse> getAllProducts(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        PageRequest pageRequest = PageRequest.of(page, size, sort);

        return productRepository.findAll(pageRequest)
                .map(this::mapToResponse);
    }

    public ProductResponse updateProduct(UUID id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product with id '" + id + "' not found"));

        if (request.getName() != null && !request.getName().isBlank()) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getSku() != null && !request.getSku().isBlank()) {
            product.setSku(request.getSku());
        }
        if (request.getPrice() != null && !request.getPrice().isBlank()) {
            product.setPrice(new BigDecimal(request.getPrice()));
        }
        if (request.getCategory() != null) {
            product.setCategory(request.getCategory());
        }

        productRepository.save(product);
        return mapToResponse(product);
    }

    public void deleteProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product with id '" + id + "' not found"));
        productRepository.delete(product);
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .sku(product.getSku())
                .price(product.getPrice().toString())
                .category(product.getCategory())
                .build();
    }
}
