package world.bentobox.challenges.panel.admin;


import org.bukkit.World;
import java.util.List;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.database.object.ChallengeLevels;
import world.bentobox.challenges.panel.CommonGUI;
import world.bentobox.challenges.panel.util.ConfirmationGUI;


/**
 * This class creates GUI that lists all Levels. Clicking on Level icon will be processed
 * by input mode.
 */
public class ListLevelsGUI extends CommonGUI
{
// ---------------------------------------------------------------------
// Section: Constructor
// ---------------------------------------------------------------------


	/**
	 * {@inheritDoc}
	 * @param mode - mode that indicate what should do icon clicking.
	 */
	public ListLevelsGUI(ChallengesAddon addon,
		World world,
		User user,
		Mode mode,
		String topLabel,
		String permissionPrefix)
	{
		this(addon, world, user, mode, topLabel, permissionPrefix, null);
	}


	/**
	 * {@inheritDoc}
	 * @param mode - mode that indicate what should do icon clicking.
	 */
	public ListLevelsGUI(ChallengesAddon addon,
		World world,
		User user,
		Mode mode,
		String topLabel,
		String permissionPrefix,
		CommonGUI parentGUI)
	{
		super(addon, world, user, topLabel, permissionPrefix, parentGUI);
		this.currentMode = mode;
	}


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void build()
	{
		PanelBuilder panelBuilder = new PanelBuilder().user(this.user).name(
			this.user.getTranslation("challenges.gui.admin.choose-level-title"));

		List<ChallengeLevels> levelList = this.addon.getChallengesManager().getChallengeLevelList();

		int MAX_ELEMENTS = 45;
		if (this.pageIndex < 0)
		{
			this.pageIndex = 0;
		}
		else if (this.pageIndex > (levelList.size() / MAX_ELEMENTS))
		{
			this.pageIndex = levelList.size() / MAX_ELEMENTS;
		}

		int levelIndex = MAX_ELEMENTS * this.pageIndex;

		while (levelIndex < ((this.pageIndex + 1) * MAX_ELEMENTS) &&
			levelIndex < levelList.size())
		{
			panelBuilder.item(this.createLevelIcon(levelList.get(levelIndex)));
			levelIndex++;
		}

		int nextIndex = levelIndex % MAX_ELEMENTS == 0 ?
			MAX_ELEMENTS :
			(((levelIndex % MAX_ELEMENTS) - 1) / 9 + 1) * 9;

		if (levelIndex > MAX_ELEMENTS)
		{
			panelBuilder.item(nextIndex + 2, this.getButton(CommonButtons.PREVIOUS));
		}

		if (levelIndex < levelList.size())
		{
			panelBuilder.item(nextIndex + 6, this.getButton(CommonButtons.NEXT));
		}

		panelBuilder.item(nextIndex + 8, this.returnButton);

		panelBuilder.build();
	}


	/**
	 * This method creates button for given level
	 * @param challengeLevel Level which button must be created.
	 * @return Level button.
	 */
	private PanelItem createLevelIcon(ChallengeLevels challengeLevel)
	{
		PanelItemBuilder itemBuilder = new PanelItemBuilder().
			name(challengeLevel.getFriendlyName()).
			description(challengeLevel.getUnlockMessage()).
			icon(challengeLevel.getIcon()).
			glow(false);

		if (this.currentMode.equals(Mode.EDIT))
		{
			itemBuilder.clickHandler((panel, user1, clickType, i) -> {
				new EditLevelGUI(this.addon,
					this.world,
					this.user,
					challengeLevel,
					this.topLabel,
					this.permissionPrefix,
					this).build();
				return true;
			});
		}
		else if (this.currentMode.equals(Mode.DELETE))
		{
			itemBuilder.clickHandler((panel, user1, clickType, i) -> {
				new ConfirmationGUI(this, this.user);
				this.valueObject = challengeLevel;
				return true;
			});
		}

		return itemBuilder.build();
	}


	/**
	 * Overwriting set value allows to catch if ConfirmationGui returns true.
	 * @param value new Value of valueObject.
	 */
	@Override
	public void setValue(Object value)
	{
		if (value instanceof Boolean && ((Boolean) value) && this.valueObject != null)
		{
			this.addon.getChallengesManager().deleteChallengeLevel((ChallengeLevels) this.valueObject);
			this.valueObject = null;
		}
		else
		{
			this.valueObject = null;
		}
	}


// ---------------------------------------------------------------------
// Section: Enums
// ---------------------------------------------------------------------


	/**
	 * Mode in which gui icons should processed.
	 */
	public enum Mode
	{
		EDIT,
		DELETE
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

	/**
	 * Current mode in which icons will act.
	 */
	private Mode currentMode;
}
