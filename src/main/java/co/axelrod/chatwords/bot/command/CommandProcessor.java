package co.axelrod.chatwords.bot.command;

import co.axelrod.chatwords.bot.analytics.Analytics;
import co.axelrod.chatwords.bot.analytics.UserEvent;
import co.axelrod.chatwords.bot.i18n.Text;
import co.axelrod.chatwords.bot.model.UserContext;
import co.axelrod.chatwords.bot.service.SenderService;
import co.axelrod.chatwords.dictionary.model.Language;
import co.axelrod.chatwords.service.NotifierService;
import co.axelrod.chatwords.service.UserService;
import co.axelrod.chatwords.service.model.ServiceResponse;
import co.axelrod.chatwords.storage.User;
import co.axelrod.chatwords.storage.UserRepository;
import co.axelrod.chatwords.storage.story.UserStory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class CommandProcessor<E extends UserStory> {
    protected final Analytics analytics;
    protected final UserService userService;
    protected final SenderService senderService;
    protected final UserRepository userRepository;
    protected final NotifierService notifierService;

    protected CommandProcessor(
            Analytics analytics,
            UserService userService,
            SenderService senderService,
            UserRepository userRepository,
            NotifierService notifierService
    ) {
        this.analytics = analytics;
        this.userService = userService;
        this.senderService = senderService;
        this.userRepository = userRepository;
        this.notifierService = notifierService;
    }

    public void process() {
        try {
            log.info("From " + UserContext.getUser().getName() + ": " + UserContext.getRequest());

            if (!processGuards()) {
                return;
            }

            setDictionaryIfEmpty();
            clearPaging();
            retrieveStory();
            processRequest();
            notifyUser();
        } catch (Exception ex) {
            processException(ex);
        }
    }

    private void retrieveStory() {
        if (getStory() != null) {
            // Create or update story
        }
    }

    private boolean processGuards() {
        // In story guard
        if (UserContext.getUser().inStory() && getStory() == null
                && !UserContext.getRequest().equals(Command.HOME.getValue())
                && !UserContext.getRequest().equals(Command.START.getValue())
        ) {
            analytics.log(UserContext.getUser(), UserEvent.USER_TRYING_TO_SWITCH_STORY_WHILE_BEING_IN_ANOTHER_STORY);
            senderService.sendMessage(Text.ACTION_UNAVAILABLE);
            return false;
        }

        return true;
    }

    private void clearPaging() {
        if (!(UserContext.getRequest().equals(Callback.NEXT_PAGE.getValue())
                || UserContext.getRequest().equals(Callback.PREVIOUS_PAGE.getValue())
                || UserContext.getRequest().equals(Callback.CHANGE_DIRECTION.getValue())
                || UserContext.getRequest().equals(Callback.CHANGE_DIRECTION_DETAILED.getValue())
        )) {
            UserContext.getUser().setPaging(null);
        }
    }

    private void setDictionaryIfEmpty() {
        if (UserContext.getUser().getCurrentDictionary() == null) {
            if (!UserContext.getUser().getDictionaries().isEmpty()) {
                if (
                        UserContext.getUser().getDictionaries()
                                .values()
                                .stream()
                                .anyMatch(userDictionary -> userDictionary.getTargetLanguage().equals(Language.ENGLISH))) {
                    UserContext.getUser().setCurrentDictionary(UserContext.getUser().getDictionaries()
                            .values()
                            .stream()
                            .filter(userDictionary -> userDictionary.getTargetLanguage().equals(Language.ENGLISH))
                            .findFirst().get());
                } else {
                    UserContext.getUser().setCurrentDictionary(UserContext.getUser().getDictionaries().values().stream().findFirst().get());
                }
            }
        }
    }

    private void notifyUser() {
        ServiceResponse notificationForUser = notifierService.getNowWeCanTranslate(UserContext.getUser());
        if (!notificationForUser.isSuccess()) {
            analytics.log(notificationForUser.getUserEvent());
            senderService.sendMessage(notificationForUser.getText());
        }
    }

    private void processException(Exception ex) {
        log.error(ex.getMessage(), ex);

        User user = new User();
        user.setId(UserContext.getChatId().toString());

        analytics.log(user, UserEvent.INTERNAL_SERVER_ERROR);
        senderService.sendUnknownError();
    }

    protected abstract void processRequest();

    protected abstract E getStory();
}
