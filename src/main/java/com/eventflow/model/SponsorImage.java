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
@Document(collection = "sponsor_images")
public class SponsorImage {

    @Id
    private String id;

    @Indexed(unique = true)
    private String sponsorId;

    private String contentType;
    private byte[] data;

    public SponsorImage(String sponsorId, String contentType, byte[] data) {
        this.sponsorId = sponsorId;
        this.contentType = contentType;
        this.data = data;
    }
}
