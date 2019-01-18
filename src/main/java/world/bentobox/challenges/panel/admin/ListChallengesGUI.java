package world.bentobox.challenges.panel.admin;


import org.bukkit.World;
import java.util.List;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.database.object.Challenges;
import world.bentobox.challenges.panel.CommonGUI;
import world.bentobox.challenges.panel.util.ConfirmationGUI;


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

		List<Challenges> challengeList = this.addon.getChallengesManager().getChallengesList();

		int MAX_ELEMENTS = 45;
		if (this.pageIndex < 0)
		{
			this.pageIndex = 0;
		}
		else if (this.pageIndex > (challengeList.size() / MAX_ELEMENTS))
		{
			this.pageIndex = challengeList.size() / MAX_ELEMENTS;
		}

		int challengeIndex = MAX_ELEMENTS * this.pageIndex;

		while (challengeIndex < ((this.pageIndex + 1) * MAX_ELEMENTS) &&
			challengeIndex < challengeList.size())
		{
			panelBuilder.item(this.createChallengeIcon(challengeList.get(challengeIndex)));
			challengeIndex++;
		}

		int nextIndex = challengeIndex % MAX_ELEMENTS == 0 ?
			MAX_ELEMENTS :
			(((challengeIndex % MAX_ELEMENTS) - 1) / 9 + 1) * 9;

		if (challengeIndex > MAX_ELEMENTS)
		{
			panelBuilder.item(nextIndex + 2, this.getButton(CommonButtons.PREVIOUS));
		}

		if (challengeIndex < challengeList.size())
		{
			panelBuilder.item(nextIndex + 6, this.getButton(CommonButtons.NEXT));
		}

		panelBuilder.item(nextIndex + 8, this.returnButton);

		panelBuilder.build();
	}


	/**
	 * This method creates button for given challenge.
	 * @param challenge Challenge which button must be created.
	 * @return Challenge button.
	 */
	private PanelItem createChallengeIcon(Challenges challenge)
	{
		PanelItemBuilder itemBuilder = new PanelItemBuilder().
			name(challenge.getFriendlyName()).
			description(challenge.getDescription()).
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
