package co.axelrod.chatwords.bot.reminder;

import co.axelrod.chatwords.bot.analytics.Analytics;
import co.axelrod.chatwords.bot.analytics.UserEvent;
import co.axelrod.chatwords.bot.command.Callback;
import co.axelrod.chatwords.bot.i18n.Text;
import co.axelrod.chatwords.dictionary.generator.WordGenerator;
import co.axelrod.chatwords.dictionary.model.Language;
import co.axelrod.chatwords.dictionary.provider.yandex.YandexDictionary;
import co.axelrod.chatwords.storage.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class DictionaryOfTheDayReminder {
    private static final int DICTIONARY_OF_THE_DAY_WORDS_COUNT = 5;

    private final Analytics analytics;
    private final WordRepository wordRepository;
    private final UserRepository userRepository;
    private final ChatWordsRepository chatWordsRepository;
    private final DefaultAbsSender chatWordsBot;
    private final YandexDictionary yandexDictionary;

    //        @Scheduled(cron = "0 * * * * *", zone = "Europe/Moscow") // Every minute
//    @Scheduled(cron = "0 0 12 * * *", zone = "Europe/Moscow") // Every day at 12:00 MSK
    public void wordOfTheDaySender() {
        log.info("Starting everyday reminder for word of the day");

        Map<Language, UserDictionary> dictionariesOfTheDay = new EnumMap<>(Language.class);

        // TODO get from DB
        if (true) {
            dictionariesOfTheDay = initializeDictionariesOfTheDay();
        }

        Map<Language, UserDictionary> finalDictionariesOfTheDay = dictionariesOfTheDay;
        userRepository.findAll().forEach(user -> {
            Language sourceLanguage = Language.RUSSIAN;
            Language targetLanguage = Language.ENGLISH;

            Locale locale = new Locale(user.getLanguageCode());
            ResourceBundle messages = ResourceBundle.getBundle("messages", locale);
            StringBuilder message = new StringBuilder();

            message.append(
                    String.format(messages.getString(Text.DICITONARY_OF_THE_DAY.getValue()),
                            sourceLanguage.getEmoji(),
                            targetLanguage.getEmoji())
            );
            message.append("\n");

            int count = 1;
            for (UserWord userWord : finalDictionariesOfTheDay.get(targetLanguage).getWords()) {
                Word word = userWord.getWord();
                message.append(count++);
                message.append(". ");
                message.append(word.getTranslation(sourceLanguage));
                message.append(" • ");
                message.append(word.getTranslation(targetLanguage));
                message.append("\n");
            }

            analytics.log(user, UserEvent.DICTIONARY_OF_THE_DAY_REMINDER);

//            message = new StringBuilder();
//            message.append("E buonasera signore e signori!\n" +
//                    "❤️\u200D\uD83D\uDD25 Любишь Måneskin?\n" +
//                    "Добавь словарь с лексикой песни \"Zitti e Buoni\" прямо сейчас.");

            try {
                chatWordsBot.execute(SendMessage.builder()
                        .chatId(user.getId())
                        .text(message.toString())
                        .replyMarkup(getDictionaryOfTheDayKeyboard(messages))
                        .parseMode(ParseMode.HTML)
                        .build());
            } catch (TelegramApiException e) {
                log.error("Unable to send quiz reminder for user " + user.getName());
            }

            log.info("Quiz reminder for " + user.getName());
        });
    }

    private InlineKeyboardMarkup getDictionaryOfTheDayKeyboard(ResourceBundle messages) {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(Collections.singletonList(
                                InlineKeyboardButton.builder()
                                        .text(messages.getString(Text.ADD_THIS_DICTIONARY.getValue()))
                                        .callbackData(Callback.ADD_DICTIONARY.getValue())
                                        .build()
                        )
                )
                .keyboardRow(Collections.singletonList(
                                InlineKeyboardButton.builder()
                                        .text(messages.getString(Text.START_QUIZ.getValue()))
                                        .callbackData(Callback.QUIZ.getValue())
                                        .build()
                        )
                )
                .build();
    }

    private Map<Language, UserDictionary> initializeDictionariesOfTheDay() {
        Map<Language, UserDictionary> dictionaries = new EnumMap<>(Language.class);
        Arrays.stream(Language.values()).filter(language -> !language.equals(Language.RUSSIAN)).forEach(language -> {
            // TODO Magic string
            UserDictionary userDictionary = new UserDictionary("Словарь дня", Language.RUSSIAN, language);

            for (int i = 0; i < DICTIONARY_OF_THE_DAY_WORDS_COUNT; i++) {
                String languageCode = "ru"; // TODO !!! ВЗЯТЬ ИЗ ПОЛЬЗОВАТЕЛЯ
                Optional<Word> word = yandexDictionary.translate(WordGenerator.getRandomWord(language), languageCode, language, Language.RUSSIAN);
                word.ifPresent(value -> userDictionary.getWords().add(new UserWord(value)));
            }

            dictionaries.put(language, userDictionary);
        });
        return dictionaries;
    }
}
