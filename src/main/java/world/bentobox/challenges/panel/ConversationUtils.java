//
// Created by BONNe
// Copyright - 2020
//


package world.bentobox.challenges.panel;


import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.kyori.adventure.text.Component;
import world.bentobox.bentobox.api.prompts.Prompts;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;
import world.bentobox.challenges.utils.Constants;
import world.bentobox.challenges.utils.Utils;


/**
 * Asks a player for typed input. These methods keep the shape they had when they were built on Bukkit's
 * conversation API, which Paper is removing, and now hand the work to BentoBox's {@link Prompts}: the player
 * gets a dialog with a text box where the server supports dialogs, and is asked in chat otherwise.
 */
public class ConversationUtils
{
    /**
     * The heading of every prompt. The question itself is shown under it.
     */
    private static final Component TITLE = Component.text("Challenges");


    /**
     * Utility class. Not for instantiation.
     */
    private ConversationUtils()
    {
    }


    /**
     * Closes the open gui and asks the player to confirm something.
     *
     * @param consumer Receives true if the player confirmed, false if they declined or did not answer.
     * @param user User who is asked.
     * @param question What the player is asked to confirm, already translated.
     * @param successMessage Message sent after the player confirms, or null for none.
     */
    public static void createConfirmation(Consumer<Boolean> consumer,
        User user,
        @NotNull String question,
        @Nullable String successMessage)
    {
        user.closeInventory();
        Prompts.confirm(user).
            title(TITLE).
            message(Util.parseMiniMessageOrLegacy(question)).
            onConfirm(() ->
            {
                consumer.accept(true);
                ConversationUtils.send(user, successMessage);
            }).
            onCancel(() -> consumer.accept(false)).
            open();
    }


    /**
     * Closes the open gui and asks the player for a name that will be turned into a unique id. The answer is
     * asked for again until the validation accepts it. Be aware, the consumer receives the text as typed, not
     * sanitized; only the failure message shows the sanitized id.
     *
     * @param consumer Receives the text, or null if the player cancelled or did not answer.
     * @param validation Returns true if the typed text is acceptable.
     * @param user User who is asked.
     * @param question What the player is asked, already translated.
     * @param successMessage Message sent after the text is accepted, or null for none.
     * @param failTranslationLocation Locale key of the message shown when the validation refuses the text. It
     * is given the sanitized id as [id].
     */
    public static void createIDStringInput(Consumer<String> consumer,
        Predicate<String> validation,
        User user,
        @NotNull String question,
        @Nullable String successMessage,
        @Nullable String failTranslationLocation)
    {
        user.closeInventory();
        Prompts.text(user).
            title(TITLE).
            message(Util.parseMiniMessageOrLegacy(question)).
            validate(input -> validation.test(input) ? null :
                ConversationUtils.failMessage(user, failTranslationLocation, input)).
            onSubmit(input ->
            {
                consumer.accept(input);
                ConversationUtils.send(user, successMessage);
            }).
            onCancel(() -> consumer.accept(null)).
            open();
    }


    /**
     * Closes the open gui and asks the player for a number.
     *
     * @param consumer Receives the number, or null if the player cancelled or did not answer.
     * @param user User who is asked.
     * @param question What the player is asked, already translated.
     * @param minValue The smallest number accepted.
     * @param maxValue The largest number accepted.
     */
    public static void createNumericInput(Consumer<Number> consumer,
        @NotNull User user,
        @NotNull String question,
        Number minValue,
        Number maxValue)
    {
        user.closeInventory();
        Prompts.decimal(user, minValue.doubleValue(), maxValue.doubleValue()).
            title(TITLE).
            message(Util.parseMiniMessageOrLegacy(question)).
            onSubmit(consumer::accept).
            onCancel(() -> consumer.accept(null)).
            open();
    }


    /**
     * Closes the open gui and asks the player for several lines of text. Color codes written with an ampersand
     * are translated; MiniMessage tags are kept as typed.
     *
     * @param consumer Receives the lines, or null if the player cancelled or did not answer.
     * @param user User who is asked.
     * @param question What the player is asked, already translated.
     * @param successMessage Message sent after the lines are accepted.
     */
    public static void createStringListInput(Consumer<List<String>> consumer,
        User user,
        @NotNull String question,
        @NotNull String successMessage)
    {
        user.closeInventory();
        Prompts.lines(user).
            title(TITLE).
            message(Util.parseMiniMessageOrLegacy(question)).
            onSubmit(lines ->
            {
                consumer.accept(lines.stream().
                    map(line -> ChatColor.translateAlternateColorCodes('&', line)).
                    toList());
                ConversationUtils.send(user, successMessage);
            }).
            onCancel(() -> consumer.accept(null)).
            open();
    }


    /**
     * Closes the open gui and asks the player for one line of text.
     *
     * @param consumer Receives the text, or null if the player cancelled or did not answer.
     * @param user User who is asked.
     * @param question What the player is asked, already translated.
     * @param successMessage Message sent after the text is accepted, or null for none.
     */
    public static void createStringInput(Consumer<String> consumer,
        User user,
        @NotNull String question,
        @Nullable String successMessage)
    {
        user.closeInventory();
        Prompts.text(user).
            title(TITLE).
            message(Util.parseMiniMessageOrLegacy(question)).
            onSubmit(text ->
            {
                consumer.accept(text);
                ConversationUtils.send(user, successMessage);
            }).
            onCancel(() -> consumer.accept(null)).
            open();
    }


    /**
     * @return the message for an id that was refused. Never null, because null would mean the id was accepted.
     */
    private static String failMessage(User user, @Nullable String failTranslationLocation, String input)
    {
        if (failTranslationLocation == null)
        {
            return user.getTranslation(Constants.CONVERSATIONS + "object-already-exists",
                Constants.PARAMETER_ID,
                Utils.sanitizeInput(input));
        }

        return user.getTranslation(failTranslationLocation,
            Constants.PARAMETER_ID,
            Utils.sanitizeInput(input));
    }


    private static void send(User user, @Nullable String message)
    {
        if (message != null && !message.isBlank())
        {
            user.sendRawMessage(message);
        }
    }
}
