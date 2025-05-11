package e_commerce.back.service;

import e_commerce.back.entity.CartItem;
import e_commerce.back.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CartService {


    private final CartRepository cartRepository;

    @Autowired
    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }



    @Transactional
    public List<CartItem> getCartItemsByUserId(Long userId) {
        return cartRepository.findByUserId(userId);
    }


    @Transactional
    public Optional<CartItem> findByUserIdAndProductIdAndVariantSku(Long userId, Long productId, String variantSku) {
        return cartRepository.findByUserIdAndProductIdAndVariantSku(userId, productId, variantSku);
    }




     @Transactional
    public Optional<CartItem> findByIdAndUserId(Long id, Long userId) {
        return cartRepository.findByIdAndUserId(id, userId);
    }

    @Transactional
    public CartItem saveCartItem(CartItem cartItem) {
        return cartRepository.save(cartItem);
    }

    @Transactional
    public CartItem updateCartItem(CartItem cartItem) {
        return cartRepository.save(cartItem);
    }




    @Transactional
    public boolean deleteByIdAndUserId(Long id,Long userId){
        Optional<CartItem> cartItem = cartRepository.findByIdAndUserId(id,userId);
        if(cartItem.isPresent()) {
              cartRepository.delete(cartItem.get());
              return true;
        }
        return  false;

    }




}
