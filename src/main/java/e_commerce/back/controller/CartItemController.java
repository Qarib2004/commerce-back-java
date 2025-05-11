package e_commerce.back.controller;

import java.util.HashMap;
import java.util.Map;

import e_commerce.back.dto.CartItemDTO;
import e_commerce.back.dto.CartItemUpdateDTO;
import e_commerce.back.entity.CartItem;
import e_commerce.back.entity.User;
import e_commerce.back.exception.InsufficientStockException;
import e_commerce.back.exception.ResourceNotFoundException;
import e_commerce.back.repository.UserRepository;
import e_commerce.back.security.UserPrincipal;
import e_commerce.back.service.CartItemService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/cart")
public class CartItemController {

    @Autowired
    private CartItemService cartItemService;

    @Autowired
    private UserRepository userRepository;


    @GetMapping
    public ResponseEntity<List<CartItem>> getCart() {
        Long userId = getCurrentUserId();
        List<CartItem> cartItems = cartItemService.getUserCartItems(userId);
        return ResponseEntity.ok(cartItems);
    }

    @PostMapping
    public ResponseEntity<?> addToCart(

            @Valid @RequestBody CartItemDTO cartItemDTO) {
        try {
            Long userId = getCurrentUserId();
            CartItem cartItem = cartItemService.addToCart(
                    userId,
                    cartItemDTO.getProductId(),
                    cartItemDTO.getVariantSku(),
                    cartItemDTO.getQuantity()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(cartItem);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(buildErrorResponse(e.getMessage()));
        } catch (InsufficientStockException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(buildErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(buildErrorResponse("Server error"));
        }
    }


    @PutMapping("/{id}")
    public ResponseEntity<?> updateCartItemQuantity(
            @PathVariable Long id,
            @Valid @RequestBody CartItemUpdateDTO updateDTO) {
        try {
            Long userId = getCurrentUserId();
            CartItem updatedCartItem = cartItemService.updateCartItemQuantity(userId, id, updateDTO.getQuantity());
            return ResponseEntity.ok(updatedCartItem);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(buildErrorResponse(e.getMessage()));
        } catch (InsufficientStockException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(buildErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(buildErrorResponse("Server error"));
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeFromCart(  @PathVariable Long id) {
        try {
            Long userId =getCurrentUserId();
            cartItemService.removeFromCart(userId, id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Item removed from cart");
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(buildErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(buildErrorResponse("Server error"));
        }
    }

    private Map<String, String> buildErrorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return response;
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == "anonymousUser") {
            throw new RuntimeException("User not authifications");
        }

        if (authentication.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            return userPrincipal.getId();
        }

        if (authentication.getPrincipal() instanceof String) {
            String email = (String) authentication.getPrincipal();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found: " + email));
            return user.getId();
        }

        throw new RuntimeException("Неизвестный тип аутентификации");
    }
}