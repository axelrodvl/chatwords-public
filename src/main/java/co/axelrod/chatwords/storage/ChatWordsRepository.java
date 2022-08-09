package co.axelrod.chatwords.storage;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatWordsRepository extends MongoRepository<ChatWords, Long> {
    default ChatWords getConfig() {
        List<ChatWords> configs = findAll();
        if (configs.isEmpty()) {
            ChatWords chatWords = new ChatWords();
            save(chatWords);
            return chatWords;
        } else {
            return configs.get(0);
        }
    }
}
