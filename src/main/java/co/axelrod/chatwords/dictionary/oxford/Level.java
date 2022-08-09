package co.axelrod.chatwords.dictionary.oxford;

import co.axelrod.chatwords.exception.ChatWordsRuntimeException;

public enum Level {
    A1,
    A2,
    B1,
    B2;

    public static String getDictionaryName(Level level) {
        switch (level) {
            case A1:
                return "The Oxford 3000™ - A1";
            case A2:
                return "The Oxford 3000™ - A2";
            case B1:
                return "The Oxford 3000™ - B1";
            case B2:
                return "The Oxford 3000™ - B2";
        }

        throw new ChatWordsRuntimeException("Invalid Oxford level");
    }
}
