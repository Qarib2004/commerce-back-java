package e_commerce.back.repository;

import e_commerce.back.entity.Order;
import e_commerce.back.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository  extends JpaRepository<Order,Long> {

    List<Order> findByUserOrderByCreatedAtDesc(User user);


    Optional<Order> findByIdAndUser(Long id,User user);


}
