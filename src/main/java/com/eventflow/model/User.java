package com.eventflow.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Locale;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "users")
public class User {

    private static final Set<String> DEMO_EMAILS = Set.of(
            "admin@eventflow.com",
            "organizer@eventflow.com",
            "participant@eventflow.com"
    );

    @Id
    private String id;

    private String fullName;

    @Indexed(unique = true)
    private String email;

    private String password;

    @Indexed
    private String role;

    private boolean enabled = true;

    private String profileImageUrl;

    public User(String fullName, String email, String password, String role) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.role = role;
        this.enabled = true;
    }

    public boolean isDemoAccount() {
        return email != null && DEMO_EMAILS.contains(email.trim().toLowerCase(Locale.ROOT));
    }
}
