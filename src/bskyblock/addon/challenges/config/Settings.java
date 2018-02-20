package bskyblock.addon.challenges.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Settings {

    // Storage
    // Challenge List
    public static Set<String> challengeList;
    // Waiver amount
    public static int waiverAmount;
    // List of challenge levels
    public static List<String> challengeLevels;
    // Free levels
    public static List<String> freeLevels = new ArrayList<String>();

    // Settings
    public static boolean resetChallenges;
    // Challenge completion broadcast
    public static boolean broadcastMessages;
    // Challenges - show or remove completed on-time challenges
    public static boolean removeCompleteOnetimeChallenges;
    // Add glow to completed challenge icons or not
    public static boolean addCompletedGlow;

}
