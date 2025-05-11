package e_commerce.back.entity;

import e_commerce.back.enums.ActorType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long senderId;

    @Enumerated(EnumType.STRING)
    private ActorType senderType;

    private Long receiverId;

    @Enumerated(EnumType.STRING)
    private ActorType receiverType;

    @NotBlank
    private String content;

    private boolean isRead = false;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt = new Date();
}