package world.bentobox.challenges.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.managers.ChallengesManager;
import world.bentobox.challenges.utils.Constants;

/**
 * Tests for {@link CompletedChallengesRequestHandler}.
 */
class CompletedChallengesRequestHandlerTest {

    @Mock
    private ChallengesAddon addon;
    @Mock
    private ChallengesManager manager;
    @Mock
    private World world;

    private CompletedChallengesRequestHandler handler;
    private AutoCloseable closeable;
    private MockedStatic<Bukkit> mockedBukkit;

    private static final UUID PLAYER_UUID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        when(addon.getChallengesManager()).thenReturn(manager);
        mockedBukkit = Mockito.mockStatic(Bukkit.class);
        handler = new CompletedChallengesRequestHandler(addon);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockedBukkit.close();
        closeable.close();
    }

    @Test
    void testHandleNull() {
        Object result = handler.handle(null);
        assertTrue(((Set<?>) result).isEmpty());
    }

    @Test
    void testHandleEmptyMap() {
        Object result = handler.handle(Collections.emptyMap());
        assertTrue(((Set<?>) result).isEmpty());
    }

    @Test
    void testHandleMissingWorldKey() {
        Object result = handler.handle(Map.of("player", PLAYER_UUID));
        assertTrue(((Set<?>) result).isEmpty());
    }

    @Test
    void testHandleMissingPlayerKey() {
        Object result = handler.handle(Map.of(Constants.WORLD_NAME_KEY, "world"));
        assertTrue(((Set<?>) result).isEmpty());
    }

    @Test
    void testHandleWrongPlayerType() {
        Object result = handler.handle(Map.of(
                Constants.WORLD_NAME_KEY, "world",
                "player", "not-a-uuid"));
        assertTrue(((Set<?>) result).isEmpty());
    }

    @Test
    void testHandleUnknownWorld() {
        mockedBukkit.when(() -> Bukkit.getWorld("nonexistent")).thenReturn(null);
        Object result = handler.handle(Map.of(
                Constants.WORLD_NAME_KEY, "nonexistent",
                "player", PLAYER_UUID));
        assertTrue(((Set<?>) result).isEmpty());
    }

    @Test
    void testHandleValidRequestNoCompletedChallenges() {
        mockedBukkit.when(() -> Bukkit.getWorld("world")).thenReturn(world);
        when(manager.getAllChallengesNames(world)).thenReturn(List.of("bskyblock_ch1", "bskyblock_ch2"));
        when(manager.isChallengeComplete(PLAYER_UUID, world, "bskyblock_ch1")).thenReturn(false);
        when(manager.isChallengeComplete(PLAYER_UUID, world, "bskyblock_ch2")).thenReturn(false);

        @SuppressWarnings("unchecked")
        Set<String> result = (Set<String>) handler.handle(Map.of(
                Constants.WORLD_NAME_KEY, "world",
                "player", PLAYER_UUID));

        assertTrue(result.isEmpty());
    }

    @Test
    void testHandleValidRequestSomeCompletedChallenges() {
        mockedBukkit.when(() -> Bukkit.getWorld("world")).thenReturn(world);
        when(manager.getAllChallengesNames(world)).thenReturn(List.of("bskyblock_ch1", "bskyblock_ch2"));
        when(manager.isChallengeComplete(PLAYER_UUID, world, "bskyblock_ch1")).thenReturn(true);
        when(manager.isChallengeComplete(PLAYER_UUID, world, "bskyblock_ch2")).thenReturn(false);

        @SuppressWarnings("unchecked")
        Set<String> result = (Set<String>) handler.handle(Map.of(
                Constants.WORLD_NAME_KEY, "world",
                "player", PLAYER_UUID));

        assertEquals(1, result.size());
        assertTrue(result.contains("bskyblock_ch1"));
    }

    @Test
    void testHandleValidRequestAllCompleted() {
        mockedBukkit.when(() -> Bukkit.getWorld("world")).thenReturn(world);
        when(manager.getAllChallengesNames(world)).thenReturn(List.of("bskyblock_ch1", "bskyblock_ch2"));
        when(manager.isChallengeComplete(PLAYER_UUID, world, "bskyblock_ch1")).thenReturn(true);
        when(manager.isChallengeComplete(PLAYER_UUID, world, "bskyblock_ch2")).thenReturn(true);

        @SuppressWarnings("unchecked")
        Set<String> result = (Set<String>) handler.handle(Map.of(
                Constants.WORLD_NAME_KEY, "world",
                "player", PLAYER_UUID));

        assertEquals(2, result.size());
    }

    @Test
    void testHandleValidWorldNoChallengesInWorld() {
        mockedBukkit.when(() -> Bukkit.getWorld("world")).thenReturn(world);
        when(manager.getAllChallengesNames(world)).thenReturn(Collections.emptyList());

        @SuppressWarnings("unchecked")
        Set<String> result = (Set<String>) handler.handle(Map.of(
                Constants.WORLD_NAME_KEY, "world",
                "player", PLAYER_UUID));

        assertTrue(result.isEmpty());
    }

    @Test
    void testHandlerLabel() {
        assertEquals("completed-challenges", handler.getLabel());
    }
}
