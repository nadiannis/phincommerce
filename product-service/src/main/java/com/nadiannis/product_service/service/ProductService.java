package com.nadiannis.product_service.service;

import com.nadiannis.product_service.dto.ProductReqDto;
import com.nadiannis.common.dto.product.ProductResDto;
import com.nadiannis.common.dto.product.QuantityUpdateReqDto;
import com.nadiannis.product_service.entity.Product;
import com.nadiannis.common.exception.ResourceInsufficientException;
import com.nadiannis.common.exception.ResourceNotFoundException;
import com.nadiannis.product_service.repository.ProductRepository;
import com.nadiannis.common.utils.QuantityUpdateAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductService {

    private ProductRepository repository;

    @Autowired
    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public Flux<ProductResDto> getAll() {
        return repository.findAll().map(product -> mapToResDto(product));
    }

    public Mono<ProductResDto> add(ProductReqDto productReqDto) {
        Product product = mapToEntity(productReqDto);
        return repository.save(product).map(newProduct -> mapToResDto(newProduct));
    }

    public Mono<ProductResDto> getById(Long id) {
        Mono<Product> productMono = repository
                .findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("product", "id", Long.toString(id))));

        return productMono.map(product -> mapToResDto(product));
    }

    public Mono<ProductResDto> update(Long id, ProductReqDto productReqDto) {
        Mono<Product> productMono = repository
                .findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("product", "id", Long.toString(id))));

        return productMono
                .flatMap(product -> {
                    product.setName(productReqDto.getName());
                    product.setPrice(productReqDto.getPrice());
                    product.setCategory(productReqDto.getCategory());
                    product.setStockQuantity(productReqDto.getStockQuantity());
                    if (productReqDto.getDescription() != null && !productReqDto.getDescription().equals("")) {
                        product.setDescription(productReqDto.getDescription());
                    }
                    if (productReqDto.getImageUrl() != null && !productReqDto.getImageUrl().equals("")) {
                        product.setImageUrl(productReqDto.getImageUrl());
                    }
                    return repository.save(product);
                })
                .map(product -> mapToResDto(product));
    }

    public Mono<ProductResDto> updateQuantity(Long id, QuantityUpdateReqDto quantityUpdateReqDto) {
        Mono<Product> productMono = repository
                .findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("product", "id", Long.toString(id))));

        return productMono
                .flatMap(product -> {
                    if (quantityUpdateReqDto.getAction().toUpperCase().equals(QuantityUpdateAction.DEDUCT.toString())) {
                        if (product.getStockQuantity() >= quantityUpdateReqDto.getStockQuantity()) {
                            product.setStockQuantity(product.getStockQuantity() - quantityUpdateReqDto.getStockQuantity());
                        } else {
                            return Mono.error(new ResourceInsufficientException("product", "quantity"));
                        }
                    } else if (quantityUpdateReqDto.getAction().toUpperCase().equals(QuantityUpdateAction.ADD.toString())) {
                        product.setStockQuantity(product.getStockQuantity() + quantityUpdateReqDto.getStockQuantity());
                    }
                    return repository.save(product);
                })
                .map(product -> mapToResDto(product));
    }

    public Mono<Void> delete(Long id) {
        Mono<Product> productMono = repository
                .findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("product", "id", Long.toString(id))));

        return productMono.flatMap(product -> repository.deleteById(id));
    }

    private ProductResDto mapToResDto(Product product) {
        ProductResDto productResDto = new ProductResDto();
        productResDto.setId(product.getId());
        productResDto.setName(product.getName());
        productResDto.setPrice(product.getPrice());
        productResDto.setCategory(product.getCategory());
        productResDto.setStockQuantity(product.getStockQuantity());
        productResDto.setDescription(product.getDescription());
        productResDto.setImageUrl(product.getImageUrl());
        productResDto.setCreatedAt(product.getCreatedAt());
        productResDto.setUpdatedAt(product.getUpdatedAt());
        return productResDto;
    }

    private Product mapToEntity(ProductReqDto productReqDto) {
        Product product = new Product();
        product.setName(productReqDto.getName());
        product.setPrice(productReqDto.getPrice());
        product.setCategory(productReqDto.getCategory());
        product.setStockQuantity(productReqDto.getStockQuantity());
        product.setDescription(productReqDto.getDescription());
        product.setImageUrl(productReqDto.getImageUrl());
        return product;
    }

}
