package co.axelrod.chatwords.service;

import co.axelrod.chatwords.bot.analytics.UserEvent;
import co.axelrod.chatwords.bot.i18n.Text;
import co.axelrod.chatwords.service.model.ServiceResponse;
import co.axelrod.chatwords.storage.User;
import co.axelrod.chatwords.storage.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotifierService {
    private final UserRepository userRepository;

    public ServiceResponse getNowWeCanTranslate(User user) {
        if (user.getUserNotifiedAboutTranslation() == null) {
            user.setUserNotifiedAboutTranslation(true);
            userRepository.save(user);

            return new ServiceResponse(Text.YOU_CAN_USE_TRANSLATE_SENTENCES_NOW, UserEvent.YOU_CAN_USE_TRANSLATE_SENTENCES_NOW);
        }

        return new ServiceResponse();
    }
}
