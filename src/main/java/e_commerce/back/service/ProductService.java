package e_commerce.back.service;


import e_commerce.back.entity.Product;
import e_commerce.back.entity.Variant;
import e_commerce.back.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ProductService {

    @PersistenceContext
    private EntityManager entityManager;
    private final ProductRepository productRepository;


    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<Product> findAllProducts() {
    return productRepository.findAllWithCategory();
    }


    @Transactional
    public Product saveProduct(Product product){
        return  productRepository.save((product));
    }


    @Transactional(readOnly = true)
    public Optional<Product> geyProductById(Long id){
        return productRepository.findById(id);
    }



    @Transactional
    public Optional<Product> updateProduct(Long id, Product productDetails) {
        return productRepository.findById(id)
                .map(existingProduct -> {
                    existingProduct.setName(productDetails.getName());
                    existingProduct.setSlug(productDetails.getSlug());
                    existingProduct.setImageUrl(productDetails.getImageUrl());
                    existingProduct.setCategory(productDetails.getCategory());
                    existingProduct.setAverageRating(productDetails.getAverageRating());
                    existingProduct.setTotalRatings(productDetails.getTotalRatings());

                    Set<Variant> updatedVariants = new HashSet<>(productDetails.getVariants()); // Конвертируем в Set
                    Set<Variant> existingVariants = existingProduct.getVariants();

                    for (Variant variant : updatedVariants) {
                        if (variant.getId() != null) {
                            variant = entityManager.merge(variant);
                        } else {
                            variant.setProduct(existingProduct);
                            existingVariants.add(variant);
                        }
                    }

                    existingProduct.setVariants(existingVariants);

                    return existingProduct;
                });
    }


    @Transactional
    public boolean deleteProduct(Long id){
        return productRepository.findById(id)
                .map(product -> {productRepository.delete(product);
                return true;
                }).orElse(false);
    }



}
