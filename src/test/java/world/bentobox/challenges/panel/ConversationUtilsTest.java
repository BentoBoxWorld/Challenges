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
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.WhiteBox;

/**
 * Tests for {@link ConversationUtils} conversation creation methods.
 * Uses MockBukkit + mockStatic(Bukkit.class) for server infrastructure.
 */
public class ConversationUtilsTest {

    @Mock
    private User user;
    @Mock
    private Player player;

    private AutoCloseable closeable;
    private ServerMock mbServer;
    private MockedStatic<Bukkit> mockedBukkit;

    @BeforeEach
    public void setUp() throws Exception {
        closeable = MockitoAnnotations.openMocks(this);
        mbServer = MockBukkit.mock();

        when(user.getTranslation(anyString())).thenAnswer(
            (Answer<String>) inv -> inv.getArgument(0, String.class));
        when(user.getPlayer()).thenReturn(player);
        when(user.getUniqueId()).thenReturn(UUID.randomUUID());

        // BentoBox.getInstance() for ConversationFactory.
        // getServer() is final from JavaPlugin — Mockito 5 inline mock maker can stub finals.
        BentoBox bentoBox = mock(BentoBox.class);
        WhiteBox.setInternalState(BentoBox.class, "instance", bentoBox);
        when(bentoBox.getServer()).thenReturn(mbServer);

        mockedBukkit = Mockito.mockStatic(Bukkit.class, Mockito.RETURNS_DEEP_STUBS);
        mockedBukkit.when(Bukkit::getServer).thenReturn(mbServer);
        mockedBukkit.when(Bukkit::getScheduler).thenReturn(mbServer.getScheduler());
        mockedBukkit.when(Bukkit::getPluginManager).thenReturn(mbServer.getPluginManager());
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (mockedBukkit != null) mockedBukkit.closeOnDemand();
        if (closeable != null) closeable.close();
        MockBukkit.unmock();
        Mockito.framework().clearInlineMocks();
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
