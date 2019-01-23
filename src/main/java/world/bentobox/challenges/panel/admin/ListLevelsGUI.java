package world.bentobox.challenges.panel.admin;


import org.bukkit.Material;
import org.bukkit.World;
import java.util.List;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.database.object.ChallengeLevel;
import world.bentobox.challenges.panel.CommonGUI;
import world.bentobox.challenges.panel.util.ConfirmationGUI;
import world.bentobox.challenges.utils.GuiUtils;


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

		if (this.currentMode.equals(Mode.DELETE))
		{
			GuiUtils.fillBorder(panelBuilder, Material.RED_STAINED_GLASS_PANE);
		}
		else
		{
			GuiUtils.fillBorder(panelBuilder);
		}

		List<ChallengeLevel> levelList = this.addon.getChallengesManager().getChallengeLevelList();

		final int MAX_ELEMENTS = 21;

		if (this.pageIndex < 0)
		{
			this.pageIndex = levelList.size() / MAX_ELEMENTS;
		}
		else if (this.pageIndex > (levelList.size() / MAX_ELEMENTS))
		{
			this.pageIndex = 0;
		}

		int levelIndex = MAX_ELEMENTS * this.pageIndex;

		// I want first row to be only for navigation and return button.
		int index = 10;

		while (levelIndex < ((this.pageIndex + 1) * MAX_ELEMENTS) &&
			levelIndex < levelList.size() &&
			index < 36)
		{
			if (!panelBuilder.slotOccupied(index))
			{
				panelBuilder.item(index, this.createLevelIcon(levelList.get(levelIndex++)));
			}

			index++;
		}

		// Navigation buttons only if necessary
		if (levelList.size() > MAX_ELEMENTS)
		{
			panelBuilder.item(18, this.getButton(CommonButtons.PREVIOUS));
			panelBuilder.item(26, this.getButton(CommonButtons.NEXT));
		}

		panelBuilder.item(44, this.returnButton);

		panelBuilder.build();
	}


	/**
	 * This method creates button for given level
	 * @param challengeLevel Level which button must be created.
	 * @return Level button.
	 */
	private PanelItem createLevelIcon(ChallengeLevel challengeLevel)
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
				new ConfirmationGUI(this.user, value -> {
					if (value)
					{
						this.addon.getChallengesManager().
							deleteChallengeLevel(challengeLevel);
					}
				});
				return true;
			});
		}

		return itemBuilder.build();
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
