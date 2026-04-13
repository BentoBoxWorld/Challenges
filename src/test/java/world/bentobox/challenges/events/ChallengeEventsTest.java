package world.bentobox.challenges.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

/**
 * Tests for all four challenge event POJOs.
 */
class ChallengeEventsTest {

    // ---------------------------------------------------------------
    // ChallengeCompletedEvent
    // ---------------------------------------------------------------

    @Test
    void testChallengeCompletedEvent_constructorAndGetters() {
        UUID uuid = UUID.randomUUID();
        ChallengeCompletedEvent event = new ChallengeCompletedEvent("ch1", uuid, false, 3);
        assertEquals("ch1", event.getChallengeID());
        assertEquals(uuid, event.getPlayerUUID());
        assertFalse(event.isAdmin());
        assertEquals(3, event.getCompletionCount());
    }

    @Test
    void testChallengeCompletedEvent_setters() {
        UUID uuid = UUID.randomUUID();
        ChallengeCompletedEvent event = new ChallengeCompletedEvent("ch1", uuid, false, 1);
        UUID newUuid = UUID.randomUUID();
        event.setChallengeID("ch2");
        event.setPlayerUUID(newUuid);
        event.setAdmin(true);
        event.setCompletionCount(5);
        assertEquals("ch2", event.getChallengeID());
        assertEquals(newUuid, event.getPlayerUUID());
        assertTrue(event.isAdmin());
        assertEquals(5, event.getCompletionCount());
    }

    @Test
    void testChallengeCompletedEvent_adminFlag() {
        ChallengeCompletedEvent event = new ChallengeCompletedEvent("ch1", UUID.randomUUID(), true, 1);
        assertTrue(event.isAdmin());
    }

    @Test
    void testChallengeCompletedEvent_handlers() {
        assertNotNull(ChallengeCompletedEvent.getHandlerList());
        ChallengeCompletedEvent event = new ChallengeCompletedEvent("ch1", UUID.randomUUID(), false, 1);
        assertNotNull(event.getHandlers());
        assertEquals(ChallengeCompletedEvent.getHandlerList(), event.getHandlers());
    }

    // ---------------------------------------------------------------
    // ChallengeResetEvent
    // ---------------------------------------------------------------

    @Test
    void testChallengeResetEvent_constructorAndGetters() {
        UUID uuid = UUID.randomUUID();
        ChallengeResetEvent event = new ChallengeResetEvent("ch1", uuid, true, "reason");
        assertEquals("ch1", event.getChallengeID());
        assertEquals(uuid, event.getPlayerUUID());
        assertTrue(event.isAdmin());
        assertEquals("reason", event.getReason());
    }

    @Test
    void testChallengeResetEvent_setters() {
        ChallengeResetEvent event = new ChallengeResetEvent("ch1", UUID.randomUUID(), false, "r1");
        UUID newUuid = UUID.randomUUID();
        event.setChallengeID("ch2");
        event.setPlayerUUID(newUuid);
        event.setAdmin(true);
        event.setReason("r2");
        assertEquals("ch2", event.getChallengeID());
        assertEquals(newUuid, event.getPlayerUUID());
        assertTrue(event.isAdmin());
        assertEquals("r2", event.getReason());
    }

    @Test
    void testChallengeResetEvent_handlers() {
        assertNotNull(ChallengeResetEvent.getHandlerList());
        ChallengeResetEvent event = new ChallengeResetEvent("ch1", UUID.randomUUID(), false, "r");
        assertNotNull(event.getHandlers());
        assertEquals(ChallengeResetEvent.getHandlerList(), event.getHandlers());
    }

    // ---------------------------------------------------------------
    // ChallengeResetAllEvent
    // ---------------------------------------------------------------

    @Test
    void testChallengeResetAllEvent_constructorAndGetters() {
        UUID uuid = UUID.randomUUID();
        ChallengeResetAllEvent event = new ChallengeResetAllEvent("world_nether", uuid, true, "wipe");
        assertEquals("world_nether", event.getWorldName());
        assertEquals(uuid, event.getPlayerUUID());
        assertTrue(event.isAdmin());
        assertEquals("wipe", event.getReason());
    }

    @Test
    void testChallengeResetAllEvent_setters() {
        ChallengeResetAllEvent event = new ChallengeResetAllEvent("w1", UUID.randomUUID(), false, "r1");
        UUID newUuid = UUID.randomUUID();
        event.setWorldName("w2");
        event.setPlayerUUID(newUuid);
        event.setAdmin(true);
        event.setReason("r2");
        assertEquals("w2", event.getWorldName());
        assertEquals(newUuid, event.getPlayerUUID());
        assertTrue(event.isAdmin());
        assertEquals("r2", event.getReason());
    }

    @Test
    void testChallengeResetAllEvent_handlers() {
        assertNotNull(ChallengeResetAllEvent.getHandlerList());
        ChallengeResetAllEvent event = new ChallengeResetAllEvent("w", UUID.randomUUID(), false, "r");
        assertNotNull(event.getHandlers());
        assertEquals(ChallengeResetAllEvent.getHandlerList(), event.getHandlers());
    }

    // ---------------------------------------------------------------
    // LevelCompletedEvent
    // ---------------------------------------------------------------

    @Test
    void testLevelCompletedEvent_constructorAndGetters() {
        UUID uuid = UUID.randomUUID();
        LevelCompletedEvent event = new LevelCompletedEvent("lvl1", uuid, false);
        assertEquals("lvl1", event.getLevelID());
        assertEquals(uuid, event.getPlayerUUID());
        assertFalse(event.isAdmin());
    }

    @Test
    void testLevelCompletedEvent_setters() {
        LevelCompletedEvent event = new LevelCompletedEvent("lvl1", UUID.randomUUID(), false);
        UUID newUuid = UUID.randomUUID();
        event.setLevelID("lvl2");
        event.setPlayerUUID(newUuid);
        event.setAdmin(true);
        assertEquals("lvl2", event.getLevelID());
        assertEquals(newUuid, event.getPlayerUUID());
        assertTrue(event.isAdmin());
    }

    @Test
    void testLevelCompletedEvent_adminFlag() {
        LevelCompletedEvent event = new LevelCompletedEvent("lvl1", UUID.randomUUID(), true);
        assertTrue(event.isAdmin());
    }

    @Test
    void testLevelCompletedEvent_handlers() {
        assertNotNull(LevelCompletedEvent.getHandlerList());
        LevelCompletedEvent event = new LevelCompletedEvent("lvl", UUID.randomUUID(), false);
        assertNotNull(event.getHandlers());
        assertEquals(LevelCompletedEvent.getHandlerList(), event.getHandlers());
    }
}
