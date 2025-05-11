package e_commerce.back.controller;

import e_commerce.back.entity.Admin;
import e_commerce.back.entity.User;
import e_commerce.back.repository.AdminRepository;
import e_commerce.back.repository.UserRepository;
import e_commerce.back.security.JwtUtil;
import e_commerce.back.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Autowired
    public AdminController(AdminService adminService,
                           AdminRepository adminRepository,
                           UserRepository userRepository,
                           JwtUtil jwtUtil) {
        this.adminService = adminService;
        this.adminRepository = adminRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public String login(@RequestBody Admin admin) {
        return adminService.login(admin);
    }

    @GetMapping("/me")
    public Admin getAdminData(@RequestHeader("Authorization") String token) {
        return adminService.getAdminData(token);
    }

    @GetMapping("/dashboard-stats")
    public Object getDashboardStats(@RequestHeader("Authorization") String token) {
        return adminService.getDashboardStats();
    }

    @GetMapping("/users")
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @DeleteMapping("/users/{id}")
    public String deleteUser(@PathVariable Long id) {
        return adminService.deleteUser(id);
    }

    @GetMapping("/products")
    public List<Object> getProducts() {
        return adminService.getProducts();
    }

    @PostMapping("/products")
    public Object addProduct(@RequestBody Object product) {
        return adminService.addProduct(product);
    }

    @PutMapping("/products/{id}")
    public Object updateProduct(@PathVariable Long id, @RequestBody Object product) {
        return adminService.updateProduct(id, product);
    }

    @DeleteMapping("/products/{id}")
    public String deleteProduct(@PathVariable Long id) {
        return adminService.deleteProduct(id);
    }
}
