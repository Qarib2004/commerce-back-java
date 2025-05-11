package e_commerce.back.controller;

import e_commerce.back.entity.Order;
import e_commerce.back.entity.OrderItem;
import e_commerce.back.entity.Product;
import e_commerce.back.entity.User;
import e_commerce.back.repository.UserRepository;
import e_commerce.back.security.UserPrincipal;
import e_commerce.back.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);


    private final OrderService orderService;
    private final UserRepository userRepository;

    @Autowired
    public OrderController(OrderService orderService, UserRepository userRepository) {
        this.orderService = orderService;
        this.userRepository = userRepository;
    }


    @GetMapping
    public ResponseEntity<List<Order>> getOrders() {
        Long userId = getCurrentUserId();
        List<Order> orders = orderService.getUserOrders(userId);
        return ResponseEntity.ok(orders);
    }


    @PostMapping
    public ResponseEntity<Map<String, String>> addToOrders(@RequestBody Map<String, Object> orderRequest) {
        try {
            logger.info("Received order request: {}", orderRequest);

            // Проверяем наличие структуры items в запросе
            List<Map<String, Object>> items = (List<Map<String, Object>>) orderRequest.get("items");

            if (items == null || items.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "At least one item is required"));
            }

            // Получаем значения из запроса
            String shippingAddress = (String) orderRequest.getOrDefault("shippingAddress", "Default address");
            String status = (String) orderRequest.getOrDefault("status", "pending");
            Double totalAmount = orderRequest.containsKey("totalAmount") ?
                    Double.valueOf(orderRequest.get("totalAmount").toString()) : 0.0;

            Long userId = getCurrentUserId();  // Получаем ID текущего пользователя
            logger.info("Текущий ID пользователя: {}", userId);

            // Создаем пользователя
            User user = new User();
            user.setId(userId);

            // Создаем заказ
            Order order = new Order();
            order.setUser(user);
            order.setShippingAddress(shippingAddress);
            order.setStatus(Order.Status.valueOf(status.toUpperCase()));
            order.setCreatedAt(new Date());
            order.setTotalAmount(totalAmount);
            order.setItems(new ArrayList<>());

            // Добавляем все товары из запроса
            for (Map<String, Object> itemData : items) {
                // Получаем ID продукта из элемента items
                Long productId = null;
                if (itemData.containsKey("product")) {
                    Object productObj = itemData.get("product");
                    if (productObj instanceof Integer) {
                        productId = ((Integer) productObj).longValue();
                    } else if (productObj instanceof Long) {
                        productId = (Long) productObj;
                    } else if (productObj instanceof String) {
                        productId = Long.parseLong((String) productObj);
                    }
                }

                if (productId == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("message", "Product ID is required for each item"));
                }

                // Создаем продукт
                Product product = new Product();
                product.setId(productId);

                // Получаем количество товара
                Integer quantity = 1; // По умолчанию 1
                if (itemData.containsKey("quantity")) {
                    Object quantityObj = itemData.get("quantity");
                    if (quantityObj instanceof Integer) {
                        quantity = (Integer) quantityObj;
                    } else if (quantityObj instanceof String) {
                        quantity = Integer.parseInt((String) quantityObj);
                    }
                }

                // Получаем цену товара, если она указана
                Double price = null;
                if (itemData.containsKey("price")) {
                    Object priceObj = itemData.get("price");
                    if (priceObj instanceof Double) {
                        price = (Double) priceObj;
                    } else if (priceObj instanceof Integer) {
                        price = ((Integer) priceObj).doubleValue();
                    } else if (priceObj instanceof String) {
                        price = Double.parseDouble((String) priceObj);
                    }
                }

                // Создаем элемент заказа
                OrderItem item = new OrderItem();
                item.setProduct(product);
                item.setQuantity(quantity);
                item.setOrder(order);
                if (price != null) {
                    item.setPrice(price);
                }

                order.getItems().add(item);
            }

            logger.info("Создание заказа для пользователя с ID {}", userId);
            logger.info("Заказ содержит {} товаров", order.getItems().size());

            orderService.createOrder(order);  // Сохраняем заказ

            logger.info("Заказ успешно создан для пользователя {}", userId);

            return ResponseEntity.ok(Map.of("message", "Order created successfully"));
        } catch (Exception e) {
            logger.error("Ошибка при создании заказа", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "An error occurred: " + e.getMessage()));
        }
    }





    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long orderId) {
        Order order = orderService.getOrderById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return ResponseEntity.ok(order);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Map<String, String>> deleteOrder(@PathVariable Long orderId) {
        try {
            orderService.deleteOrder(orderId);
            return ResponseEntity.ok(Map.of("message", "Order deleted"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Error: " + e.getMessage()));
        }
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<Map<String, String>> cancelOrder(@PathVariable Long orderId) {
        try {
            orderService.cancelOrder(orderId);
            return ResponseEntity.ok(Map.of("message", "Order cancelled"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<Map<String, String>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam("status") String status) {
        try {
            orderService.updateOrderStatus(orderId, status);
            return ResponseEntity.ok(Map.of("message", "Status updated"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<Order> updateOrderAddress(
            @PathVariable Long orderId,
            @RequestParam("shippingAddress") String newAddress) {
        Order updatedOrder = orderService.updateShippingAddress(orderId, newAddress);
        return ResponseEntity.ok(updatedOrder);
    }




    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            throw new RuntimeException("User is not authenticated");
        }

        if (authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getId();
        }

        if (authentication.getPrincipal() instanceof String email) {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found: " + email));
            return user.getId();
        }

        throw new RuntimeException("Unknown authentication type");
    }
}
