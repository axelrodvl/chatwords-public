package co.axelrod.chatwords.storage;

import co.axelrod.chatwords.dictionary.model.Language;
import co.axelrod.chatwords.storage.internal.WordOfTheDay;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.EnumMap;
import java.util.Map;

@EqualsAndHashCode
@Document
@Getter
@Setter
public class ChatWords {
    @Id
    private String id;

    private Long timestamp;
    private Map<Language, UserDictionary> dictionaryOfTheDay;

    private WordOfTheDay wordOfTheDay;

    public ChatWords() {
        this.timestamp = Date.from(
                Instant.now().atZone(ZoneId.systemDefault())
                        .truncatedTo(ChronoUnit.DAYS).toInstant()
        ).getTime();

        this.dictionaryOfTheDay = new EnumMap<>(Language.class);
        this.wordOfTheDay = new WordOfTheDay();
    }
}
