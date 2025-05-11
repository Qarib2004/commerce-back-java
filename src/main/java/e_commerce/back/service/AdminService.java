package e_commerce.back.service;


import e_commerce.back.entity.Admin;
import e_commerce.back.entity.User;
import e_commerce.back.repository.AdminRepository;
import e_commerce.back.repository.UserRepository;
import e_commerce.back.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    public String login(Admin admin) {
        Admin foundAdmin = adminRepository.findByEmail(admin.getEmail());
        if (foundAdmin == null || !foundAdmin.getPassword().equals(admin.getPassword())) {
            throw new RuntimeException("Email or password incorrect");
        }
        return jwtUtil.generateToken(foundAdmin.getId(), "ADMIN");
    }

    public Admin getAdminData(String token) {
        Long adminId = jwtUtil.validateToken(token.replace("Bearer ", "")).get("id", Long.class);
        return adminRepository.findById(adminId).orElseThrow(() -> new RuntimeException("Admin not found"));
    }

    public Object getDashboardStats() {
        return new Object();
    }

    public String deleteUser(Long id) {
        userRepository.deleteById(id);
        return "User deleted";
    }

    public List<Object> getProducts() {
        return List.of();
    }

    public Object addProduct(Object product) {
        return new Object();
    }

    public Object updateProduct(Long id, Object product) {
        return new Object();
    }

    public String deleteProduct(Long id) {
        return "Product deleted";
    }
}
