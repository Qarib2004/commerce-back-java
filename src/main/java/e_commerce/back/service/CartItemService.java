package e_commerce.back.service;

import e_commerce.back.entity.CartItem;
import e_commerce.back.entity.Product;
import e_commerce.back.entity.User;
import e_commerce.back.entity.Variant;
import e_commerce.back.exception.InsufficientStockException;
import e_commerce.back.exception.ResourceNotFoundException;
import e_commerce.back.repository.CartItemRepository;
import e_commerce.back.repository.ProductRepository;
import e_commerce.back.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CartItemService {


    private final CartItemRepository cartItemRepository;

    private final ProductRepository productRepository;

    private UserRepository userRepository;

    @Autowired
    public CartItemService(CartItemRepository cartItemRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }


    public List<CartItem> getUserCartItems(Long userId){
        return  cartItemRepository.findByUserId(userId);
    }



    @Transactional
    public CartItem addToCart(Long userId, Long productId, String variantSku, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Optional<Variant> optionalVariant = product.getVariants().stream()
                .filter(v -> v.getSku().equals(variantSku))
                .findFirst();

        if (optionalVariant.isEmpty()) {
            throw new ResourceNotFoundException("Variant not found");
        }

        Variant variant = optionalVariant.get();

        if (variant.getStock() < quantity) {
            throw new InsufficientStockException("Not enough stock available");
        }

        Optional<CartItem> existingCartItem = cartItemRepository
                .findByUserIdAndProductIdAndVariantSku(userId, productId, variantSku);

        CartItem cartItem;

        if (existingCartItem.isPresent()) {
            cartItem = existingCartItem.get();
            int newQuantity = cartItem.getQuantity() + quantity;

            if (variant.getStock() < newQuantity) {
                throw new InsufficientStockException("Not enough stock available");
            }

            cartItem.setQuantity(newQuantity);
        } else {
            cartItem = new CartItem();
            cartItem.setUser(user);
            cartItem.setProduct(product);
            cartItem.setVariantSku(variantSku);
            cartItem.setQuantity(quantity);
        }

        return cartItemRepository.save(cartItem);
    }



    @Transactional
    public CartItem updateCartItemQuantity(Long userId, Long cartItemId, Integer quantity) {
        CartItem cartItem = cartItemRepository.findByIdAndUserId(cartItemId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        Product product = cartItem.getProduct();

        Optional<Variant> optionalVariant = product.getVariants().stream()
                .filter(v -> v.getSku().equals(cartItem.getVariantSku()))
                .findFirst();

        if (optionalVariant.isEmpty()) {
            throw new ResourceNotFoundException("Variant not found");
        }

        Variant variant = optionalVariant.get();

        if (variant.getStock() < quantity) {
            throw new InsufficientStockException("Not enough stock available");
        }

        cartItem.setQuantity(quantity);
        return cartItemRepository.save(cartItem);
    }


    @Transactional
    public void removeFromCart(Long userId, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findByIdAndUserId(cartItemId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        cartItemRepository.delete(cartItem);
    }



}
