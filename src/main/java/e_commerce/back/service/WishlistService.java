package e_commerce.back.service;

import e_commerce.back.entity.Product;
import e_commerce.back.entity.User;
import e_commerce.back.repository.ProductRepository;
import e_commerce.back.repository.UserRepository;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class WishlistService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Autowired
    public WishlistService(UserRepository userRepository, ProductRepository productRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }



    public List<Product> getWishlist(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Istifadeçi tapılmadı"));
        return user.getWishlist();
    }



    @Transactional
    public boolean addToWishlist(Long userId, Long productId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("İstifadəçi tapılmadı"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException(("Məhsul tapılmadı")));

        if (user.getWishlist().contains(product)) {
            return false;
        }

        user.addToWishlist(product);
        userRepository.save(user);
        return true;
    }


    @Transactional
    public void removeFromWishlist(Long userId, Long productId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("İstifadəçi tapılmadı"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("Məhsul tapılmadı"));

        user.removeFromWishlist(product);
        userRepository.save(user);
    }





}
