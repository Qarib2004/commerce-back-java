package e_commerce.back.security;

import e_commerce.back.entity.Admin;
import e_commerce.back.entity.User;
import e_commerce.back.repository.AdminRepository;
import e_commerce.back.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;

    @Autowired
    public AuthFilter(JwtUtil jwtUtil, UserRepository userRepository, AdminRepository adminRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            try {
                String token = header.replace("Bearer ", "");
                Claims claims = jwtUtil.validateToken(token);
                Long id = claims.get("id", Long.class);
                String role = claims.get("role", String.class);

                UserPrincipal userPrincipal;
                if ("USER".equalsIgnoreCase(role)) {
                    User user = userRepository.findById(id).orElseThrow();
                    userPrincipal = new UserPrincipal(user);
                    request.setAttribute("user", user);
                } else if ("ADMIN".equalsIgnoreCase(role)) {
                    Admin admin = adminRepository.findById(id).orElseThrow();
                    userPrincipal = new UserPrincipal(admin);
                    request.setAttribute("admin", admin);
                } else {
                    throw new RuntimeException("Unknown role: " + role);
                }

                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                userPrincipal,
                                null,
                                userPrincipal.getAuthorities()
                        )
                );

            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token etibarsızdır");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}