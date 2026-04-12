package world.bentobox.challenges.database.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import world.bentobox.bentobox.api.logs.LogEntry;

/**
 * Tests for {@link ChallengesPlayerData}.
 */
class ChallengesPlayerDataTest {

    private static final String UUID_STR = UUID.randomUUID().toString();
    private static final String CHALLENGE = "bskyblock_mychallenge";

    private ChallengesPlayerData data;

    @BeforeEach
    void setUp() {
        data = new ChallengesPlayerData(UUID_STR);
    }

    // ---------------------------------------------------------------
    // Constructor / identity
    // ---------------------------------------------------------------

    @Test
    void testConstructorSetsUniqueId() {
        assertEquals(UUID_STR, data.getUniqueId());
    }

    @Test
    void testDefaultConstructorHasEmptyUniqueId() {
        ChallengesPlayerData empty = new ChallengesPlayerData();
        assertEquals("", empty.getUniqueId());
    }

    @Test
    void testSetUniqueId() {
        String newId = UUID.randomUUID().toString();
        data.setUniqueId(newId);
        assertEquals(newId, data.getUniqueId());
    }

    // ---------------------------------------------------------------
    // Challenge completion state
    // ---------------------------------------------------------------

    @Test
    void testNewDataChallengeNotDone() {
        assertFalse(data.isChallengeDone(CHALLENGE));
        assertEquals(0, data.getTimes(CHALLENGE));
    }

    @Test
    void testSetChallengeDoneIncrements() {
        data.setChallengeDone(CHALLENGE);
        assertTrue(data.isChallengeDone(CHALLENGE));
        assertEquals(1, data.getTimes(CHALLENGE));
    }

    @Test
    void testSetChallengeDoneRepeatedly() {
        data.setChallengeDone(CHALLENGE);
        data.setChallengeDone(CHALLENGE);
        assertEquals(2, data.getTimes(CHALLENGE));
    }

    @Test
    void testAddChallengeDone() {
        data.addChallengeDone(CHALLENGE, 5);
        assertEquals(5, data.getTimes(CHALLENGE));
        data.addChallengeDone(CHALLENGE, 3);
        assertEquals(8, data.getTimes(CHALLENGE));
    }

    @Test
    void testSetChallengeTimesAbsoluteOverride() {
        data.setChallengeDone(CHALLENGE);
        data.setChallengeTimes(CHALLENGE, 10);
        assertEquals(10, data.getTimes(CHALLENGE));
    }

    @Test
    void testCaseInsensitiveChallengeStatus() {
        data.setChallengeDone("BSkyBlock_Challenge");
        assertTrue(data.isChallengeDone("bskyblock_challenge"));
        assertEquals(1, data.getTimes("BSKYBLOCK_CHALLENGE"));
    }

    // ---------------------------------------------------------------
    // Timestamps
    // ---------------------------------------------------------------

    @Test
    void testGetLastCompletionTimeZeroIfNotDone() {
        assertEquals(0L, data.getLastCompletionTime(CHALLENGE));
    }

    @Test
    void testGetLastCompletionTimeUpdatedOnDone() {
        long before = System.currentTimeMillis();
        data.setChallengeDone(CHALLENGE);
        long after = System.currentTimeMillis();
        long ts = data.getLastCompletionTime(CHALLENGE);
        assertTrue(ts >= before && ts <= after);
    }

    @Test
    void testSetChallengeTimesUpdatesTimestamp() {
        long before = System.currentTimeMillis();
        data.setChallengeTimes(CHALLENGE, 1);
        long after = System.currentTimeMillis();
        long ts = data.getLastCompletionTime(CHALLENGE);
        assertTrue(ts >= before && ts <= after);
    }

    // ---------------------------------------------------------------
    // Reset
    // ---------------------------------------------------------------

    @Test
    void testResetRemovesGameModeChallenge() {
        data.setChallengeDone(CHALLENGE);
        data.reset("bskyblock");
        assertFalse(data.isChallengeDone(CHALLENGE));
        assertEquals(0, data.getTimes(CHALLENGE));
    }

    @Test
    void testResetLeavesOtherGameModeIntact() {
        data.setChallengeDone(CHALLENGE);
        data.setChallengeDone("acidisland_other");
        data.reset("bskyblock");
        assertTrue(data.isChallengeDone("acidisland_other"));
    }

    @Test
    void testResetCaseInsensitive() {
        data.setChallengeDone("BSkyBlock_ch1");
        data.reset("bskyblock");
        assertFalse(data.isChallengeDone("BSkyBlock_ch1"));
    }

    @Test
    void testResetClearsTimestamp() {
        data.setChallengeDone(CHALLENGE);
        data.reset("bskyblock");
        assertEquals(0L, data.getLastCompletionTime(CHALLENGE));
    }

    // ---------------------------------------------------------------
    // Levels
    // ---------------------------------------------------------------

    @Test
    void testIsLevelDoneFalseWhenEmpty() {
        assertFalse(data.isLevelDone("bskyblock_level1"));
    }

    @Test
    void testAddCompletedLevel() {
        data.addCompletedLevel("bskyblock_level1");
        assertTrue(data.isLevelDone("bskyblock_level1"));
    }

    @Test
    void testIsLevelDoneFalseForUnknownLevel() {
        data.addCompletedLevel("bskyblock_level1");
        assertFalse(data.isLevelDone("bskyblock_level2"));
    }

    @Test
    void testResetClearsLevels() {
        data.addCompletedLevel("bskyblock_level1");
        data.reset("bskyblock");
        assertFalse(data.isLevelDone("bskyblock_level1"));
    }

    // ---------------------------------------------------------------
    // History
    // ---------------------------------------------------------------

    @Test
    void testHistoryEmptyInitially() {
        assertTrue(data.getHistory().isEmpty());
    }

    @Test
    void testAddHistoryRecord() {
        LogEntry entry = mock(LogEntry.class);
        data.addHistoryRecord(entry);
        assertEquals(1, data.getHistory().size());
        assertSame(entry, data.getHistory().get(0));
    }

    @Test
    void testMultipleHistoryRecordsPreserveOrder() {
        LogEntry e1 = mock(LogEntry.class);
        LogEntry e2 = mock(LogEntry.class);
        data.addHistoryRecord(e1);
        data.addHistoryRecord(e2);
        assertEquals(2, data.getHistory().size());
        assertSame(e1, data.getHistory().get(0));
        assertSame(e2, data.getHistory().get(1));
    }

    // ---------------------------------------------------------------
    // equals / hashCode
    // ---------------------------------------------------------------

    @Test
    void testEqualsSelf() {
        assertEquals(data, data);
    }

    @Test
    void testEqualsWithSameId() {
        ChallengesPlayerData other = new ChallengesPlayerData(UUID_STR);
        assertEquals(data, other);
    }

    @Test
    void testEqualsWithDifferentCaseId() {
        ChallengesPlayerData other = new ChallengesPlayerData(UUID_STR.toUpperCase());
        assertEquals(data, other);
    }

    @Test
    void testEqualsWithDifferentId() {
        ChallengesPlayerData other = new ChallengesPlayerData(UUID.randomUUID().toString());
        assertNotEquals(data, other);
    }

    @Test
    void testEqualsNull() {
        assertNotEquals(null, data);
    }

    @Test
    void testEqualsOtherType() {
        assertNotEquals("string", data);
    }

    @Test
    void testHashCodeConsistentForEqualObjects() {
        ChallengesPlayerData other = new ChallengesPlayerData(UUID_STR);
        assertEquals(data.hashCode(), other.hashCode());
    }

    // ---------------------------------------------------------------
    // Bulk getters / setters
    // ---------------------------------------------------------------

    @Test
    void testGetChallengeStatus() {
        data.setChallengeDone(CHALLENGE);
        assertNotNull(data.getChallengeStatus());
        assertTrue(data.getChallengeStatus().containsKey(CHALLENGE));
    }

    @Test
    void testGetChallengesTimestamp() {
        data.setChallengeDone(CHALLENGE);
        assertNotNull(data.getChallengesTimestamp());
        assertTrue(data.getChallengesTimestamp().containsKey(CHALLENGE));
    }

    @Test
    void testGetLevelsDone() {
        data.addCompletedLevel("bskyblock_level1");
        assertNotNull(data.getLevelsDone());
        assertTrue(data.getLevelsDone().contains("bskyblock_level1"));
    }
}
