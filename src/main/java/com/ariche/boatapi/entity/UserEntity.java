package com.ariche.boatapi.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * A user.
 */
@Entity
@Getter
@Setter
@Table(name = "_user",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "email", name = "ux_user_email"),
        @UniqueConstraint(columnNames = "login", name = "ux_user_login")
    })
public class UserEntity {

    @Id
    @SequenceGenerator(name = "user_seq", sequenceName = "user_seq", allocationSize = 1, initialValue = 300_000)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    private Long id;

    @NotBlank(message = "{user.validation.error.login.blank}")
    @Size(min = 3, max = 50, message = "{user.validation.error.login.invalid.length}")
    @Column(length = 50, unique = true, nullable = false)
    private String login;

    @Size(min = 60, max = 60)
    @Column(name = "password_hash", length = 60, nullable = false)
    private String password;

    @NotBlank(message = "{user.validation.error.firstname.blank}")
    @Size(min = 2, max = 50, message = "{user.validation.error.firstname.invalid.length}")
    @Pattern(regexp = "[A-Za-z\\-\s]{2,50}")   // TODO: Could be improved to prevent from "--" or " "
    @Column(name = "first_name", length = 50, nullable = false)
    private String firstName;

    @NotBlank(message = "{user.validation.error.lastname.blank}")
    @Size(min = 2, max = 50, message = "{user.validation.error.lastname.invalid.length}")
    @Pattern(regexp = "[A-Za-z\\-\s]{2,50}")   // TODO: Could be improved to prevent from "--" or " "
    @Column(name = "last_name", length = 50, nullable = false)
    private String lastName;

    @Email(message = "{user.validation.error.email.invalid.format}")
    @Size(min = 5, max = 255, message = "{user.validation.error.email.invalid.length}")
    @Column(length = 255, unique = true, nullable = false)
    private String email;

    @NotNull
    @Column(nullable = false)
    private boolean activated = false;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "_user_authority",
        joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")},
        inverseJoinColumns = {@JoinColumn(name = "authority_name", referencedColumnName = "name")})
    private List<AuthorityEntity> authorities;

}
