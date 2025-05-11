package e_commerce.back.controller;
import e_commerce.back.entity.Product;
import e_commerce.back.entity.User;
import e_commerce.back.repository.UserRepository;
import e_commerce.back.security.UserPrincipal;
import e_commerce.back.service.UserService;
import e_commerce.back.service.WishlistService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {
    private static final Logger logger = (Logger) LoggerFactory.getLogger(WishlistController.class);
    private final WishlistService wishlistService;
    private final UserService userService;
    private final UserRepository userRepository;

    @Autowired
    public WishlistController(WishlistService wishlistService, UserService userService,UserRepository userRepository) {
        this.wishlistService = wishlistService;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<Product>> getWishlist() {
        Long userId = getCurrentUserId();
        List<Product> wishlist = wishlistService.getWishlist(userId);
        return ResponseEntity.ok(wishlist);
    }

    @PostMapping("/{productId}")
    public ResponseEntity<Map<String, String>> addToWishlist(@PathVariable Long productId) {
        try {
            Long userId = getCurrentUserId();
            if (userId == null) {
                throw new RuntimeException("User not found");
            }

            boolean productAdded = wishlistService.addToWishlist(userId, productId);
            if (!productAdded) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Product could not be added to wishlist"));
            }

            Map<String, String> response = new HashMap<>();
            response.put("message", "Product added to wishlist");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error while adding product to wishlist", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "An error occurred"));
        }
    }



    @DeleteMapping("/{productId}")
    public ResponseEntity<Map<String, String>> removeFromWishlist(@PathVariable Long productId) {
        Long userId = getCurrentUserId();
        wishlistService.removeFromWishlist(userId, productId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Məhsul sevimlilərdən silindi");
        return ResponseEntity.ok(response);
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
