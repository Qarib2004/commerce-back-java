package e_commerce.back.service;


import e_commerce.back.entity.Order;
import e_commerce.back.entity.OrderItem;
import e_commerce.back.entity.Product;
import e_commerce.back.entity.User;
import e_commerce.back.repository.OrderRepository;
import e_commerce.back.repository.ProductRepository;
import e_commerce.back.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    @Autowired

    public OrderService(OrderRepository orderRepository, UserRepository userRepository,ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }


    public List<Order> getUserOrders(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found by id:" + userId));

        return  orderRepository.findByUserOrderByCreatedAtDesc(user);
    }


    @Transactional
    public Order createOrder(Order order) {
        for (OrderItem item : order.getItems()) {
            Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + item.getProduct().getId()));

            item.setProduct(product);

            if (item.getPrice() == null || item.getPrice() == 0.0) {
                product.getVariants().stream()
                        .findFirst()
                        .ifPresentOrElse(variant -> item.setPrice(variant.getPrice()),
                                () -> {
                                    throw new RuntimeException("No price available for product ID: " + product.getId());
                                });
            }


            item.setOrder(order);
        }

        double total = order.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        order.setTotalAmount(total);
        order.setCreatedAt(new Date());

        return orderRepository.save(order);
    }

    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }




    public void deleteOrder(Long orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new RuntimeException("Order not found");
        }
        orderRepository.deleteById(orderId);
    }

    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() == Order.Status.CANCELLED) {
            throw new RuntimeException("Order is already cancelled");
        }

        order.setStatus(Order.Status.CANCELLED);
        orderRepository.save(order);
    }

    public void updateOrderStatus(Long orderId, String statusStr) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        try {
            Order.Status status = Order.Status.valueOf(statusStr.toUpperCase());
            order.setStatus(status);
            orderRepository.save(order);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + statusStr);
        }
    }

    public Order updateShippingAddress(Long orderId, String newAddress) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setShippingAddress(newAddress);
        return orderRepository.save(order);
    }








}
