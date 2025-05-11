package e_commerce.back.repository;

import e_commerce.back.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem,Long> {

    List<CartItem> findByUserId(Long userId);

    Optional<CartItem> findByUserIdAndProductIdAndVariantSku(Long userId,Long productId,String variantSku);


    Optional<CartItem> findByIdAndUserId(Long id,Long userId);


    void deleteByIdAndUserId(Long id,Long userId);



}
