package world.bentobox.challenges.panel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.Settings;
import world.bentobox.bentobox.api.prompts.ChatInputListener;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.WhiteBox;

/**
 * Tests that {@link ConversationUtils} hands its questions to BentoBox's prompts and passes the answers back
 * the way its callers expect: the value on success, and null (or false for a confirmation) when the player
 * backs out.
 * <p>
 * Dialogs cannot be built without a server, so they are switched off here and every prompt is answered in
 * chat. Translations come back as their locale key, so the cancel, done and confirm words are the keys.
 */
class ConversationUtilsTest {

    private static final String CANCEL = "prompts.chat.cancel-word";
    private static final String EXISTS = "challenges.conversations.object-already-exists";

    @Mock
    private User user;
    @Mock
    private Player player;
    @Mock
    private BukkitScheduler sch;

    private BentoBox bentoBox;
    private UUID uuid;
    private AutoCloseable closeable;
    private MockedStatic<Bukkit> mockedBukkit;

    private final ChatInputListener listener = new ChatInputListener();
    private final List<Object> answers = new ArrayList<>();

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        ServerMock mbServer = MockBukkit.mock();
        PanelTestHelper.primeBukkitRegistry();

        uuid = UUID.randomUUID();
        Answer<String> key = inv -> inv.getArgument(0, String.class);
        when(user.getTranslation(anyString())).thenAnswer(key);
        when(user.getTranslation(anyString(), any(String[].class))).thenAnswer(key);
        when(user.getPlayer()).thenReturn(player);
        when(user.isPlayer()).thenReturn(true);
        when(user.getUniqueId()).thenReturn(uuid);
        when(player.getUniqueId()).thenReturn(uuid);

        bentoBox = mock(BentoBox.class);
        WhiteBox.setInternalState(BentoBox.class, "instance", bentoBox);
        Settings settings = new Settings();
        settings.setDialogPrompts(false);
        when(bentoBox.getSettings()).thenReturn(settings);

        mockedBukkit = Mockito.mockStatic(Bukkit.class, Mockito.RETURNS_DEEP_STUBS);
        mockedBukkit.when(Bukkit::getServer).thenReturn(mbServer);
        mockedBukkit.when(Bukkit::getScheduler).thenReturn(sch);
        mockedBukkit.when(Bukkit::getPluginManager).thenReturn(mbServer.getPluginManager());
    }

    @AfterEach
    void tearDown() throws Exception {
        // Forget any prompt still waiting, so one test cannot leak into the next
        PlayerQuitEvent quit = mock(PlayerQuitEvent.class);
        when(quit.getPlayer()).thenReturn(player);
        listener.onQuit(quit);
        if (mockedBukkit != null) mockedBukkit.closeOnDemand();
        if (closeable != null) closeable.close();
        MockBukkit.unmock();
        Mockito.framework().clearInlineMocks();
    }

    /**
     * Sends a chat message as the player and runs the task that carries it to the main thread.
     */
    private void say(String text) {
        AsyncChatEvent chat = mock(AsyncChatEvent.class);
        when(chat.getPlayer()).thenReturn(player);
        when(chat.message()).thenReturn(Component.text(text));
        listener.onChat(chat);
        ArgumentCaptor<Runnable> task = ArgumentCaptor.forClass(Runnable.class);
        verify(sch, atLeastOnce()).runTask(eq(bentoBox), task.capture());
        task.getValue().run();
    }

    private boolean waiting() {
        return ChatInputListener.isWaitingFor(uuid);
    }

    // ------------------------------------------------------------------
    // Confirmation
    // ------------------------------------------------------------------

    @Test
    void testCreateConfirmation() {
        ConversationUtils.createConfirmation(answers::add, user, "Are you sure?", "Done!");
        verify(user).closeInventory();
        assertTrue(waiting());

        say("prompts.chat.confirm-word");

        assertEquals(List.of(true), answers);
        verify(user).sendRawMessage("Done!");
        assertFalse(waiting());
    }

    @Test
    void testCreateConfirmationDeclined() {
        ConversationUtils.createConfirmation(answers::add, user, "Are you sure?", "Done!");

        say("no");

        assertEquals(List.of(false), answers);
        verify(user, never()).sendRawMessage("Done!");
    }

    @Test
    void testCreateConfirmationWithNullSuccessMessage() {
        ConversationUtils.createConfirmation(answers::add, user, "Are you sure?", null);

        say("prompts.chat.confirm-word");

        assertEquals(List.of(true), answers);
        verify(user, never()).sendRawMessage(any());
    }

    // ------------------------------------------------------------------
    // ID string
    // ------------------------------------------------------------------

    /** The consumer gets the text as typed; sanitizing it is the caller's job */
    @Test
    void testCreateIDStringInput() {
        ConversationUtils.createIDStringInput(answers::add, input -> true, user, "Enter ID:", "Created!", EXISTS);

        say("My New-Challenge");

        assertEquals(List.of("My New-Challenge"), answers);
        verify(user).sendRawMessage("Created!");
        assertFalse(waiting());
    }

    /** A refused id is explained, with the sanitized id, and asked for again */
    @Test
    void testCreateIDStringInputRefusedThenAccepted() {
        ConversationUtils.createIDStringInput(answers::add, input -> !input.equalsIgnoreCase("Taken Name"), user,
            "Enter ID:", "Created!", EXISTS);

        say("Taken Name");

        assertTrue(answers.isEmpty());
        assertTrue(waiting());
        verify(user).getTranslation(EXISTS, "[id]", "taken_name");
        verify(user).sendRawMessage(EXISTS);
        verify(user, never()).sendRawMessage("Created!");

        say("Free Name");

        assertEquals(List.of("Free Name"), answers);
        verify(user).sendRawMessage("Created!");
    }

    @Test
    void testCreateIDStringInputCancelGivesNull() {
        ConversationUtils.createIDStringInput(answers::add, input -> true, user, "Enter ID:", "Created!", EXISTS);

        say(CANCEL);

        assertEquals(1, answers.size());
        assertNull(answers.getFirst());
        assertFalse(waiting());
    }

    // ------------------------------------------------------------------
    // Number
    // ------------------------------------------------------------------

    @Test
    void testCreateNumericInput() {
        ConversationUtils.createNumericInput(answers::add, user, "Enter number:", 1, 100);

        say("lots");
        say("101");
        assertTrue(answers.isEmpty());
        assertTrue(waiting());

        say("42");

        assertEquals(1, answers.size());
        assertEquals(42, ((Number) answers.getFirst()).intValue());
    }

    @Test
    void testCreateNumericInputCancelGivesNull() {
        ConversationUtils.createNumericInput(answers::add, user, "Enter:", 0, 2000);

        say(CANCEL);

        assertEquals(1, answers.size());
        assertNull(answers.getFirst());
    }

    // ------------------------------------------------------------------
    // Text
    // ------------------------------------------------------------------

    @Test
    void testCreateStringInput() {
        ConversationUtils.createStringInput(answers::add, user, "Enter text:", "Saved!");

        say("hello world");

        assertEquals(List.of("hello world"), answers);
        verify(user).sendRawMessage("Saved!");
    }

    @Test
    void testCreateStringInputWithNullSuccess() {
        ConversationUtils.createStringInput(answers::add, user, "Enter text:", null);

        say("hello");

        assertEquals(List.of("hello"), answers);
        verify(user, never()).sendRawMessage(any());
    }

    @Test
    void testCreateStringInputCancelGivesNull() {
        ConversationUtils.createStringInput(answers::add, user, "Enter text:", "Saved!");

        say(CANCEL);

        assertEquals(1, answers.size());
        assertNull(answers.getFirst());
        verify(user, never()).sendRawMessage("Saved!");
    }

    /** Ampersand codes are translated; MiniMessage tags are left for the caller */
    @Test
    void testCreateStringListInput() {
        ConversationUtils.createStringListInput(answers::add, user, "Enter lines:", "Saved!");

        say("&aFirst");
        say("<red>Second");
        assertTrue(answers.isEmpty());
        say("prompts.chat.done-word");

        assertEquals(List.of(List.of("§aFirst", "<red>Second")), answers);
        verify(user).sendRawMessage("Saved!");
    }

    @Test
    void testCreateStringListInputCancelGivesNull() {
        ConversationUtils.createStringListInput(answers::add, user, "Enter lines:", "Saved!");

        say("a line");
        say(CANCEL);

        assertEquals(1, answers.size());
        assertNull(answers.getFirst());
    }
}
