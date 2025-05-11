package e_commerce.back.security;

import e_commerce.back.entity.Admin;
import e_commerce.back.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserPrincipal implements UserDetails {


    private final Long id;
    private final String fullName;
    private final String email;
    private final String password;
    private final String role;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(User user) {
        this.id = user.getId();
        this.fullName = user.getFullName();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.role = "USER";
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    public UserPrincipal(Admin admin) {
        this.id = admin.getId();
        this.fullName = admin.getName();
        this.email = admin.getEmail();
        this.password = admin.getPassword();
        this.role = "ADMIN";
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));


    }

    public static UserPrincipal create(User user) {
        return new UserPrincipal(user);
    }

    public static UserPrincipal create(Admin admin) {
        return new UserPrincipal(admin);
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}