package com.eventflow.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "profile_images")
public class ProfileImage {

    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;

    private String contentType;

    private byte[] data;

    public ProfileImage(String userId, String contentType, byte[] data) {
        this.userId = userId;
        this.contentType = contentType;
        this.data = data;
    }
}
