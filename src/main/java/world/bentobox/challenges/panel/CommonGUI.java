package world.bentobox.challenges.panel;


import org.bukkit.Material;
import org.bukkit.World;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.ChallengesAddon;


/**
 * This class contains common methods that will be used over all GUIs. It also allows
 * easier navigation between different GUIs.
 */
public abstract class CommonGUI
{
// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

	/**
	 * This variable stores parent gui.
	 */
	private CommonGUI parentGUI;

	/**
	 * Variable stores Challenges addon.
	 */
	protected ChallengesAddon addon;

	/**
	 * Variable stores world in which panel is referred to.
	 */
	protected World world;

	/**
	 * Variable stores user who created this panel.
	 */
	protected User user;

	/**
	 * Variable stores top label of command from which panel was called.
	 */
	protected String topLabel;

	/**
	 * Variable stores permission prefix of command from which panel was called.
	 */
	protected String permissionPrefix;

	/**
	 * Variable stores any value.
	 */
	protected Object valueObject;

	/**
	 * This object holds current page index.
	 */
	protected int pageIndex;

	/**
	 * This object holds PanelItem that allows to return to previous panel.
	 */
	protected PanelItem returnButton;


// ---------------------------------------------------------------------
// Section: Constants
// ---------------------------------------------------------------------


	protected static final String ADMIN = "admin";

	protected static final String CHALLENGES = "challenges";

	protected static final String IMPORT = "import";

	protected static final String SETTINGS = "settings";

	protected static final String DELETE = "delete";

	protected static final String EDIT = "edit";

	protected static final String ADD = "add";

	protected static final String RESET = "reset";

	protected static final String COMPLETE = "complete";


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
	public CommonGUI(ChallengesAddon addon,
		World world,
		User user,
		String topLabel,
		String permissionPrefix)
	{
		this(addon, world, user, topLabel, permissionPrefix, null);
	}


	/**
	 * Default constructor that inits panels with minimal requirements.
	 *
	 * @param addon Addon where panel operates.
	 * @param world World from which panel was created.
	 * @param user User who created panel.
	 * @param topLabel Command top label which creates panel (f.e. island or ai)
	 * @param permissionPrefix Command permission prefix (f.e. bskyblock.)
	 * @param parentGUI Parent panel for current panel.
	 */
	public CommonGUI(ChallengesAddon addon,
		World world,
		User user,
		String topLabel,
		String permissionPrefix,
		CommonGUI parentGUI)
	{
		this.addon = addon;
		this.world = world;
		this.user = user;

		this.topLabel = topLabel;
		this.permissionPrefix = permissionPrefix;

		this.parentGUI = parentGUI;

		this.pageIndex = 0;

		this.returnButton = new PanelItemBuilder().
			name(this.user.getTranslation("challenges.gui.buttons.back")).
			icon(Material.OAK_DOOR).
			clickHandler((panel, user1, clickType, i) -> {
				if (this.parentGUI == null)
				{
					this.user.closeInventory();
					return true;
				}

				this.parentGUI.build();
				return true;
			}).build();

		this.build();
	}


// ---------------------------------------------------------------------
// Section: Common methods
// ---------------------------------------------------------------------


	/**
	 * This method builds all necessary elements in GUI panel.
	 */
	public abstract void build();
}

