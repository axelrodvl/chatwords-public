package co.axelrod.chatwords.storage;

import co.axelrod.chatwords.cache.ChatWordsCache;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    @Cacheable(value = ChatWordsCache.USERS, unless = "#result == null")
    Optional<User> findById(String id);

    @Query(value = "{ timeZoneId : { $exists : true } }")
    List<User> findUsersForReminder();

    @Query(value = "{ turnOffWordOfTheDay : { $exists : false } }")
    Page<User> findUsersForWordOfTheDay(Pageable pageable);

    @Query(value = "{ turnOffWordOfTheDay : { $exists : false } }", count = true)
    Long totalUsersForWordOfTheDay();

    @CachePut(value = "users")
    <S extends User> S save(S entity);
}
