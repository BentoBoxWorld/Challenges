package world.bentobox.challenges.panel.admin;


import org.bukkit.Material;
import org.bukkit.World;
import java.util.List;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.database.object.Challenge;
import world.bentobox.challenges.panel.CommonGUI;
import world.bentobox.challenges.panel.util.ConfirmationGUI;
import world.bentobox.challenges.utils.GuiUtils;


/**
 * This class contains all necessary elements to create GUI that lists all challenges.
 * It allows to edit them or remove, depending on given input mode.
 */
public class ListChallengesGUI extends CommonGUI
{
	// ---------------------------------------------------------------------
// Section: Constructor
// ---------------------------------------------------------------------


	/**
	 * {@inheritDoc}
	 * @param mode - mode that indicate what should do icon clicking.
	 */
	public ListChallengesGUI(ChallengesAddon addon,
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
	public ListChallengesGUI(ChallengesAddon addon,
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
			this.user.getTranslation("challenges.gui.admin.choose-challenge-title"));

		if (this.currentMode.equals(Mode.DELETE))
		{
			GuiUtils.fillBorder(panelBuilder, Material.RED_STAINED_GLASS_PANE);
		}
		else
		{
			GuiUtils.fillBorder(panelBuilder);
		}

		List<Challenge> challengeList = this.addon.getChallengesManager().getAllChallenges(this.world);

		final int MAX_ELEMENTS = 21;

		if (this.pageIndex < 0)
		{
			this.pageIndex = challengeList.size() / MAX_ELEMENTS;
		}
		else if (this.pageIndex > (challengeList.size() / MAX_ELEMENTS))
		{
			this.pageIndex = 0;
		}

		int challengeIndex = MAX_ELEMENTS * this.pageIndex;

		// I want first row to be only for navigation and return button.
		int index = 10;

		while (challengeIndex < ((this.pageIndex + 1) * MAX_ELEMENTS) &&
			challengeIndex < challengeList.size() &&
			index < 36)
		{
			if (!panelBuilder.slotOccupied(index))
			{
				panelBuilder.item(index, this.createChallengeIcon(challengeList.get(challengeIndex++)));
			}

			index++;
		}

		// Navigation buttons only if necessary
		if (challengeList.size() > MAX_ELEMENTS)
		{
			panelBuilder.item(18, this.getButton(CommonButtons.PREVIOUS));
			panelBuilder.item(26, this.getButton(CommonButtons.NEXT));
		}

		panelBuilder.item(44, this.returnButton);

		panelBuilder.build();
	}


	/**
	 * This method creates button for given challenge.
	 * @param challenge Challenge which button must be created.
	 * @return Challenge button.
	 */
	private PanelItem createChallengeIcon(Challenge challenge)
	{
		PanelItemBuilder itemBuilder = new PanelItemBuilder().
			name(challenge.getFriendlyName()).
			description(GuiUtils.stringSplit(challenge.getDescription(),
				this.addon.getChallengesSettings().getLoreLineLength())).
			icon(challenge.getIcon()).
			glow(challenge.isDeployed());

		if (this.currentMode.equals(Mode.EDIT))
		{
			itemBuilder.clickHandler((panel, user1, clickType, i) -> {
				new EditChallengeGUI(this.addon,
					this.world,
					this.user,
					challenge,
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
						this.addon.getChallengesManager().deleteChallenge(challenge);
					}

					this.build();
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
