package e_commerce.back.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "promo_codes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromoCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true, nullable = false)
    private String code;

    @Email
    @NotBlank
    @Column(nullable = false)
    private String email;

    @Min(0)
    @Max(100)
    @NotNull
    private Integer discountPercent;

    private boolean isUsed = false;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    private Date expiresAt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt = new Date();
}