package co.axelrod.chatwords.storage.internal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WordOfTheDay {
    private Integer usersToNotify;
    private Integer notifiedUsers;
    private Boolean finished;

    public WordOfTheDay() {
        reset();
    }

    public void reset() {
        this.usersToNotify = 0;
        this.notifiedUsers = 0;
        this.finished = false;
    }
}
