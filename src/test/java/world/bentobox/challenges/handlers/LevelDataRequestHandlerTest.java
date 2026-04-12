package world.bentobox.challenges.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.database.object.ChallengeLevel;
import world.bentobox.challenges.managers.ChallengesManager;
import world.bentobox.challenges.utils.Constants;

/**
 * Tests for {@link LevelDataRequestHandler}.
 */
class LevelDataRequestHandlerTest {

    @Mock
    private ChallengesAddon addon;
    @Mock
    private ChallengesManager manager;
    @Mock
    private ChallengeLevel level;

    private LevelDataRequestHandler handler;
    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        when(addon.getChallengesManager()).thenReturn(manager);
        handler = new LevelDataRequestHandler(addon);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testHandleNull() {
        Object result = handler.handle(null);
        assertTrue(((Map<?, ?>) result).isEmpty());
    }

    @Test
    void testHandleEmptyMap() {
        Object result = handler.handle(Collections.emptyMap());
        assertTrue(((Map<?, ?>) result).isEmpty());
    }

    @Test
    void testHandleMissingLevelKey() {
        Object result = handler.handle(Map.of("other-key", "value"));
        assertTrue(((Map<?, ?>) result).isEmpty());
    }

    @Test
    void testHandleWrongValueType() {
        Object result = handler.handle(Map.of(Constants.LEVEL_NAME_KEY, 99));
        assertTrue(((Map<?, ?>) result).isEmpty());
    }

    @Test
    void testHandleUnknownLevel() {
        when(manager.getLevel("unknown")).thenReturn(null);
        Object result = handler.handle(Map.of(Constants.LEVEL_NAME_KEY, "unknown"));
        assertTrue(((Map<?, ?>) result).isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testHandleValidLevel() {
        Set<String> challenges = Set.of("bskyblock_ch1", "bskyblock_ch2");
        when(manager.getLevel("bskyblock_level1")).thenReturn(level);
        when(level.getUniqueId()).thenReturn("bskyblock_level1");
        when(level.getFriendlyName()).thenReturn("Level One");
        when(level.getIcon()).thenReturn(null);
        when(level.getOrder()).thenReturn(1);
        when(level.getUnlockMessage()).thenReturn("Unlocked!");
        when(level.getWorld()).thenReturn("world");
        when(level.getChallenges()).thenReturn(challenges);
        when(level.getWaiverAmount()).thenReturn(3);

        Map<String, Object> result = (Map<String, Object>) handler.handle(
                Map.of(Constants.LEVEL_NAME_KEY, "bskyblock_level1"));

        assertFalse(result.isEmpty());
        assertEquals("bskyblock_level1", result.get("uniqueId"));
        assertEquals("Level One", result.get("name"));
        assertEquals(1, result.get("order"));
        assertEquals("Unlocked!", result.get("message"));
        assertEquals("world", result.get("world"));
        assertEquals(challenges, result.get("challenges"));
        assertEquals(3, result.get("waiveramount"));
    }

    @Test
    void testHandlerLabel() {
        assertEquals("level-data", handler.getLabel());
    }
}
