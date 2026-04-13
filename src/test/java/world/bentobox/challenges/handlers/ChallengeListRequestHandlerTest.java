package world.bentobox.challenges.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;

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
 * Tests for {@link ChallengeListRequestHandler}.
 */
class ChallengeListRequestHandlerTest {

    @Mock
    private ChallengesAddon addon;
    @Mock
    private ChallengesManager manager;
    @Mock
    private World world;

    private ChallengeListRequestHandler handler;
    private AutoCloseable closeable;
    private MockedStatic<Bukkit> mockedBukkit;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        when(addon.getChallengesManager()).thenReturn(manager);
        mockedBukkit = Mockito.mockStatic(Bukkit.class);
        handler = new ChallengeListRequestHandler(addon);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockedBukkit.close();
        closeable.close();
    }

    @Test
    void testHandleNull() {
        Object result = handler.handle(null);
        assertTrue(((List<?>) result).isEmpty());
    }

    @Test
    void testHandleEmptyMap() {
        Object result = handler.handle(Collections.emptyMap());
        assertTrue(((List<?>) result).isEmpty());
    }

    @Test
    void testHandleMissingWorldKey() {
        Object result = handler.handle(Map.of("other-key", "value"));
        assertTrue(((List<?>) result).isEmpty());
    }

    @Test
    void testHandleWrongValueType() {
        Object result = handler.handle(Map.of(Constants.WORLD_NAME_KEY, 123));
        assertTrue(((List<?>) result).isEmpty());
    }

    @Test
    void testHandleUnknownWorld() {
        mockedBukkit.when(() -> Bukkit.getWorld("nonexistent")).thenReturn(null);
        Object result = handler.handle(Map.of(Constants.WORLD_NAME_KEY, "nonexistent"));
        assertTrue(((List<?>) result).isEmpty());
    }

    @Test
    void testHandleValidWorldReturnsChallenges() {
        mockedBukkit.when(() -> Bukkit.getWorld("world")).thenReturn(world);
        when(manager.getAllChallengesNames(world)).thenReturn(List.of("bskyblock_ch1", "bskyblock_ch2"));

        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) handler.handle(Map.of(Constants.WORLD_NAME_KEY, "world"));

        assertEquals(2, result.size());
        assertTrue(result.contains("bskyblock_ch1"));
        assertTrue(result.contains("bskyblock_ch2"));
    }

    @Test
    void testHandleValidWorldEmptyChallenges() {
        mockedBukkit.when(() -> Bukkit.getWorld("world")).thenReturn(world);
        when(manager.getAllChallengesNames(world)).thenReturn(Collections.emptyList());

        Object result = handler.handle(Map.of(Constants.WORLD_NAME_KEY, "world"));

        assertTrue(((List<?>) result).isEmpty());
    }

    @Test
    void testHandlerLabel() {
        assertEquals("challenge-list", handler.getLabel());
    }
}
