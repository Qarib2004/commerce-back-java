//package e_commerce.back.controller;
//
//import e_commerce.back.dto.AddToCartRequest;
//import e_commerce.back.dto.SuccessResponse;
//import e_commerce.back.dto.UpdateCartRequest;
//import e_commerce.back.entity.CartItem;
//import e_commerce.back.entity.Product;
//import e_commerce.back.entity.User;
//import e_commerce.back.entity.Variant;
//import e_commerce.back.repository.ProductRepository;
//import e_commerce.back.repository.UserRepository;
//import e_commerce.back.service.CartService;
//import e_commerce.back.service.ProductService;
//import e_commerce.back.service.UserService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.web.ErrorResponse;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.Optional;
//
//@RestController
//@RequestMapping("/api/cart")
//public class CartController {
//
//    private CartService cartService;
//    private ProductService productService;
//    private UserService userService;
//
//    private ProductRepository productRepository;
//    private UserRepository  userRepository;
//    @Autowired
//    public CartController(CartService cartService, ProductService productService,UserService userService) {
//        this.cartService = cartService;
//        this.productService = productService;
//        this.userService = userService;
//    }
//
//
//
//
//    @GetMapping
//    public ResponseEntity<?> getCart(@AuthenticationPrincipal UserDetails userDetails) {
//        try {
//            Long userId = Long.parseLong(userDetails.getUsername());
//
//            List<CartItem> cartItems = cartService.getCartItemsByUserId(userId);
//            return ResponseEntity.ok(cartItems);
//
//        } catch (NumberFormatException e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body("Invalid user ID format");
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .build();
//        }
//    }
//
//
//
//    @PostMapping
//    public ResponseEntity<?> addToCart(@RequestBody AddToCartRequest request,
//                                       @AuthenticationPrincipal UserDetails userDetails) {
//        try {
//            String userId = userDetails.getUsername();
//
//            Optional<Product> productOpt = productService.geyProductById(request.getProductId());
//            if (productOpt.isEmpty()) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                        .build();
//            }
//
//            Product product = productOpt.get();
//            Optional<Variant> variantOpt = product.getVariants().stream()
//                    .filter(v -> v.getSku().equals(request.getVariantSku()))
//                    .findFirst();
//
//            if (variantOpt.isEmpty()) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                        .build();
//            }
//
//            Variant variant = variantOpt.get();
//
//            if (variant.getStock() < request.getQuantity()) {
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                        .build();
//            }
//
//            Optional<CartItem> existingCartItem = cartService.findByUserIdAndProductIdAndVariantSku(
//                    Long.parseLong(userId), product.getId(), request.getVariantSku());
//
//            CartItem cartItem;
//
//            User user = userRepository.findById(Long.parseLong(userId)).orElseThrow(() -> new RuntimeException("User not found"));
//            Product product1 = productRepository.findById(product.getId()).orElseThrow(() -> new RuntimeException("Product not found"));
//            if (existingCartItem.isPresent()) {
//                cartItem = existingCartItem.get();
//                int newQuantity = cartItem.getQuantity() + request.getQuantity();
//
//                if (variant.getStock() < newQuantity) {
//                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                            .build();                }
//
//                cartItem.setQuantity(newQuantity);
//                cartItem = cartService.updateCartItem(cartItem);
//            } else {
//                cartItem = new CartItem();
//                cartItem.setUser(user);
//                cartItem.setProduct(product1);
//                cartItem.setVariantSku(request.getVariantSku());
//                cartItem.setQuantity(request.getQuantity());
//                cartItem = cartService.saveCartItem(cartItem);
//            }
//
//            return ResponseEntity.status(HttpStatus.CREATED).body(cartItem);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .build();
//        }
//    }
//
//    @PutMapping("/{id}")
//    public ResponseEntity<?> updateCartItem(@PathVariable Long id,
//                                            @RequestBody UpdateCartRequest request,
//                                            @AuthenticationPrincipal UserDetails userDetails) {
//        try {
//            Long userId = Long.parseLong(userDetails.getUsername());
//            Optional<CartItem> cartItemOpt = cartService.findByIdAndUserId(id, userId);
//            if (cartItemOpt.isEmpty()) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                        .build();
//            }
//
//            CartItem cartItem = cartItemOpt.get();
//
//            Optional<Product> productOpt = productService.geyProductById(cartItem.getProduct().getId());
//            if (productOpt.isEmpty()) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                        .build();
//            }
//
//            Product product = productOpt.get();
//            String variantSku = cartItem.getVariantSku();
//            Optional<Variant> variantOpt = product.getVariants().stream()
//                    .filter(v -> v.getSku().equals(variantSku))
//                    .findFirst();
//
//
//            if (variantOpt.isEmpty()) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                        .build();
//            }
//
//            Variant variant = variantOpt.get();
//
//            if (variant.getStock() < request.getQuantity()) {
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                        .build();
//            }
//
//            cartItem.setQuantity(request.getQuantity());
//            cartItem = cartService.updateCartItem(cartItem);
//
//            return ResponseEntity.ok(cartItem);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .build();
//        }
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<?> removeFromCart(@PathVariable Long id,
//                                            @AuthenticationPrincipal UserDetails userDetails) {
//        try {
//            Long userId = Long.parseLong(userDetails.getUsername());
//
//            boolean deleted = cartService.deleteByIdAndUserId(id, userId);
//            if (!deleted) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                        .build();
//            }
//
//            return ResponseEntity.ok(new SuccessResponse("Product deleted"));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .build();
//        }
//
//
//    }
//}
