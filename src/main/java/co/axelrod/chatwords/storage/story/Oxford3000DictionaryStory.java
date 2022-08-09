package co.axelrod.chatwords.storage.story;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Oxford3000DictionaryStory extends UserStory {
    private Boolean enterLanguage;

    public Oxford3000DictionaryStory() {
        this.enterLanguage = false;
    }
}
