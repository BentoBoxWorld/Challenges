package world.bentobox.challenges.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.database.object.Challenge;
import world.bentobox.challenges.database.object.Challenge.ChallengeType;
import world.bentobox.challenges.managers.ChallengesManager;
import world.bentobox.challenges.utils.Constants;

/**
 * Tests for {@link ChallengeDataRequestHandler}.
 */
class ChallengeDataRequestHandlerTest {

    @Mock
    private ChallengesAddon addon;
    @Mock
    private ChallengesManager manager;
    @Mock
    private Challenge challenge;

    private ChallengeDataRequestHandler handler;
    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        when(addon.getChallengesManager()).thenReturn(manager);
        handler = new ChallengeDataRequestHandler(addon);
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
    void testHandleMissingChallengeKey() {
        Object result = handler.handle(Map.of("other-key", "value"));
        assertTrue(((Map<?, ?>) result).isEmpty());
    }

    @Test
    void testHandleWrongValueType() {
        Object result = handler.handle(Map.of(Constants.CHALLENGE_NAME_KEY, 42));
        assertTrue(((Map<?, ?>) result).isEmpty());
    }

    @Test
    void testHandleUnknownChallenge() {
        when(manager.getChallenge("unknown")).thenReturn(null);
        Object result = handler.handle(Map.of(Constants.CHALLENGE_NAME_KEY, "unknown"));
        assertTrue(((Map<?, ?>) result).isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testHandleValidChallenge() {
        when(manager.getChallenge("bskyblock_ch1")).thenReturn(challenge);
        when(challenge.getUniqueId()).thenReturn("bskyblock_ch1");
        when(challenge.getFriendlyName()).thenReturn("My Challenge");
        when(challenge.getIcon()).thenReturn(null);
        when(challenge.getLevel()).thenReturn("bskyblock_level1");
        when(challenge.getOrder()).thenReturn(1);
        when(challenge.isDeployed()).thenReturn(true);
        when(challenge.getDescription()).thenReturn(List.of("A challenge"));
        when(challenge.getChallengeType()).thenReturn(ChallengeType.INVENTORY_TYPE);
        when(challenge.isRepeatable()).thenReturn(false);
        when(challenge.getMaxTimes()).thenReturn(1);

        Map<String, Object> result = (Map<String, Object>) handler.handle(
                Map.of(Constants.CHALLENGE_NAME_KEY, "bskyblock_ch1"));

        assertFalse(result.isEmpty());
        assertEquals("bskyblock_ch1", result.get("uniqueId"));
        assertEquals("My Challenge", result.get("name"));
        assertEquals("bskyblock_level1", result.get("levelId"));
        assertEquals(1, result.get("order"));
        assertEquals(true, result.get("deployed"));
        assertEquals(List.of("A challenge"), result.get("description"));
        assertEquals("INVENTORY_TYPE", result.get("type"));
        assertEquals(false, result.get("repeatable"));
        assertEquals(1, result.get("maxTimes"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testHandleRepeatableChallengeUsesMaxTimes() {
        when(manager.getChallenge("bskyblock_ch1")).thenReturn(challenge);
        when(challenge.getUniqueId()).thenReturn("bskyblock_ch1");
        when(challenge.getFriendlyName()).thenReturn("Repeatable");
        when(challenge.getIcon()).thenReturn(null);
        when(challenge.getLevel()).thenReturn("");
        when(challenge.getOrder()).thenReturn(0);
        when(challenge.isDeployed()).thenReturn(false);
        when(challenge.getDescription()).thenReturn(Collections.emptyList());
        when(challenge.getChallengeType()).thenReturn(ChallengeType.ISLAND_TYPE);
        when(challenge.isRepeatable()).thenReturn(true);
        when(challenge.getMaxTimes()).thenReturn(5);

        Map<String, Object> result = (Map<String, Object>) handler.handle(
                Map.of(Constants.CHALLENGE_NAME_KEY, "bskyblock_ch1"));

        assertEquals(true, result.get("repeatable"));
        assertEquals(5, result.get("maxTimes"));
    }

    @Test
    void testHandlerLabel() {
        assertEquals("challenge-data", handler.getLabel());
    }
}
