package world.bentobox.challenges.listeners;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import world.bentobox.bentobox.api.events.island.IslandCreatedEvent;
import world.bentobox.bentobox.api.events.island.IslandRegisteredEvent;
import world.bentobox.bentobox.api.events.island.IslandResettedEvent;
import world.bentobox.bentobox.api.events.team.TeamKickEvent;
import world.bentobox.bentobox.api.events.team.TeamLeaveEvent;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.config.Settings;
import world.bentobox.challenges.managers.ChallengesManager;

/**
 * Tests for {@link ResetListener}.
 */
class ResetListenerTest {

    @Mock
    private ChallengesAddon addon;
    @Mock
    private ChallengesManager manager;
    @Mock
    private Settings settings;
    @Mock
    private Island island;
    @Mock
    private Location location;
    @Mock
    private World world;

    private ResetListener listener;
    private AutoCloseable closeable;

    private static final UUID OWNER_UUID = UUID.randomUUID();
    private static final UUID PLAYER_UUID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        when(addon.getChallengesManager()).thenReturn(manager);
        when(addon.getChallengesSettings()).thenReturn(settings);
        when(island.getOwner()).thenReturn(OWNER_UUID);
        when(location.getWorld()).thenReturn(world);
        listener = new ResetListener(addon);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    // ---------------------------------------------------------------
    // onIslandCreated
    // ---------------------------------------------------------------

    @Test
    void testOnIslandCreated_resetEnabled_callsReset() {
        when(settings.isResetChallenges()).thenReturn(true);
        IslandCreatedEvent event = new IslandCreatedEvent(island, OWNER_UUID, false, location);

        listener.onIslandCreated(event);

        verify(manager).resetAllChallenges(OWNER_UUID, world, OWNER_UUID);
    }

    @Test
    void testOnIslandCreated_resetDisabled_noReset() {
        when(settings.isResetChallenges()).thenReturn(false);
        IslandCreatedEvent event = new IslandCreatedEvent(island, OWNER_UUID, false, location);

        listener.onIslandCreated(event);

        verify(manager, never()).resetAllChallenges(OWNER_UUID, world, OWNER_UUID);
    }

    // ---------------------------------------------------------------
    // onIslandResetted — only resets when NOT storing as island data
    // ---------------------------------------------------------------

    @Test
    void testOnIslandResetted_resetEnabledPerPlayer_callsReset() {
        when(settings.isResetChallenges()).thenReturn(true);
        when(settings.isStoreAsIslandData()).thenReturn(false);
        // Mock the event directly to avoid IslandResettedEvent copying the old island
        IslandResettedEvent event = Mockito.mock(IslandResettedEvent.class);
        when(event.getOwner()).thenReturn(OWNER_UUID);
        when(event.getLocation()).thenReturn(location);

        listener.onIslandResetted(event);

        verify(manager).resetAllChallenges(OWNER_UUID, world, OWNER_UUID);
    }

    @Test
    void testOnIslandResetted_resetEnabledIslandData_noReset() {
        when(settings.isResetChallenges()).thenReturn(true);
        when(settings.isStoreAsIslandData()).thenReturn(true);
        IslandResettedEvent event = Mockito.mock(IslandResettedEvent.class);

        listener.onIslandResetted(event);

        verify(manager, never()).resetAllChallenges(OWNER_UUID, world, OWNER_UUID);
    }

    @Test
    void testOnIslandResetted_resetDisabled_noReset() {
        when(settings.isResetChallenges()).thenReturn(false);
        IslandResettedEvent event = Mockito.mock(IslandResettedEvent.class);

        listener.onIslandResetted(event);

        verify(manager, never()).resetAllChallenges(OWNER_UUID, world, OWNER_UUID);
    }

    // ---------------------------------------------------------------
    // onIslandRegistered
    // ---------------------------------------------------------------

    @Test
    void testOnIslandRegistered_resetEnabledPerPlayer_callsReset() {
        when(settings.isResetChallenges()).thenReturn(true);
        when(settings.isStoreAsIslandData()).thenReturn(false);
        IslandRegisteredEvent event = new IslandRegisteredEvent(island, OWNER_UUID, false, location);

        listener.onIslandRegistered(event);

        verify(manager).resetAllChallenges(OWNER_UUID, world, OWNER_UUID);
    }

    @Test
    void testOnIslandRegistered_resetEnabledIslandData_noReset() {
        when(settings.isResetChallenges()).thenReturn(true);
        when(settings.isStoreAsIslandData()).thenReturn(true);
        IslandRegisteredEvent event = new IslandRegisteredEvent(island, OWNER_UUID, false, location);

        listener.onIslandRegistered(event);

        verify(manager, never()).resetAllChallenges(OWNER_UUID, world, OWNER_UUID);
    }

    @Test
    void testOnIslandRegistered_resetDisabled_noReset() {
        when(settings.isResetChallenges()).thenReturn(false);
        IslandRegisteredEvent event = new IslandRegisteredEvent(island, OWNER_UUID, false, location);

        listener.onIslandRegistered(event);

        verify(manager, never()).resetAllChallenges(OWNER_UUID, world, OWNER_UUID);
    }

    // ---------------------------------------------------------------
    // onTeamLeave — resets the leaving player (not the owner)
    // ---------------------------------------------------------------

    @Test
    void testOnTeamLeave_resetEnabledPerPlayer_resetsLeavingPlayer() {
        when(settings.isResetChallenges()).thenReturn(true);
        when(settings.isStoreAsIslandData()).thenReturn(false);
        TeamLeaveEvent event = new TeamLeaveEvent(island, PLAYER_UUID, false, location);

        listener.onTeamLeave(event);

        verify(manager).resetAllChallenges(PLAYER_UUID, world, OWNER_UUID);
    }

    @Test
    void testOnTeamLeave_resetEnabledIslandData_noReset() {
        when(settings.isResetChallenges()).thenReturn(true);
        when(settings.isStoreAsIslandData()).thenReturn(true);
        TeamLeaveEvent event = new TeamLeaveEvent(island, PLAYER_UUID, false, location);

        listener.onTeamLeave(event);

        verify(manager, never()).resetAllChallenges(PLAYER_UUID, world, OWNER_UUID);
    }

    @Test
    void testOnTeamLeave_resetDisabled_noReset() {
        when(settings.isResetChallenges()).thenReturn(false);
        TeamLeaveEvent event = new TeamLeaveEvent(island, PLAYER_UUID, false, location);

        listener.onTeamLeave(event);

        verify(manager, never()).resetAllChallenges(PLAYER_UUID, world, OWNER_UUID);
    }

    // ---------------------------------------------------------------
    // onTeamKick — resets the kicked player (not the owner)
    // ---------------------------------------------------------------

    @Test
    void testOnTeamKick_resetEnabledPerPlayer_resetsKickedPlayer() {
        when(settings.isResetChallenges()).thenReturn(true);
        when(settings.isStoreAsIslandData()).thenReturn(false);
        TeamKickEvent event = new TeamKickEvent(island, PLAYER_UUID, false, location);

        listener.onTeamKick(event);

        verify(manager).resetAllChallenges(PLAYER_UUID, world, OWNER_UUID);
    }

    @Test
    void testOnTeamKick_resetEnabledIslandData_noReset() {
        when(settings.isResetChallenges()).thenReturn(true);
        when(settings.isStoreAsIslandData()).thenReturn(true);
        TeamKickEvent event = new TeamKickEvent(island, PLAYER_UUID, false, location);

        listener.onTeamKick(event);

        verify(manager, never()).resetAllChallenges(PLAYER_UUID, world, OWNER_UUID);
    }

    @Test
    void testOnTeamKick_resetDisabled_noReset() {
        when(settings.isResetChallenges()).thenReturn(false);
        TeamKickEvent event = new TeamKickEvent(island, PLAYER_UUID, false, location);

        listener.onTeamKick(event);

        verify(manager, never()).resetAllChallenges(PLAYER_UUID, world, OWNER_UUID);
    }
}
