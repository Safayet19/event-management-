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
@Document(collection = "speaker_images")
public class SpeakerImage {

    @Id
    private String id;

    @Indexed(unique = true)
    private String speakerId;

    private String contentType;
    private byte[] data;

    public SpeakerImage(String speakerId, String contentType, byte[] data) {
        this.speakerId = speakerId;
        this.contentType = contentType;
        this.data = data;
    }
}
