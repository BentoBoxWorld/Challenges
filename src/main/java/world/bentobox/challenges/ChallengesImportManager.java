package world.bentobox.challenges;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import world.bentobox.bentobox.api.configuration.Config;
import world.bentobox.bentobox.util.ItemParser;
import world.bentobox.challenges.database.object.ChallengeLevel;
import world.bentobox.challenges.database.object.ChallengeLevels;
import world.bentobox.challenges.database.object.Challenge;
import world.bentobox.challenges.database.object.Challenges;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;
import world.bentobox.challenges.utils.GuiUtils;


/**
 * Imports challenges
 * @author tastybento
 *
 */
public class ChallengesImportManager
{

    private ChallengesAddon addon;
    private YamlConfiguration chal;

    /**
     * Import challenges from challenges.yml
     * @param challengesAddon
     */
    public ChallengesImportManager(ChallengesAddon challengesAddon) {
        this.addon = challengesAddon;
        File challengeFile = new File(addon.getDataFolder(), "challenges.yml");
        if (!challengeFile.exists()) {
            addon.saveResource("challenges.yml",false);
        }
    }

    /**
     * Import challenges
     * @param user - user
     * @param world - world to import into
     * @param overwrite - true if previous ones should be overwritten
     * @return true if successful
     */
    public boolean importChallenges(User user, World world, boolean overwrite) {
        File challengeFile = new File(addon.getDataFolder(), "challenges.yml");
        if (!challengeFile.exists()) {
            user.sendMessage("challenges.admin.import.no-file");
            return false;
        }
        chal = new YamlConfiguration();
        try {
            chal.load(challengeFile);
        } catch (IOException | InvalidConfigurationException e) {
            user.sendMessage("challenges.admin.import.no-load","[message]", e.getMessage());
            return false;
        }
        makeLevels(user, world, overwrite);
        makeChallenges(user, world, overwrite);
        addon.getChallengesManager().save();
        return true;
    }

    private void makeLevels(User user, World world, boolean overwrite) {
        // Parse the levels
        String levels = chal.getString("challenges.levels", "");
        if (!levels.isEmpty()) {
            user.sendMessage("challenges.admin.import.levels", "[levels]", levels);
            String[] lvs = levels.split(" ");
            int order = 0;
            for (String level : lvs) {
                ChallengeLevel challengeLevel = new ChallengeLevel();
                challengeLevel.setFriendlyName(level);
                challengeLevel.setUniqueId(Util.getWorld(world).getName() + "_" + level);
                challengeLevel.setOrder(order++);
                challengeLevel.setWorld(Util.getWorld(world).getName());
                challengeLevel.setWaiverAmount(chal.getInt("challenges.waiveramount"));
                // Check if there is a level reward
                ConfigurationSection unlock = chal.getConfigurationSection("challenges.levelUnlock." + level);
                if (unlock != null) {
                    challengeLevel.setUnlockMessage(unlock.getString("message", ""));
                    challengeLevel.setRewardText(unlock.getString("rewardDesc",""));
                    challengeLevel.setRewardItems(parseItems(unlock.getString("itemReward","")));
                    challengeLevel.setRewardMoney(unlock.getInt("moneyReward"));
                    challengeLevel.setRewardExperience(unlock.getInt("expReward"));
                    challengeLevel.setRewardCommands(unlock.getStringList("commands"));
                }
                addon.getChallengesManager().loadLevel(challengeLevel, overwrite, user, false);
            }
        } else {
            user.sendMessage("challenges.admin.import.no-levels");
        }
    }

    /**
     * Imports challenges
     * @param overwrite
     */
    private void makeChallenges(User user, World world, boolean overwrite) {
        int size = 0;
        // Parse the challenge file
        ConfigurationSection chals = chal.getConfigurationSection("challenges.challengeList");
        for (String challenge : chals.getKeys(false)) {
            Challenge newChallenge = new Challenge();
            newChallenge.setUniqueId(Util.getWorld(world).getName() + "_" + challenge);
            newChallenge.setDeployed(true);
            ConfigurationSection details = chals.getConfigurationSection(challenge);
            newChallenge.setFriendlyName(details.getString("friendlyname", challenge));
            newChallenge.setDescription(GuiUtils.stringSplit(
                details.getString("description", ""),
                this.addon.getChallengesSettings().getLoreLineLength()));
            newChallenge.setIcon(ItemParser.parse(details.getString("icon", "") + ":1"));

            if (details.getString("type", "").equalsIgnoreCase("level"))
            {
                // Fix for older version config
                newChallenge.setChallengeType(Challenge.ChallengeType.OTHER);
            }
            else
            {
                newChallenge.setChallengeType(Challenge.ChallengeType.valueOf(details.getString("type","INVENTORY").toUpperCase()));
            }

            newChallenge.setTakeItems(details.getBoolean("takeItems",true));
            newChallenge.setRewardText(details.getString("rewardText", ""));
            newChallenge.setRewardCommands(details.getStringList("rewardcommands"));
            newChallenge.setRewardMoney(details.getInt("moneyReward",0));
            newChallenge.setRewardExperience(details.getInt("expReward"));
            newChallenge.setRepeatable(details.getBoolean("repeatable"));
            newChallenge.setRepeatRewardText(details.getString("repeatRewardText",""));
            newChallenge.setRepeatMoneyReward(details.getInt("repearMoneyReward"));
            newChallenge.setRepeatExperienceReward(details.getInt("repeatExpReward"));
            newChallenge.setRepeatRewardCommands(details.getStringList("repeatrewardcommands"));
            newChallenge.setMaxTimes(details.getInt("maxtimes"));
            // TODO reset allowed
            newChallenge.setRequiredMoney(details.getInt("requiredMoney"));
            newChallenge.setRequiredExperience(details.getInt("requiredExp"));
            String reqItems = details.getString("requiredItems","");
            if (newChallenge.getChallengeType().equals(Challenge.ChallengeType.INVENTORY)) {
                newChallenge.setRequiredItems(parseItems(reqItems));
            } else if (newChallenge.getChallengeType().equals(Challenge.ChallengeType.OTHER)) {
                newChallenge.setRequiredIslandLevel(Long.parseLong(reqItems));
            } else if (newChallenge.getChallengeType().equals(Challenge.ChallengeType.ISLAND)) {
                parseEntities(newChallenge, reqItems);
            }
            newChallenge.setRewardItems(parseItems(details.getString("itemReward", "")));
            newChallenge.setRepeatItemReward(parseItems(details.getString("repeatItemReward", "")));
            // Save
            this.addon.getChallengesManager().addChallengeToLevel(newChallenge,
                addon.getChallengesManager().getLevel(Util.getWorld(world).getName() + "_" + details.getString("level", "")));

            if (addon.getChallengesManager().loadChallenge(newChallenge, overwrite, user, false)) {
                size++;
            }
        }

        user.sendMessage("challenges.admin.import.number", "[number]", String.valueOf(size));
    }

    /**
     * Run through entity types and materials and try to match to the string given
     * @param challenge - challenge to be adjusted
     * @param string - string from YAML file
     */
    private void parseEntities(Challenge challenge, String string) {
        Map<EntityType, Integer> req = new EnumMap<>(EntityType.class);
        Map<Material, Integer> blocks = new EnumMap<>(Material.class);
        if (!string.isEmpty()) {
            for (String s : string.split(" ")) {
                String[] part = s.split(":");
                try {
                    Arrays.asList(EntityType.values()).stream().filter(t -> t.name().equalsIgnoreCase(part[0])).forEach(t -> req.put(t, Integer.valueOf(part[1])));
                    Arrays.asList(Material.values()).stream().filter(t -> t.name().equalsIgnoreCase(part[0])).forEach(t -> blocks.put(t, Integer.valueOf(part[1])));
                } catch (Exception e) {
                    addon.getLogger().severe("Cannot parse '" + s + "'. Skipping...");
                }
            }
        }
        challenge.setRequiredEntities(req);
        challenge.setRequiredBlocks(blocks);
    }

    private List<ItemStack> parseItems(String reqList) {
        List<ItemStack> result = new ArrayList<>();
        if (!reqList.isEmpty()) {
            for (String s : reqList.split(" ")) {
                ItemStack item = ItemParser.parse(s);
                if (item != null) {
                    result.add(item);
                }
            }
        }
        return result;
    }


// ---------------------------------------------------------------------
// Section: Backward compatibility
// ---------------------------------------------------------------------


    /**
     * This method imports challenges form 0.3 and below version.
     * @param user - user
     * @param world - world to import into
     * @param overwrite - true if previous ones should be overwritten
     * @return true if successful
     */
    public boolean importPreviousChallenges(User user, World world, boolean overwrite)
    {
        ChallengesManager manager = this.addon.getChallengesManager();

        List<Challenges> challenges =
            new Config<>(this.addon, Challenges.class).loadConfigObjects();

        if (!challenges.isEmpty())
        {
            List<ChallengeLevels> levels =
                new Config<>(this.addon, ChallengeLevels.class).loadConfigObjects();

            for (ChallengeLevels level : levels)
            {
                ChallengeLevel newlevel = this.createLevel(level, world);

                if (newlevel != null)
                {
                    manager.loadLevel(newlevel, overwrite, user, false);
                }
            }

            for (Challenges challenge : challenges)
            {
                Challenge newChallenge = this.createChallenge(challenge, world);

                if (newChallenge == null)
                {
                    continue;
                }

                manager.loadChallenge(newChallenge, overwrite, user, false);

                if (challenge.getLevel().isEmpty() || challenge.getLevel().equals("FREE"))
                {
                    newChallenge.setLevel(ChallengesManager.FREE);
                }
                else
                {
                    String levelName = Util.getWorld(world).getName() + "_" + challenge.getLevel();

                    if (this.addon.getChallengesManager().containsLevel(levelName))
                    {
                        manager.addChallengeToLevel(newChallenge,
                            this.addon.getChallengesManager().getLevel(levelName));
                    }
                }
            }
        }

        return true;
    }


    /**
     * This method creates new ChallengeLevel based on old level settings.
     * @param level Old level object.
     * @param world World where new challenge will operate.
     * @return New level or null, if old level does not operate in this world.
     */
    private ChallengeLevel createLevel(ChallengeLevels level, World world)
    {
        if (!level.getWorlds().isEmpty() &&
            !level.getWorlds().contains(Util.getWorld(world).getName()))
        {
            return null;
        }

        ChallengeLevel newLevel = new ChallengeLevel();
        newLevel.setUniqueId(Util.getWorld(world).getName() + "_" + level.getUniqueId());
        newLevel.setFriendlyName(level.getFriendlyName());
        newLevel.setOrder(level.getOrder());
        newLevel.setWorld(Util.getWorld(world).getName());

        newLevel.setUnlockMessage(level.getUnlockMessage());
        newLevel.setWaiverAmount(level.getWaiveramount());

        newLevel.setRewardText(level.getRewardDescription());
        newLevel.setRewardMoney(level.getMoneyReward());
        newLevel.setRewardExperience(level.getExpReward());
        newLevel.setRewardItems(level.getRewardItems());
        newLevel.setRewardCommands(level.getRewardCommands());

        return newLevel;
    }


    /**
     * This method creates new Challenge based on old challenges settings.
     * @param challenge Old challenges object.
     * @param world World where new challenge will operate.
     * @return New Challenge or null, if old challenge does not operate in this world.
     */
    private Challenge createChallenge(Challenges challenge, World world)
    {
        if (!challenge.getWorld().equals(Util.getWorld(world).getName()))
        {
            // Does not operate in given world.
            return null;
        }

        Challenge newChallenge = new Challenge();
        newChallenge.setUniqueId(challenge.getUniqueId());
        newChallenge.setFriendlyName(challenge.getFriendlyName());
        newChallenge.setRemoveWhenCompleted(challenge.isRemoveWhenCompleted());
        newChallenge.setDeployed(challenge.isDeployed());
        newChallenge.setIcon(challenge.getIcon());

        newChallenge.setEnvironment(new HashSet<>(challenge.getEnvironment()));

        switch (challenge.getChallengeType())
        {
            case INVENTORY:
                newChallenge.setChallengeType(Challenge.ChallengeType.INVENTORY);
                break;
            case ISLAND:
                newChallenge.setChallengeType(Challenge.ChallengeType.ISLAND);
                break;
            default:
                newChallenge.setChallengeType(Challenge.ChallengeType.OTHER);
                break;
        }

        newChallenge.setOrder(challenge.getSlot());
        newChallenge.setDescription(challenge.getDescription());

        newChallenge.setRequiredEntities(challenge.getRequiredEntities());
        newChallenge.setRequiredItems(challenge.getRequiredItems());
        newChallenge.setRequiredBlocks(challenge.getRequiredBlocks());
        newChallenge.setRequiredMoney(challenge.getReqMoney());
        newChallenge.setRequiredExperience(challenge.getReqExp());
        newChallenge.setRequiredIslandLevel(challenge.getReqIslandlevel());
        newChallenge.setRequiredPermissions(challenge.getReqPerms());

        newChallenge.setTakeMoney(challenge.isTakeMoney());
        newChallenge.setTakeItems(challenge.isTakeItems());
        newChallenge.setSearchRadius(challenge.getSearchRadius());

        newChallenge.setRewardText(challenge.getRewardText());
        newChallenge.setRewardItems(challenge.getRewardItems());
        newChallenge.setRewardMoney(challenge.getRewardMoney());
        newChallenge.setRewardExperience(challenge.getRewardExp());
        newChallenge.setRewardCommands(challenge.getRewardCommands());

        newChallenge.setRepeatable(challenge.isRepeatable());
        newChallenge.setMaxTimes(challenge.getMaxTimes());

        newChallenge.setRepeatRewardText(challenge.getRepeatRewardText());
        newChallenge.setRepeatItemReward(challenge.getRepeatItemReward());
        newChallenge.setRepeatMoneyReward(challenge.getRepeatMoneyReward());
        newChallenge.setRepeatExperienceReward(challenge.getRepeatExpReward());
        newChallenge.setRepeatRewardCommands(challenge.getRepeatRewardCommands());

        return newChallenge;
    }
}