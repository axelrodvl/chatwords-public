package co.axelrod.chatwords.storage;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface SpeechKitRepository extends MongoRepository<SpeechKitFile, String> {
}
