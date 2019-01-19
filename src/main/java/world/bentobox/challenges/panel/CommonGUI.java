package world.bentobox.challenges.panel;


import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import java.util.Collections;
import java.util.List;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
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


	/**
	 * This enum contains buttons that is offten used in multiple GUIs.
	 */
	protected enum CommonButtons
	{
		NEXT,
		PREVIOUS,
		RETURN
	}


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
	}


// ---------------------------------------------------------------------
// Section: Common methods
// ---------------------------------------------------------------------


	/**
	 * This method builds all necessary elements in GUI panel.
	 */
	public abstract void build();


	/**
	 * This method returns PanelItem that represents given Button.
	 * @param button Button that must be returned.
	 * @return PanelItem with requested functionality.
	 */
	protected PanelItem getButton(CommonButtons button)
	{
		ItemStack icon;
		String name;
		List<String> description;
		PanelItem.ClickHandler clickHandler;

		switch (button)
		{
			case NEXT:
			{
				name = this.user.getTranslation("challenges.gui.buttons.next");
				description = Collections.emptyList();
				icon = new ItemStack(Material.SIGN);
				clickHandler = (panel, user, clickType, slot) -> {
					this.pageIndex++;
					this.build();
					return true;
				};

				break;
			}
			case PREVIOUS:
			{
				name = this.user.getTranslation("challenges.gui.buttons.previous");
				description = Collections.emptyList();
				icon = new ItemStack(Material.SIGN);
				clickHandler = (panel, user, clickType, slot) -> {
					this.pageIndex--;
					this.build();
					return true;
				};

				break;
			}
			case RETURN:
				return this.returnButton;
			default:
				return null;
		}

		return new PanelItem(icon, name, description, false, clickHandler, false);
	}


	/**
	 * This method creates border of black panes around given panel with 5 rows.
	 * @param panelBuilder PanelBuilder which must be filled with border blocks.
	 */
	protected void fillBorder(PanelBuilder panelBuilder)
	{
		this.fillBorder(panelBuilder, 5, Material.BLACK_STAINED_GLASS_PANE);
	}


	/**
	 * This method sets black stained glass pane around Panel with given row count.
	 * @param panelBuilder object that builds Panel.
	 * @param rowCount in Panel.
	 */
	protected void fillBorder(PanelBuilder panelBuilder, int rowCount)
	{
		this.fillBorder(panelBuilder, rowCount, Material.BLACK_STAINED_GLASS_PANE);
	}


	/**
	 * This method sets blocks with given Material around Panel with 5 rows.
	 * @param panelBuilder object that builds Panel.
	 * @param material that will be around Panel.
	 */
	protected void fillBorder(PanelBuilder panelBuilder, Material material)
	{
		this.fillBorder(panelBuilder, 5, material);
	}


	/**
	 * This method sets blocks with given Material around Panel with given row count.
	 * @param panelBuilder object that builds Panel.
	 * @param rowCount in Panel.
	 * @param material that will be around Panel.
	 */
	protected void fillBorder(PanelBuilder panelBuilder, int rowCount, Material material)
	{
		// Only for useful filling.
		if (rowCount < 3)
		{
			return;
		}

		for (int i = 0; i < 9 * rowCount; i++)
		{
			// First (i < 9) and last (i > 35) rows must be filled
			// First column (i % 9 == 0) and last column (i % 9 == 8) also must be filled.

			if (i < 9 || i > 9 * (rowCount - 1) || i % 9 == 0 || i % 9 == 8)
			{
				panelBuilder.item(i, new PanelItemBuilder().name("&2").icon(material).build());
			}
		}
	}


	/**
	 * This method sets new value to ValueObject variable.
	 * @param value new Value of valueObject.
	 */
	public void setValue(Object value)
	{
		this.valueObject = value;
	}
}

