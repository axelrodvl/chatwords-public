package co.axelrod.chatwords.storage.story;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AlterDictionaryStory extends UserStory {
    private Boolean renamingDictionary;
    private Boolean deletingDictionary;
}
