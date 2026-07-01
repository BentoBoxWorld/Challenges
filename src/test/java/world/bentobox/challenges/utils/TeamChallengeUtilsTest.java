package world.bentobox.challenges.utils;


import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;


/**
 * Tests for the pure team challenge maths in {@link TeamChallengeUtils}.
 *
 * @author tastybento
 */
public class TeamChallengeUtilsTest
{
    // ---------------------------------------------------------------------
    // requiredPresentCount
    // ---------------------------------------------------------------------

    @Test
    public void testRequiredPresentCountDisabledWhenZero()
    {
        // A presence of 0 disables the gate.
        assertEquals(0, TeamChallengeUtils.requiredPresentCount(5, 0.0));
    }


    @Test
    public void testRequiredPresentCountFloorOfOne()
    {
        // ceil(3 * 0.1) = 1; a positive presence never rounds down to 0.
        assertEquals(1, TeamChallengeUtils.requiredPresentCount(3, 0.1));
    }


    @Test
    public void testRequiredPresentCountHalfOfTwoIsOne()
    {
        // 50% of a 2-member team is 1, not 2 (the floor-of-2 was a bug).
        assertEquals(1, TeamChallengeUtils.requiredPresentCount(2, 0.5));
    }


    @Test
    public void testRequiredPresentCountCeil()
    {
        // ceil(3 * 0.5) = ceil(1.5) = 2.
        assertEquals(2, TeamChallengeUtils.requiredPresentCount(3, 0.5));
        // ceil(5 * 0.5) = ceil(2.5) = 3.
        assertEquals(3, TeamChallengeUtils.requiredPresentCount(5, 0.5));
        // Full presence.
        assertEquals(5, TeamChallengeUtils.requiredPresentCount(5, 1.0));
    }


    // ---------------------------------------------------------------------
    // perMemberShare
    // ---------------------------------------------------------------------

    @Test
    public void testPerMemberShareRoundsUp()
    {
        // 20 total across 3 present -> ceil(6.67) = 7 each.
        assertEquals(7, TeamChallengeUtils.perMemberShare(20, 3));
        // 20 across 4 -> 5 each.
        assertEquals(5, TeamChallengeUtils.perMemberShare(20, 4));
        // 20 across 1 -> 20.
        assertEquals(20, TeamChallengeUtils.perMemberShare(20, 1));
    }


    @Test
    public void testPerMemberShareZeroPresent()
    {
        // Degenerate case: fall back to the full total.
        assertEquals(20, TeamChallengeUtils.perMemberShare(20, 0));
    }


    // ---------------------------------------------------------------------
    // waterLevelSplit
    // ---------------------------------------------------------------------

    @Test
    public void testWaterLevelSplitWorkedExample()
    {
        // The canonical design example: 5 members, need 100.
        int[] take = TeamChallengeUtils.waterLevelSplit(new int[] {10, 15, 20, 40, 100}, 100);
        assertArrayEquals(new int[] {10, 15, 20, 27, 28}, take);
        assertEquals(100, sum(take));
    }


    @Test
    public void testWaterLevelSplitEqualHoldings()
    {
        // Five members each with 20, need 100 -> everyone gives 20.
        int[] take = TeamChallengeUtils.waterLevelSplit(new int[] {20, 20, 20, 20, 20}, 100);
        assertArrayEquals(new int[] {20, 20, 20, 20, 20}, take);
    }


    @Test
    public void testWaterLevelSplitOneRichHolder()
    {
        // One holder has 96, others 1 each, need 100 -> all removed.
        int[] take = TeamChallengeUtils.waterLevelSplit(new int[] {96, 1, 1, 1, 1}, 100);
        assertArrayEquals(new int[] {96, 1, 1, 1, 1}, take);
        assertEquals(100, sum(take));
    }


    @Test
    public void testWaterLevelSplitSingleHolderCarries()
    {
        // A single member holds everything; a solo contribution is allowed.
        int[] take = TeamChallengeUtils.waterLevelSplit(new int[] {100, 0, 0}, 100);
        assertArrayEquals(new int[] {100, 0, 0}, take);
    }


    @Test
    public void testWaterLevelSplitRemainderGoesToRichest()
    {
        // need 10, holdings [3, 3, 9]. Level 3 -> [3,3,3]=9, remainder 1 to the richest (index 2).
        int[] take = TeamChallengeUtils.waterLevelSplit(new int[] {3, 3, 9}, 10);
        assertArrayEquals(new int[] {3, 3, 4}, take);
        assertEquals(10, sum(take));
    }


    @Test
    public void testWaterLevelSplitNeedExceedsTotalTakesEverything()
    {
        int[] take = TeamChallengeUtils.waterLevelSplit(new int[] {5, 5, 5}, 100);
        assertArrayEquals(new int[] {5, 5, 5}, take);
        assertEquals(15, sum(take));
    }


    @Test
    public void testWaterLevelSplitZeroNeed()
    {
        int[] take = TeamChallengeUtils.waterLevelSplit(new int[] {5, 5, 5}, 0);
        assertArrayEquals(new int[] {0, 0, 0}, take);
    }


    @Test
    public void testTotalRemovable()
    {
        assertEquals(100, TeamChallengeUtils.totalRemovable(new int[] {10, 15, 20, 40, 100}, 100));
        assertEquals(15, TeamChallengeUtils.totalRemovable(new int[] {5, 5, 5}, 100));
    }


    private static int sum(int[] a)
    {
        int s = 0;

        for (int v : a)
        {
            s += v;
        }

        return s;
    }
}
