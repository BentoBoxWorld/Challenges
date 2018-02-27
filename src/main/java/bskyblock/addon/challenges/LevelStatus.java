package bskyblock.addon.challenges;

import bskyblock.addon.challenges.database.object.ChallengeLevels;

/**
 * Level status class
 * @author tastybento
 *
 */
public class LevelStatus {
    private final ChallengeLevels level;
    private final ChallengeLevels previousLevel;
    private final int numberOfChallengesStillToDo;
    private final boolean complete;

    public LevelStatus(ChallengeLevels level, ChallengeLevels previousLevel, int numberOfChallengesStillToDo, boolean complete) {
        super();
        this.level = level;
        this.previousLevel = previousLevel;
        this.numberOfChallengesStillToDo = numberOfChallengesStillToDo;
        this.complete = complete;
    }
    /**
     * @return the level
     */
    public ChallengeLevels getLevel() {
        return level;
    }
    /**
     * @return the numberOfChallengesStillToDo
     */
    public int getNumberOfChallengesStillToDo() {
        return numberOfChallengesStillToDo;
    }
    /**
     * @return the previousLevel
     */
    public ChallengeLevels getPreviousLevel() {
        return previousLevel;
    }
    /**
     * @return the complete
     */
    public boolean isComplete() {
        return complete;
    }


}
