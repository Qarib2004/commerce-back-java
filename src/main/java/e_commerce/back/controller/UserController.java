package e_commerce.back.controller;



import e_commerce.back.dto.BalanceRequest;
import e_commerce.back.dto.LoginRequest;
import e_commerce.back.dto.PasswordChangeRequest;
import e_commerce.back.dto.RegisterRequest;
import e_commerce.back.entity.User;
import e_commerce.back.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/auth/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }

        String token = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("token", token));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }

        String token = userService.loginUser(request);
        return ResponseEntity.ok(Map.of("token", token));
    }


    @GetMapping("/auth/me")
    public ResponseEntity<?> getCurrentUser(@RequestAttribute("user") User user) {
        return ResponseEntity.ok(userService.getUserDetails(user.getId()));
    }

    @PostMapping("/auth/change-password")
    public ResponseEntity<?> changePassword(
            @RequestAttribute("user") User user,
            @Valid @RequestBody PasswordChangeRequest request) {

        userService.changePassword(user.getId(), request);
        return ResponseEntity.ok(Map.of("message", "Şifrə uğurla yeniləndi"));
    }

    @PostMapping("/auth/update-avatar")
    public ResponseEntity<?> updateAvatar(
            @RequestAttribute("user") User user,
            @RequestParam("avatar") MultipartFile file) {

        String avatarUrl = userService.updateAvatar(user.getId(), file);
        return ResponseEntity.ok(Map.of("avatarUrl", avatarUrl));
    }

    @DeleteMapping("/auth/avatar")
    public ResponseEntity<?> deleteAvatar(@RequestAttribute("user") User user) {
        userService.deleteAvatar(user.getId());
        return ResponseEntity.ok(Map.of("message", "Profil şəkli silindi"));
    }

    @PostMapping("/auth/add-balance")
    public ResponseEntity<?> updateBalance(
            @RequestAttribute("user") User user,
            @Valid @RequestBody BalanceRequest request) {

        double newBalance = userService.updateBalance(user.getId(), request.getAmount());
        return ResponseEntity.ok(Map.of("balance", newBalance));
    }
}