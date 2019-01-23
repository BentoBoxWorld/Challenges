package world.bentobox.challenges;

import world.bentobox.challenges.database.object.ChallengeLevel;

/**
 * Level status class
 * @author tastybento
 *
 */
public class LevelStatus {
    private final ChallengeLevel level;
    private final ChallengeLevel previousLevel;
    private final int numberOfChallengesStillToDo;
    private final boolean complete;
    private final boolean isUnlocked;

    /**
     * @param level - level
     * @param previousLevel - previous level
     * @param numberOfChallengesStillToDo - number of challenges still to do on this level
     * @param complete - whether complete or not
     * @param isUnlocked 
     */
    public LevelStatus(ChallengeLevel level, ChallengeLevel previousLevel, int numberOfChallengesStillToDo, boolean complete, boolean isUnlocked) {
        super();
        this.level = level;
        this.previousLevel = previousLevel;
        this.numberOfChallengesStillToDo = numberOfChallengesStillToDo;
        this.complete = complete;
        this.isUnlocked = isUnlocked;
    }
    /**
     * @return the level
     */
    public ChallengeLevel getLevel() {
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
    public ChallengeLevel getPreviousLevel() {
        return previousLevel;
    }
    /**
     * @return the complete
     */
    public boolean isComplete() {
        return complete;
    }
    /**
     * @return the isUnlocked
     */
    public boolean isUnlocked() {
        return isUnlocked;
    }


}
