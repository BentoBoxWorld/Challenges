package world.bentobox.challenges.panel.admin;


import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import java.util.Collections;
import java.util.List;

import net.wesjd.anvilgui.AnvilGUI;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.panel.CommonGUI;
import world.bentobox.challenges.utils.GuiUtils;


/**
 * This class contains Main
 */
public class AdminGUI extends CommonGUI
{
// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

	/**
	 * This boolean holds if import should overwrite existing challenges.
	 */
	private boolean overwriteMode;

	/**
	 * This indicate if Reset Challenges must work as reset all.
	 */
	private boolean resetAllMode;


// ---------------------------------------------------------------------
// Section: Enums
// ---------------------------------------------------------------------


	/**
	 * This enum contains all button variations. Just for cleaner code.
	 */
	private enum Button
	{
		COMPLETE_USER_CHALLENGES,
		RESET_USER_CHALLENGES,
		ADD_CHALLENGE,
		ADD_LEVEL,
		EDIT_CHALLENGE,
		EDIT_LEVEL,
		DELETE_CHALLENGE,
		DELETE_LEVEL,
		IMPORT_CHALLENGES,
		BACKWARD_CHALLENGES,
		EDIT_SETTINGS
	}


// ---------------------------------------------------------------------
// Section: Constructor
// ---------------------------------------------------------------------


	/**
	 * {@inheritDoc}
	 */
	public AdminGUI(ChallengesAddon addon,
		World world,
		User user,
		String topLabel,
		String permissionPrefix)
	{
		super(addon, world, user, topLabel, permissionPrefix);
	}


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


	/**
	 * {@inheritDoc}
	 */
	public void build()
	{
		PanelBuilder panelBuilder = new PanelBuilder().user(this.user).name(
			this.user.getTranslation("challenges.admin.gui-title"));

		GuiUtils.fillBorder(panelBuilder);

		panelBuilder.item(10, this.createButton(Button.COMPLETE_USER_CHALLENGES));
		panelBuilder.item(19, this.createButton(Button.RESET_USER_CHALLENGES));

		// Add Challenges
		panelBuilder.item(12, this.createButton(Button.ADD_CHALLENGE));
		panelBuilder.item(13, this.createButton(Button.ADD_LEVEL));

		// Edit Challenges
		panelBuilder.item(21, this.createButton(Button.EDIT_CHALLENGE));
		panelBuilder.item(22, this.createButton(Button.EDIT_LEVEL));

		// Remove Challenges
		panelBuilder.item(30, this.createButton(Button.DELETE_CHALLENGE));
		panelBuilder.item(31, this.createButton(Button.DELETE_LEVEL));


		// Import Challenges
		panelBuilder.item(15, this.createButton(Button.IMPORT_CHALLENGES));
		panelBuilder.item(24, this.createButton(Button.BACKWARD_CHALLENGES));

		// Edit Addon Settings
		panelBuilder.item(16, this.createButton(Button.EDIT_SETTINGS));

		panelBuilder.item(44, this.returnButton);

		panelBuilder.build();
	}


	/**
	 * This method is used to create PanelItem for each button type.
	 * @param button Button which must be created.
	 * @return PanelItem with necessary functionality.
	 */
	private PanelItem createButton(Button button)
	{
		ItemStack icon;
		String name;
		List<String> description;
		boolean glow;
		PanelItem.ClickHandler clickHandler;

		String permissionSuffix;

		switch (button)
		{
			case COMPLETE_USER_CHALLENGES:
				permissionSuffix = COMPLETE;

				name = this.user.getTranslation("challenges.gui.admin.buttons.complete");
				description = Collections.emptyList();
				icon = new ItemStack(Material.WRITTEN_BOOK);
				clickHandler = (panel, user, clickType, slot) -> {
					new ListUsersGUI(this.addon,
						this.world,
						this.user,
						ListUsersGUI.Mode.COMPLETE,
						this.topLabel,
						this.permissionPrefix,
						this).build();

					return true;
				};
				glow = false;

				break;
			case RESET_USER_CHALLENGES:
				permissionSuffix = RESET;

				name = this.user.getTranslation("challenges.gui.admin.buttons.reset");
				description = Collections.emptyList();
				icon = new ItemStack(Material.WRITABLE_BOOK);

				glow = this.resetAllMode;

				clickHandler = (panel, user, clickType, slot) -> {
					if (clickType.isRightClick())
					{
						this.resetAllMode = !this.resetAllMode;
						this.build();
					}
					else
					{
						new ListUsersGUI(this.addon,
							this.world,
							this.user,
							this.resetAllMode ? ListUsersGUI.Mode.RESET_ALL : ListUsersGUI.Mode.RESET,
							this.topLabel,
							this.permissionPrefix,
							this).build();
					}

					return true;
				};

				break;
			case ADD_CHALLENGE:
				permissionSuffix = ADD;

				name = this.user.getTranslation("challenges.gui.admin.buttons.add-challenge");
				description = Collections.emptyList();
				icon = new ItemStack(Material.BOOK);
				clickHandler = (panel, user, clickType, slot) -> {
					new AnvilGUI(this.addon.getPlugin(),
						this.user.getPlayer(),
						"unique_id",
						(player, reply) -> {
							String newName = Util.getWorld(this.world).getName() + "_" + reply;

							if (!this.addon.getChallengesManager().containsChallenge(newName))
							{
								new EditChallengeGUI(this.addon,
									this.world,
									this.user,
									this.addon.getChallengesManager().createChallenge(newName),
									this.topLabel,
									this.permissionPrefix,
									this).build();
							}
							else
							{
								this.user.sendMessage("challenges.errors.unique-id", "[id]", reply);
							}

							return reply;
						});

					return true;
				};
				glow = false;

				break;
			case ADD_LEVEL:
				permissionSuffix = ADD;

				name = this.user.getTranslation("challenges.gui.admin.buttons.add-level");
				description = Collections.emptyList();
				icon = new ItemStack(Material.BOOK);
				clickHandler = (panel, user, clickType, slot) -> {
					new AnvilGUI(this.addon.getPlugin(),
						this.user.getPlayer(),
						"unique_id",
						(player, reply) -> {
							String newName = Util.getWorld(this.world).getName() + "_" + reply;

							if (!this.addon.getChallengesManager().containsLevel(newName))
							{
								new EditLevelGUI(this.addon,
									this.world,
									this.user,
									this.addon.getChallengesManager().createLevel(newName, this.world),
									this.topLabel,
									this.permissionPrefix,
									this).build();
							}
							else
							{
								this.user.sendMessage("challenges.errors.unique-id", "[id]", reply);
							}

							return reply;
						});

					return true;
				};
				glow = false;

				break;
			case EDIT_CHALLENGE:
				permissionSuffix = EDIT;

				name = this.user.getTranslation("challenges.gui.admin.buttons.edit-challenge");
				description = Collections.emptyList();
				icon = new ItemStack(Material.ANVIL);
				clickHandler = (panel, user, clickType, slot) -> {
					new ListChallengesGUI(this.addon,
						this.world,
						this.user,
						ListChallengesGUI.Mode.EDIT,
						this.topLabel,
						this.permissionPrefix,
						this).build();

					return true;
				};
				glow = false;

				break;
			case EDIT_LEVEL:
			{
				permissionSuffix = EDIT;

				name = this.user.getTranslation("challenges.gui.admin.buttons.edit-level");
				description = Collections.emptyList();
				icon = new ItemStack(Material.ANVIL);
				clickHandler = (panel, user, clickType, slot) -> {
					new ListLevelsGUI(this.addon,
						this.world,
						this.user,
						ListLevelsGUI.Mode.EDIT,
						this.topLabel,
						this.permissionPrefix,
						this).build();

					return true;
				};
				glow = false;

				break;
			}
			case DELETE_CHALLENGE:
			{
				permissionSuffix = DELETE;

				name = this.user.getTranslation("challenges.gui.admin.buttons.delete-challenge");
				description = Collections.emptyList();
				icon = new ItemStack(Material.LAVA_BUCKET);
				clickHandler = (panel, user, clickType, slot) -> {
					new ListChallengesGUI(this.addon,
						this.world,
						this.user,
						ListChallengesGUI.Mode.DELETE,
						this.topLabel,
						this.permissionPrefix,
						this).build();

					return true;
				};
				glow = false;

				break;
			}
			case DELETE_LEVEL:
			{
				permissionSuffix = DELETE;

				name = this.user.getTranslation("challenges.gui.admin.buttons.delete-level");
				description = Collections.emptyList();
				icon = new ItemStack(Material.LAVA_BUCKET);
				clickHandler = (panel, user, clickType, slot) -> {
					new ListLevelsGUI(this.addon,
						this.world,
						this.user,
						ListLevelsGUI.Mode.DELETE,
						this.topLabel,
						this.permissionPrefix,
						this).build();

					return true;
				};
				glow = false;

				break;
			}
			case IMPORT_CHALLENGES:
			{
				permissionSuffix = IMPORT;

				name = this.user.getTranslation("challenges.gui.admin.buttons.import");
				description = Collections.emptyList();
				icon = new ItemStack(Material.HOPPER);
				clickHandler = (panel, user, clickType, slot) -> {
					if (clickType.isRightClick())
					{
						this.overwriteMode = !this.overwriteMode;
						this.build();
					}
					else
					{
						// Run import command.
						this.user.performCommand(this.topLabel + " " + CHALLENGES + " " + IMPORT +
							(this.overwriteMode ? " overwrite" : ""));
					}
					return true;
				};
				glow = this.overwriteMode;

				break;
			}
			case BACKWARD_CHALLENGES:
			{
				permissionSuffix = IMPORT;

				name = this.user.getTranslation("challenges.gui.admin.buttons.backward");
				description = Collections.emptyList();
				icon = new ItemStack(Material.HOPPER);
				clickHandler = (panel, user, clickType, slot) -> {
					this.addon.getImportManager().
						importPreviousChallenges(this.user, this.world, false);

					return true;
				};
				glow = false;

				break;
			}
			case EDIT_SETTINGS:
			{
				permissionSuffix = SETTINGS;

				name = this.user.getTranslation("challenges.gui.admin.buttons.settings");
				description = Collections.emptyList();
				icon = new ItemStack(Material.CRAFTING_TABLE);
				clickHandler = (panel, user, clickType, slot) -> {
					new EditSettingsGUI(this.addon,
						this.world,
						this.user,
						this.topLabel,
						this.permissionPrefix,
						this).build();

					return true;
				};
				glow = false;

				break;
			}
			default:
				// This should never happen.
				return null;
		}

		// If user does not have permission to run command, then change icon and clickHandler.
		final String actionPermission = this.permissionPrefix + ADMIN + "." + CHALLENGES + "." + permissionSuffix;

		if (!this.user.hasPermission(actionPermission))
		{
			icon = new ItemStack(Material.BARRIER);
			clickHandler = (panel, user, clickType, slot) -> {
				this.user.sendMessage("general.errors.no-permission", "[permission]", actionPermission);
				return true;
			};
		}

		return new PanelItem(icon, name, description, glow, clickHandler, false);
	}
}