package co.axelrod.chatwords.storage.sorting;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Sort {
    OLD_TO_NEW("⬇️"),
    NEW_TO_OLD("⬆️");

    private final String emoji;
}
