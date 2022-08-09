package co.axelrod.chatwords.storage;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Getter
@Setter
public class SpeechKitFile {
    @Id
    private String id;
    private Binary file;

    // For mongo only
    public SpeechKitFile() {

    }
}
