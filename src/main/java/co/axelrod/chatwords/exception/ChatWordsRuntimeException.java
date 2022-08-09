package co.axelrod.chatwords.exception;

public class ChatWordsRuntimeException extends RuntimeException {
    public ChatWordsRuntimeException(String message) {
        super(message);
    }

    public ChatWordsRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
