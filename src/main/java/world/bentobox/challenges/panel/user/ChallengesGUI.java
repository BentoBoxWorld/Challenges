package world.bentobox.challenges.panel.user;


import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.ChallengesManager;
import world.bentobox.challenges.LevelStatus;
import world.bentobox.challenges.database.object.Challenges;
import world.bentobox.challenges.panel.CommonGUI;
import world.bentobox.challenges.panel.TryToComplete;


/**
 * This is UserGUI class. It contains everything necessary for user to use it.
 */
public class ChallengesGUI extends CommonGUI
{
// ---------------------------------------------------------------------
// Section: Constructors
// ---------------------------------------------------------------------

	/**
	 * Default constructor that inits panels with minimal requirements, without parent panel.
	 *
	 * @param addon Addon where panel operates.
	 * @param world World from which panel was created.
	 * @param user User who created panel.
	 * @param topLabel Command top label which creates panel (f.e. island or ai)
	 * @param permissionPrefix Command permission prefix (f.e. bskyblock.)
	 */
	public ChallengesGUI(ChallengesAddon addon,
		World world,
		User user,
		String topLabel,
		String permissionPrefix)
	{
		super(addon, world, user, topLabel, permissionPrefix);
		this.challengesManager = this.addon.getChallengesManager();

		this.levelStatusList = this.challengesManager.getChallengeLevelStatus(this.user, this.world);
		this.lastSelectedLevel = this.levelStatusList.get(0);
	}

// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


	/**
	 * This method builds all necessary elements in GUI panel.
	 */
	@Override
	public void build()
	{
		PanelBuilder panelBuilder = new PanelBuilder().user(this.user).
			name(this.user.getTranslation("challenges.gui.title"));

		// TODO: get last completed level.

		if (this.addon.getChallengesSettings().isFreeChallengesFirst())
		{
			this.addFreeChallenges(panelBuilder);

			// Start new row for challenges.
			while (panelBuilder.nextSlot() % 9 != 0)
			{
				panelBuilder.item(
					new PanelItemBuilder().icon(Material.LIGHT_GRAY_STAINED_GLASS_PANE).name(" ").build());
			}
		}

		this.addChallenges(panelBuilder);

		// Start new row for levels.
		while (panelBuilder.nextSlot() % 9 != 0)
		{
			panelBuilder.item(
				new PanelItemBuilder().icon(Material.LIGHT_GRAY_STAINED_GLASS_PANE).name(" ").build());
		}

		this.addChallengeLevels(panelBuilder);

		if (!this.addon.getChallengesSettings().isFreeChallengesFirst())
		{
			// Start new row for free challenges.
			while (panelBuilder.nextSlot() % 9 != 0)
			{
				panelBuilder.item(
					new PanelItemBuilder().icon(Material.LIGHT_GRAY_STAINED_GLASS_PANE).name(" ").build());
			}

			this.addFreeChallenges(panelBuilder);
		}

		panelBuilder.build();
	}


	/**
	 * This method adds free challenges to panelBuilder.
	 * @param panelBuilder where free challenges must be added.
	 */
	private void addFreeChallenges(PanelBuilder panelBuilder)
	{
		List<Challenges> freeChallenges = this.challengesManager.getFreeChallenges(this.user, this.world);
		final int freeChallengesCount = freeChallenges.size();

		if (freeChallengesCount > 18)
		{
			int firstIndex = panelBuilder.nextSlot();

			if (this.freeChallengeIndex > 0)
			{
				panelBuilder.item(new PanelItemBuilder().
					icon(Material.SIGN).
					name("Previous").
					clickHandler((panel, user1, clickType, slot) -> {
						this.freeChallengeIndex--;
						this.build();
						return true;
					}).build());
			}

			int currentIndex = this.freeChallengeIndex;

			while (panelBuilder.nextSlot() != firstIndex + 18 && currentIndex < freeChallengesCount)
			{
				panelBuilder.item(this.getChallengeButton(freeChallenges.get(currentIndex++)));
			}

			// Check if one challenge is left
			if (currentIndex + 1 == freeChallengesCount)
			{
				panelBuilder.item(this.getChallengeButton(freeChallenges.get(currentIndex)));
			}
			else if (currentIndex < freeChallengesCount)
			{
				panelBuilder.item(new PanelItemBuilder().
					icon(Material.SIGN).
					name("Next").
					clickHandler((panel, user1, clickType, slot) -> {
						this.freeChallengeIndex++;
						this.build();
						return true;
					}).build());
			}
		}
		else
		{
			for (Challenges challenge : freeChallenges)
			{
				// there are no limitations. Just bunch insert.
				panelBuilder.item(this.getChallengeButton(challenge));
			}
		}
	}


	/**
	 * This method adds last selected level challenges to panelBuilder.
	 * @param panelBuilder where last selected level challenges must be added.
	 */
	private void addChallenges(PanelBuilder panelBuilder)
	{
		List<Challenges> challenges = this.challengesManager.getChallenges(this.lastSelectedLevel.getLevel());
		final int challengesCount = challenges.size();

		if (challengesCount > 18)
		{
			int firstIndex = panelBuilder.nextSlot();

			if (this.pageIndex > 0)
			{
				panelBuilder.item(new PanelItemBuilder().
					icon(Material.SIGN).
					name("Previous").
					clickHandler((panel, user1, clickType, slot) -> {
						this.pageIndex--;
						this.build();
						return true;
					}).build());
			}

			int currentIndex = this.pageIndex;

			while (panelBuilder.nextSlot() != firstIndex + 18 && currentIndex < challengesCount)
			{
				panelBuilder.item(this.getChallengeButton(challenges.get(currentIndex++)));
			}

			// Check if one challenge is left
			if (currentIndex + 1 == challengesCount)
			{
				panelBuilder.item(this.getChallengeButton(challenges.get(currentIndex)));
			}
			else if (currentIndex < challengesCount)
			{
				panelBuilder.item(new PanelItemBuilder().
					icon(Material.SIGN).
					name("Next").
					clickHandler((panel, user1, clickType, slot) -> {
						this.pageIndex++;
						this.build();
						return true;
					}).build());
			}
		}
		else
		{
			for (Challenges challenge : challenges)
			{
				// there are no limitations. Just bunch insert.
				panelBuilder.item(this.getChallengeButton(challenge));
			}
		}
	}


	/**
	 * This method adds challenge levels to panelBuilder.
	 * @param panelBuilder where challenge levels must be added.
	 */
	private void addChallengeLevels(PanelBuilder panelBuilder)
	{
		// Clone to avoid creating new level on each build.
		List<LevelStatus> leftLevels = new ArrayList<>(this.levelStatusList);
		leftLevels.remove(this.lastSelectedLevel);

		// TODO: Focusing on middle should be awsome.

		final int levelCounts = leftLevels.size();

		if (levelCounts > 9)
		{
			int firstIndex = panelBuilder.nextSlot();

			if (this.levelIndex > 0)
			{
				panelBuilder.item(new PanelItemBuilder().
					icon(Material.SIGN).
					name("Previous").
					clickHandler((panel, user1, clickType, slot) -> {
						this.levelIndex--;
						this.build();
						return true;
					}).build());
			}

			int currentIndex = this.levelIndex;

			while (panelBuilder.nextSlot() != firstIndex + 9 && currentIndex < levelCounts)
			{
				panelBuilder.item(this.getLevelButton(leftLevels.get(currentIndex++)));
			}

			// Check if one challenge is left
			if (currentIndex + 1 == levelCounts)
			{
				panelBuilder.item(this.getLevelButton(leftLevels.get(currentIndex)));
			}
			else if (currentIndex < levelCounts)
			{
				panelBuilder.item(new PanelItemBuilder().
					icon(Material.SIGN).
					name("Next").
					clickHandler((panel, user1, clickType, slot) -> {
						this.levelIndex++;
						this.build();
						return true;
					}).build());
			}
		}
		else
		{
			for (LevelStatus level : leftLevels)
			{
				// there are no limitations. Just bunch insert.
				panelBuilder.item(this.getLevelButton(level));
			}
		}
	}


// ---------------------------------------------------------------------
// Section: Icon building
// ---------------------------------------------------------------------


	/**
	 * This method creates given challenges icon that on press tries to complete it.
	 * @param challenge which icon must be constructed.
	 * @return PanelItem icon for challenge.
	 */
	private PanelItem getChallengeButton(Challenges challenge)
	{
		return new PanelItemBuilder().
			icon(challenge.getIcon()).
			name(challenge.getFriendlyName().isEmpty() ? challenge.getUniqueId() : challenge.getFriendlyName()).
			description(this.createChallengeDescription(challenge)).
			clickHandler((panel, user1, clickType, slot) -> {
				new TryToComplete(this.addon,
					this.user,
					this.challengesManager,
					challenge,
					this.world,
					this.permissionPrefix,
					this.topLabel);
				return true;
			}).
			glow(this.challengesManager.isChallengeComplete(this.user, challenge)).
			build();
	}


	/**
	 * This method creates challenges description by adding all information that is necessary for this challenge.
	 * @param challenge Which information must be retrieved.
	 * @return List with strings that contains information about given challenge.
	 */
	private List<String> createChallengeDescription(Challenges challenge)
	{
		List<String> result = new ArrayList<>();

		result.add(this.user.getTranslation("challenges.level",
			"[level]", this.challengesManager.getChallengesLevel(challenge)));

		boolean completed = this.challengesManager.isChallengeComplete(this.user, challenge);

		if (completed)
		{
			result.add(this.user.getTranslation("challenges.complete"));
		}

		if (challenge.isRepeatable())
		{
			int maxTimes = challenge.getMaxTimes();
			long doneTimes = this.challengesManager.checkChallengeTimes(this.user, challenge);

			if (maxTimes > 0)
			{
				if (doneTimes < maxTimes)
				{
					result.add(this.user.getTranslation("challenges.completed-times",
						"[donetimes]", String.valueOf(doneTimes),
						"[maxtimes]", String.valueOf(maxTimes)));

					// Change value to false, as max count not reached.
					completed = false;
				}
				else
				{
					result.add(this.user.getTranslation("challenges.maxed-reached",
						"[donetimes]", String.valueOf(doneTimes),
						"[maxtimes]", String.valueOf(maxTimes)));
				}
			}
			else
			{
				result.add(this.user.getTranslation("challenges.completed-times",
					"[donetimes]", String.valueOf(doneTimes)));

				// Change value to false, as max count not reached.
				completed = false;
			}
		}

		if (!completed)
		{
			result.addAll(challenge.getDescription());

			if (challenge.getChallengeType().equals(Challenges.ChallengeType.INVENTORY))
			{
				if (challenge.isTakeItems())
				{
					result.add(this.user.getTranslation("challenges.item-take-warning"));
				}
			}
			else if (challenge.getChallengeType().equals(Challenges.ChallengeType.ISLAND))
			{
				result.add(this.user.getTranslation("challenges.items-closeby"));

				if (challenge.isRemoveEntities() && !challenge.getRequiredEntities().isEmpty())
				{
					result.add(this.user.getTranslation("challenges.entities-kill-warning"));
				}

				if (challenge.isRemoveBlocks() && !challenge.getRequiredBlocks().isEmpty())
				{
					result.add(this.user.getTranslation("challenges.blocks-take-warning"));
				}
			}
		}

		if (completed)
		{
			result.add(this.user.getTranslation("challenges.not-repeatable"));
		}
		else
		{
			result.addAll(this.challengeRewards(challenge));
		}

		return result;
	}


	/**
	 * This method returns list of strings that contains basic information about challenge rewards.
	 * @param challenge which reward message must be created.
	 * @return list of strings that contains rewards message.
	 */
	private List<String> challengeRewards(Challenges challenge)
	{
		String rewardText;
		double rewardMoney;
		int rewardExperience;

		if (!this.challengesManager.isChallengeComplete(this.user, challenge))
		{
			rewardText = challenge.getRewardText();
			rewardMoney = challenge.getRewardMoney();
			rewardExperience = challenge.getRewardExp();
		}
		else
		{
			rewardText = challenge.getRepeatRewardText();
			rewardMoney = challenge.getRepeatMoneyReward();
			rewardExperience = challenge.getRepeatExpReward();
		}

		List<String> result = new ArrayList<>();

		// Add reward text
		result.add(rewardText);

		// Add message about reward XP
		if (rewardExperience > 0)
		{
			result.add(this.user.getTranslation("challenges.exp-reward",
				"[reward]", Integer.toString(rewardExperience)));
		}

		// Add message about reward money
		if (this.addon.getPlugin().getSettings().isUseEconomy() && rewardMoney > 0)
		{
			result.add(this.user.getTranslation("challenges.money-reward",
				"[reward]", Double.toString(rewardMoney)));
		}

		return result;
	}


	/**
	 * This method creates button for given level.
	 * @param level which button must be created.
	 * @return Button for given level.
	 */
	private PanelItem getLevelButton(LevelStatus level)
	{
		// Create a nice name for the level
		String name = level.getLevel().getFriendlyName().isEmpty() ?
			level.getLevel().getUniqueId() :
			level.getLevel().getFriendlyName();

		ItemStack icon;
		List<String> description;
		PanelItem.ClickHandler clickHandler;

		if (level.isUnlocked())
		{
			icon = level.getLevel().getIcon();
			description = Collections.singletonList(
				this.user.getTranslation("challenges.navigation", "[level]", name));
			clickHandler = (panel, user1, clickType, slot) -> {
				this.lastSelectedLevel = level;

				// Reset level and page index.
				this.levelIndex = 0;
				this.pageIndex = 0;

				this.build();
				return true;
			};
		}
		else
		{
			icon = new ItemStack(Material.BOOK);

			// This should be safe as first level always should be unlocked.
			LevelStatus previousLevel = this.levelStatusList.get(this.levelStatusList.indexOf(level) - 1);

			description = Collections.singletonList(
				this.user.getTranslation("challenges.to-complete",
					"[challengesToDo]", Integer.toString(previousLevel.getNumberOfChallengesStillToDo()),
					"[thisLevel]", previousLevel.getLevel().getFriendlyName()));

			clickHandler = null;
		}

		return new PanelItem(icon, name, description, false, clickHandler, false);
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

	/**
	 * This will be used if free challenges are more then 18.
	 */
	private int freeChallengeIndex = 0;

	/**
	 * This will be used if levels are more then 9.
	 */
	private int levelIndex;

	/**
	 * This list contains all information about level completion in current world.
	 */
	private List<LevelStatus> levelStatusList;

	/**
	 * This indicate last selected level.
	 */
	private LevelStatus lastSelectedLevel;

	/**
	 * Challenge Manager object.
	 */
	private ChallengesManager challengesManager;
}
