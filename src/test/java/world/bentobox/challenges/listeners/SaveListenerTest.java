package world.bentobox.challenges.listeners;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.managers.ChallengesManager;

/**
 * Tests for {@link SaveListener}.
 */
class SaveListenerTest {

    @Mock
    private ChallengesAddon addon;
    @Mock
    private ChallengesManager manager;
    @Mock
    private Player player;

    private SaveListener listener;
    private AutoCloseable closeable;

    private static final UUID PLAYER_UUID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        when(addon.getChallengesManager()).thenReturn(manager);
        when(player.getUniqueId()).thenReturn(PLAYER_UUID);
        listener = new SaveListener(addon);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testOnPlayerKickEvent_removesPlayerFromCache() {
        PlayerKickEvent event = Mockito.mock(PlayerKickEvent.class);
        when(event.getPlayer()).thenReturn(player);

        listener.onPlayerKickEvent(event);

        verify(manager).removeFromCache(PLAYER_UUID);
    }

    @Test
    void testOnPlayerQuitEvent_removesPlayerFromCache() {
        PlayerQuitEvent event = Mockito.mock(PlayerQuitEvent.class);
        when(event.getPlayer()).thenReturn(player);

        listener.onPlayerQuitEvent(event);

        verify(manager).removeFromCache(PLAYER_UUID);
    }

    @Test
    void testOnPlayerKickEvent_differentPlayers() {
        UUID otherUuid = UUID.randomUUID();
        Player otherPlayer = Mockito.mock(Player.class);
        when(otherPlayer.getUniqueId()).thenReturn(otherUuid);

        PlayerKickEvent event = Mockito.mock(PlayerKickEvent.class);
        when(event.getPlayer()).thenReturn(otherPlayer);

        listener.onPlayerKickEvent(event);

        verify(manager).removeFromCache(otherUuid);
    }
}
