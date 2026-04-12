package world.bentobox.challenges.panel;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.user.User;

/**
 * Tests for {@link ConversationUtils} conversation creation methods.
 * ConversationFactory gets the server from the Plugin instance.
 * BentoBox extends JavaPlugin where getServer() is final, so we set the
 * Bukkit.server field to a mock server whose getScheduler() etc. are stubbed.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ConversationUtilsTest {

    @Mock
    private User user;
    @Mock
    private Player player;
    @Mock
    private Server server;
    @Mock
    private BukkitScheduler scheduler;
    @Mock
    private PluginManager pluginManager;

    private BentoBox previousInstance;
    private Server previousServer;

    @BeforeEach
    public void setUp() throws Exception {
        when(user.getTranslation(anyString())).thenAnswer(
            (Answer<String>) inv -> inv.getArgument(0, String.class));
        when(user.getPlayer()).thenReturn(player);
        when(user.getUniqueId()).thenReturn(UUID.randomUUID());

        // Set up server mock for ConversationFactory (which calls Plugin.getServer())
        when(server.getScheduler()).thenReturn(scheduler);
        when(server.getPluginManager()).thenReturn(pluginManager);
        previousServer = Bukkit.getServer();
        PanelTestHelper.setServer(server);

        // BentoBox.getInstance() must return a real-ish instance.
        // getServer() is final from JavaPlugin, so we set the server field via reflection.
        BentoBox bentoBox = mock(BentoBox.class);
        previousInstance = BentoBox.getInstance();
        PanelTestHelper.setBentoBoxInstance(bentoBox);
        PanelTestHelper.setPluginServer(bentoBox, server);
    }

    @AfterEach
    public void tearDown() throws Exception {
        PanelTestHelper.setBentoBoxInstance(previousInstance);
        PanelTestHelper.setServer(previousServer);
    }

    @Test
    public void testCreateConfirmation() {
        @SuppressWarnings("unchecked")
        Consumer<Boolean> consumer = mock(Consumer.class);
        ConversationUtils.createConfirmation(consumer, user, "Are you sure?", "Done!");
        verify(user).getPlayer();
    }

    @Test
    public void testCreateConfirmationWithNullSuccessMessage() {
        @SuppressWarnings("unchecked")
        Consumer<Boolean> consumer = mock(Consumer.class);
        ConversationUtils.createConfirmation(consumer, user, "Are you sure?", null);
        verify(user).getPlayer();
    }

    @Test
    public void testCreateIDStringInput() {
        @SuppressWarnings("unchecked")
        Consumer<String> consumer = mock(Consumer.class);
        Function<String, Boolean> validation = input -> true;
        ConversationUtils.createIDStringInput(consumer, validation, user,
            "Enter ID:", "Created!", "challenges.conversations.object-already-exists");
        verify(user).getPlayer();
    }

    @Test
    public void testCreateNumericInput() {
        @SuppressWarnings("unchecked")
        Consumer<Number> consumer = mock(Consumer.class);
        ConversationUtils.createNumericInput(consumer, user, "Enter number:", 1, 100);
        verify(user).getPlayer();
    }

    @Test
    public void testCreateNumericInputWithBounds() {
        @SuppressWarnings("unchecked")
        Consumer<Number> consumer = mock(Consumer.class);
        ConversationUtils.createNumericInput(consumer, user, "Enter:", 0, 2000);
        verify(user).getPlayer();
    }

    @Test
    public void testCreateStringInput() {
        @SuppressWarnings("unchecked")
        Consumer<String> consumer = mock(Consumer.class);
        ConversationUtils.createStringInput(consumer, user, "Enter text:", "Saved!");
        verify(user).getPlayer();
    }

    @Test
    public void testCreateStringInputWithNullSuccess() {
        @SuppressWarnings("unchecked")
        Consumer<String> consumer = mock(Consumer.class);
        ConversationUtils.createStringInput(consumer, user, "Enter text:", null);
        verify(user).getPlayer();
    }

    @Test
    public void testCreateStringListInput() {
        @SuppressWarnings("unchecked")
        Consumer<List<String>> consumer = mock(Consumer.class);
        ConversationUtils.createStringListInput(consumer, user, "Enter lines:", "Saved!");
        verify(user).getPlayer();
    }
}
