package world.bentobox.challenges.utils;


import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.IntStream;


/**
 * Pure, side-effect-free helpers for team challenge maths. Kept separate from the Bukkit-heavy
 * completion pipeline so they can be unit-tested without a live server.
 *
 * @author tastybento
 */
public final class TeamChallengeUtils
{
    private TeamChallengeUtils()
    {
        // Utility class.
    }


    /**
     * Number of team members that must be online for a team challenge to be completable.
     * The rule is {@code max(1, ceil(teamSize * presence))}; a presence fraction of {@code 0}
     * (or less) disables the gate and returns {@code 0}. (The requirement that a team exists at
     * all - at least two members - is enforced separately, not by this presence gate, so 50% of a
     * 2-member team is 1, not 2.)
     *
     * @param teamSize total number of members on the team
     * @param presence fraction (0.0 - 1.0) of the team that must be online
     * @return required present count, or 0 when the gate is disabled
     */
    public static int requiredPresentCount(int teamSize, double presence)
    {
        if (presence <= 0.0)
        {
            // Gate disabled - aggregation only.
            return 0;
        }

        return Math.max(1, (int) Math.ceil(teamSize * presence));
    }


    /**
     * The per-member share of a team-total requirement for a "everyone pays" (Roll Call Feast)
     * challenge, rounded up so the team always covers at least the configured total.
     *
     * @param total the configured team-total amount
     * @param presentCount number of present members that will each pay
     * @return the amount each present member must contribute
     */
    public static int perMemberShare(int total, int presentCount)
    {
        if (presentCount <= 0)
        {
            return total;
        }

        return (int) Math.ceil((double) total / presentCount);
    }


    /**
     * Option A "water level" split: distribute a fixed obligation {@code need} across members
     * holding {@code holdings[i]} each, as equitably as possible. Everyone pays the same absolute
     * amount, capped at what they hold; anyone short pays all they have and the rest cover the
     * difference. Any integer rounding remainder is absorbed by the largest holder(s) first.
     *
     * <p>Example: holdings {@code [10, 15, 20, 40, 100]}, need {@code 100} -> {@code [10, 15, 20, 27, 28]}.
     *
     * @param holdings the amount each member holds (non-negative)
     * @param need the total amount to remove
     * @return an array parallel to {@code holdings} of the amount to take from each member
     */
    public static int[] waterLevelSplit(int[] holdings, int need)
    {
        int n = holdings.length;
        int[] take = new int[n];

        if (need <= 0 || n == 0)
        {
            return take;
        }

        long total = 0;
        int maxHolding = 0;

        for (int h : holdings)
        {
            int safe = Math.max(0, h);
            total += safe;
            maxHolding = Math.max(maxHolding, safe);
        }

        // Not enough held anywhere - take everything available.
        if (need >= total)
        {
            for (int i = 0; i < n; i++)
            {
                take[i] = Math.max(0, holdings[i]);
            }

            return take;
        }

        // Find the largest water level L such that sum(min(h, L)) <= need.
        int level = 0;

        for (int candidate = 0; candidate <= maxHolding; candidate++)
        {
            long sum = 0;

            for (int h : holdings)
            {
                sum += Math.min(Math.max(0, h), candidate);
            }

            if (sum <= need)
            {
                level = candidate;
            }
            else
            {
                break;
            }
        }

        long base = 0;

        for (int i = 0; i < n; i++)
        {
            take[i] = Math.min(Math.max(0, holdings[i]), level);
            base += take[i];
        }

        int remainder = (int) (need - base);

        if (remainder > 0)
        {
            // Give the odd unit(s) to the largest holder(s) above the water level.
            final int waterLevel = level;

            Integer[] order = IntStream.range(0, n).boxed().
                    filter(i -> holdings[i] > waterLevel).
                    sorted(Comparator.<Integer>comparingInt(i -> holdings[i]).reversed()).
                    toArray(Integer[]::new);

            for (int k = 0; k < remainder && k < order.length; k++)
            {
                take[order[k]]++;
            }
        }

        return take;
    }


    /**
     * Convenience wrapper returning the sum of a split - the amount actually removed.
     *
     * @param holdings holdings per member
     * @param need requested amount
     * @return total removed (== need when the team holds enough)
     */
    public static int totalRemovable(int[] holdings, int need)
    {
        return Arrays.stream(waterLevelSplit(holdings, need)).sum();
    }
}
