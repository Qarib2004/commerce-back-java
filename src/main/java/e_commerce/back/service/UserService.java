package e_commerce.back.service;

import e_commerce.back.dto.BalanceRequest;
import e_commerce.back.dto.LoginRequest;
import e_commerce.back.dto.PasswordChangeRequest;
import e_commerce.back.dto.RegisterRequest;
import e_commerce.back.entity.User;
import e_commerce.back.repository.UserRepository;
import e_commerce.back.security.JwtUtil;
import e_commerce.back.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;


    @Autowired
    public UserService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    private static final String UPLOAD_DIR = "uploads/avatars";

    {
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
        } catch (IOException e) {
            System.err.println("Failed to create avatar directory: " + e.getMessage());
        }
    }

    public String registerUser(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bu email artıq istifadə olunub");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setFullName(request.getFullName());
        user.setBalance(0.0);

        User savedUser = userRepository.save(user);
        return jwtUtil.generateToken(savedUser.getId(), "USER");
    }

    public String loginUser(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "İstifadəçi tapılmadı"));

        if (!user.comparePassword(request.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Yanlış şifrə");
        }

        return jwtUtil.generateToken(user.getId(), "USER");
    }

    public User getUserDetails(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "İstifadəçi tapılmadı"));
    }

    public void changePassword(Long userId, PasswordChangeRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "İstifadəçi tapılmadı"));

        if (!user.comparePassword(request.getCurrentPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cari şifrə yanlışdır");
        }

        user.setPassword(request.getNewPassword());
        userRepository.save(user);
    }

    public String updateAvatar(Long userId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Şəkil yüklənmədi");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "İstifadəçi tapılmadı"));

        try {
            String contentType = file.getContentType();
            if (contentType == null ||
                    !(contentType.equals("image/jpeg") ||
                            contentType.equals("image/png") ||
                            contentType.equals("image/gif"))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Yalnız şəkil faylları yükləyə bilərsiniz!");
            }

            if (file.getSize() > 5 * 1024 * 1024) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fayl həcmi 5MB-dan çox olmamalıdır");
            }

            deleteExistingAvatar(user);

            String filename = UUID.randomUUID() + "-" + file.getOriginalFilename();
            Path targetPath = Paths.get(UPLOAD_DIR, filename);
            Files.copy(file.getInputStream(), targetPath);

            String avatarUrl = "/uploads/avatars/" + filename;
            user.setAvatarUrl(avatarUrl);
            userRepository.save(user);

            return avatarUrl;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Server xətası: " + e.getMessage());
        }
    }

    public void deleteAvatar(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "İstifadəçi tapılmadı"));

        try {
            deleteExistingAvatar(user);
            user.setAvatarUrl("");
            userRepository.save(user);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Server xətası: " + e.getMessage());
        }
    }

    public double updateBalance(Long userId, Double amount) {
        if (amount == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Düzgün məbləğ daxil edin");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "İstifadəçi tapılmadı"));

        if (amount < 0 && user.getBalance() + amount < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Kifayət qədər balans yoxdur");
        }

        user.setBalance(user.getBalance() + amount);
        userRepository.save(user);

        return user.getBalance();
    }

    private void deleteExistingAvatar(User user) throws IOException {
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            String filename = user.getAvatarUrl().substring(user.getAvatarUrl().lastIndexOf("/") + 1);
            Path filePath = Paths.get(UPLOAD_DIR, filename);
            Files.deleteIfExists(filePath);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Пользователь не найден с email: " + email)
                );

        return UserPrincipal.create(user);
    }

    public Long findIdByEmail(String email) {
        return userRepository.findIdByEmail(email);
    }

}
