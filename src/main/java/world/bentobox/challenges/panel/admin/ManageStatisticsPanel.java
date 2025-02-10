package world.bentobox.challenges.panel.admin;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.panels.Panel;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.challenges.database.object.requirements.StatisticRequirements.StatisticRec;
import world.bentobox.challenges.panel.CommonPagedPanel;
import world.bentobox.challenges.panel.CommonPanel;
import world.bentobox.challenges.panel.ConversationUtils;
import world.bentobox.challenges.panel.admin.EditChallengePanel.RequirementButton;
import world.bentobox.challenges.panel.util.SingleBlockSelector;
import world.bentobox.challenges.panel.util.SingleEntitySelector;
import world.bentobox.challenges.panel.util.StatisticSelector;
import world.bentobox.challenges.utils.Constants;
import world.bentobox.challenges.utils.Utils;


/**
 * This class allows to edit material that are in required material map.
 */
public class ManageStatisticsPanel extends CommonPagedPanel<StatisticRec>
{

    // ---------------------------------------------------------------------
    // Section: Enums
    // ---------------------------------------------------------------------

    /**
     * Functional buttons in current GUI.
     */
    private enum Button {
        ADD_STATISTIC, REMOVE_STATISTIC
    }

    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------

    /**
     * Contains selected stats.
     */
    private final Set<StatisticRec> selectedStats;

    /**
     * List of required statistics
     */
    private final List<StatisticRec> statisticsList;

    /**
     * Stores filtered items.
     */
    private List<StatisticRec> filterElements;

    private ManageStatisticsPanel(CommonPanel parentGUI, List<StatisticRec> statisticsList)
	{
		super(parentGUI);
        this.statisticsList = statisticsList;

        // Sort tags by their ordinal value.
        this.statisticsList.sort(Comparator.comparing(tag -> tag.statistic().getKey().getKey()));

        this.selectedStats = new HashSet<>();

		// Init without filters applied.
		this.filterElements = this.statisticsList;
	}


	/**
	 * Open the Challenges Admin GUI.
	 */
    public static void open(CommonPanel parentGUI, List<StatisticRec> statisticsList) {
        new ManageStatisticsPanel(parentGUI, statisticsList).build();
	}


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


	/**
	 * This method is called when filter value is updated.
	 */
	@Override
	protected void updateFilters()
	{
		if (this.searchString == null || this.searchString.isBlank())
		{
			this.filterElements = this.statisticsList;
		}
		else
		{
			this.filterElements = this.statisticsList.stream().
				filter(element -> {
					// If element name is set and name contains search field, then do not filter out.
                        return element.statistic().getKey().getKey().toLowerCase(Locale.ENGLISH)
                                .contains(this.searchString.toLowerCase(Locale.ENGLISH));
				}).
				distinct().
				collect(Collectors.toList());
		}
	}


	/**
	 * This method builds all necessary elements in GUI panel.
	 */
	@Override
	protected void build()
	{
		PanelBuilder panelBuilder = new PanelBuilder().user(this.user).
                name(this.user.getTranslation(Constants.TITLE + "manage-statistics"));

		// Create nice border.
		PanelUtils.fillBorder(panelBuilder);

        panelBuilder.item(3, this.createButton(Button.ADD_STATISTIC));
        panelBuilder.item(5, this.createButton(Button.REMOVE_STATISTIC));
        // Fill the box with what is selected
		this.populateElements(panelBuilder, this.filterElements);

		// Add return button.
		panelBuilder.item(44, this.returnButton);

		panelBuilder.build();
	}


	/**
	 * This method creates PanelItem button of requested type.
	 * @param button Button which must be created.
	 * @return new PanelItem with requested functionality.
	 */
	private PanelItem createButton(Button button)
	{
		final String reference = Constants.BUTTON + button.name().toLowerCase() + ".";

        final String name = this.user.getTranslation(reference + "name");
		final List<String> description = new ArrayList<>(3);
		description.add(this.user.getTranslation(reference + "description"));

		ItemStack icon;
		PanelItem.ClickHandler clickHandler;
		boolean glow;

		switch (button)
		{
        case ADD_STATISTIC -> {
				icon = new ItemStack(Material.BUCKET);
				clickHandler = (panel, user1, clickType, slot) ->
				{
                    StatisticSelector.open(this.user, (status, statistic) ->
						{
							if (status)
							{
                            StatisticRec newItem = new StatisticRec(statistic, null, null, 0, false);
                            this.statisticsList.add(newItem);

							}

							this.build();
						});
					return true;
				};
				glow = false;

				description.add("");
				description.add(this.user.getTranslation(Constants.TIPS + "click-to-add"));
			}
            case REMOVE_STATISTIC -> {

                if (!this.selectedStats.isEmpty())
				{
					description.add(this.user.getTranslation(reference + "title"));
                    this.selectedStats.forEach(stat ->
                    description.add(this.user.getTranslation(reference + "statistic_element", "[statistic]",
                            Utils.prettifyObject(stat.statistic(), this.user))));
				}

				icon = new ItemStack(Material.LAVA_BUCKET);

				clickHandler = (panel, user1, clickType, slot) ->
				{
                    if (!this.selectedStats.isEmpty())
					{
                        this.statisticsList.removeAll(this.selectedStats);
                        this.selectedStats.clear();
						this.build();
					}

					return true;
				};

                glow = !this.selectedStats.isEmpty();

				description.add("");
				description.add(this.user.getTranslation(Constants.TIPS + "click-to-remove"));
			}
			default -> {
				icon = new ItemStack(Material.PAPER);
				clickHandler = null;
				glow = false;
			}
		}

		return new PanelItemBuilder().
			icon(icon).
			name(name).
			description(description).
			clickHandler(clickHandler).
			glow(glow).
			build();
	}


	/**
     * This method creates button for given stat.
     * @param rec material which button must be created.
     * @return new Button for material.
     */
	@Override
    protected PanelItem createElementButton(StatisticRec rec)
	{
        final String reference = Constants.BUTTON + "statistic_element.";

		List<String> description = new ArrayList<>();

        // Show everything about this statistic
        // Type is not shown to the user, just used to decide what to show
        switch (rec.statistic().getType()) {
        case BLOCK:
            description.add(this.user.getTranslation(reference + "block", "[block]",
                    Utils.prettifyObject(rec.material(), this.user)));
            break;
        case ENTITY:
            description.add(this.user.getTranslation(reference + "entity", "[entity]",
                    Utils.prettifyObject(rec.entity(), this.user)));
            break;
        case ITEM:
            description.add(this.user.getTranslation(reference + "item", "[item]",
                    Utils.prettifyObject(rec.material(), this.user)));
            break;
        default:
            break;
        }
        // Amount
        description.add(this.user.getTranslation(reference + "amount", Constants.PARAMETER_NUMBER,
                String.valueOf(Objects.requireNonNullElse(rec.amount(), 0))));
        // Removal
        description.add(this.user.getTranslation(reference + "remove.name", "[value]",
                this.user.getTranslation(reference + "remove.value."
                        + (Objects.requireNonNullElse(rec.reduceStatistic(), false) ? "enabled" : "disabled"))));

        if (this.selectedStats.contains(rec))
		{
			description.add(this.user.getTranslation(reference + "selected"));
		}

		description.add("");
		description.add(this.user.getTranslation(Constants.TIPS + "left-click-to-choose"));

        if (this.selectedStats.contains(rec))
		{
			description.add(this.user.getTranslation(Constants.TIPS + "right-click-to-deselect"));
		}
		else
		{
			description.add(this.user.getTranslation(Constants.TIPS + "right-click-to-select"));
		}

		return new PanelItemBuilder().
                name(this.user.getTranslation(reference + "name", "[statistic]",
                        Utils.prettifyObject(rec.statistic(), this.user)))
                .icon(getStatisticIcon(rec.statistic())).
			description(description).
			clickHandler((panel, user1, clickType, slot) -> {
				// On right click change which entities are selected for deletion.
				if (clickType.isRightClick())
				{
                        if (!this.selectedStats.add(rec))
					{
						// Remove material if it is already selected
                            this.selectedStats.remove(rec);
					}

					this.build();
				}
				else
				{
                        // Left click
                        this.buildStatisticPanel(rec);
				}
				return true;
			}).
                glow(this.selectedStats.contains(rec)).
			build();
	}

    /**
     * Get an icon for a Statistic. Hand selected!
     * @param stat Statistic
     * @return ItemStack icon
     */
    public static ItemStack getStatisticIcon(Statistic stat) {
        return switch (stat) {
        case ANIMALS_BRED -> new ItemStack(Material.WHEAT);
        case ARMOR_CLEANED -> new ItemStack(Material.LEATHER_CHESTPLATE);
        case AVIATE_ONE_CM -> new ItemStack(Material.ELYTRA);
        case BANNER_CLEANED -> new ItemStack(Material.RED_BANNER);
        case BEACON_INTERACTION -> new ItemStack(Material.BEACON);
        case BELL_RING -> new ItemStack(Material.BELL);
        case BOAT_ONE_CM -> new ItemStack(Material.OAK_BOAT);
        case BREAK_ITEM -> new ItemStack(Material.AMETHYST_SHARD);
        case BREWINGSTAND_INTERACTION -> new ItemStack(Material.BREWING_STAND);
        case CAKE_SLICES_EATEN -> new ItemStack(Material.CAKE);
        case CAULDRON_FILLED -> new ItemStack(Material.CAULDRON);
        case CAULDRON_USED -> new ItemStack(Material.CAULDRON);
        case CHEST_OPENED -> new ItemStack(Material.CHEST);
        case CLEAN_SHULKER_BOX -> new ItemStack(Material.SHULKER_BOX);
        case CLIMB_ONE_CM -> new ItemStack(Material.LADDER);
        case CRAFTING_TABLE_INTERACTION -> new ItemStack(Material.CRAFTING_TABLE);
        case CRAFT_ITEM -> new ItemStack(Material.CRAFTING_TABLE);
        case CROUCH_ONE_CM -> new ItemStack(Material.PAPER);
        case DAMAGE_ABSORBED -> new ItemStack(Material.IRON_SWORD);
        case DAMAGE_BLOCKED_BY_SHIELD -> new ItemStack(Material.SHIELD);
        case DAMAGE_DEALT -> new ItemStack(Material.DIAMOND_SWORD);
        case DAMAGE_DEALT_ABSORBED -> new ItemStack(Material.GOLDEN_SWORD);
        case DAMAGE_DEALT_RESISTED -> new ItemStack(Material.WOODEN_SWORD);
        case DAMAGE_RESISTED -> new ItemStack(Material.IRON_HELMET);
        case DAMAGE_TAKEN -> new ItemStack(Material.IRON_LEGGINGS);
        case DEATHS -> new ItemStack(Material.OBSIDIAN);
        case DISPENSER_INSPECTED -> new ItemStack(Material.DISPENSER);
        case DROP -> new ItemStack(Material.PAPER);
        case DROPPER_INSPECTED -> new ItemStack(Material.DROPPER);
        case DROP_COUNT -> new ItemStack(Material.PAPER);
        case ENDERCHEST_OPENED -> new ItemStack(Material.ENDER_CHEST);
        case ENTITY_KILLED_BY -> new ItemStack(Material.BOW);
        case FALL_ONE_CM -> new ItemStack(Material.PAPER);
        case FISH_CAUGHT -> new ItemStack(Material.FISHING_ROD);
        case FLOWER_POTTED -> new ItemStack(Material.FLOWER_POT);
        case FLY_ONE_CM -> new ItemStack(Material.ELYTRA);
        case FURNACE_INTERACTION -> new ItemStack(Material.FURNACE);
        case HOPPER_INSPECTED -> new ItemStack(Material.HOPPER);
        case HORSE_ONE_CM -> new ItemStack(Material.IRON_HORSE_ARMOR);
        case INTERACT_WITH_ANVIL -> new ItemStack(Material.ANVIL);
        case INTERACT_WITH_BLAST_FURNACE -> new ItemStack(Material.BLAST_FURNACE);
        case INTERACT_WITH_CAMPFIRE -> new ItemStack(Material.CAMPFIRE);
        case INTERACT_WITH_CARTOGRAPHY_TABLE -> new ItemStack(Material.CARTOGRAPHY_TABLE);
        case INTERACT_WITH_GRINDSTONE -> new ItemStack(Material.GRINDSTONE);
        case INTERACT_WITH_LECTERN -> new ItemStack(Material.LECTERN);
        case INTERACT_WITH_LOOM -> new ItemStack(Material.LOOM);
        case INTERACT_WITH_SMITHING_TABLE -> new ItemStack(Material.SMITHING_TABLE);
        case INTERACT_WITH_SMOKER -> new ItemStack(Material.SMOKER);
        case INTERACT_WITH_STONECUTTER -> new ItemStack(Material.STONECUTTER);
        case ITEM_ENCHANTED -> new ItemStack(Material.ENCHANTED_GOLDEN_APPLE);
        case JUMP -> new ItemStack(Material.RABBIT_FOOT);
        case KILL_ENTITY -> new ItemStack(Material.DIAMOND_SWORD);
        case LEAVE_GAME -> new ItemStack(Material.PAPER);
        case MINECART_ONE_CM -> new ItemStack(Material.MINECART);
        case MINE_BLOCK -> new ItemStack(Material.STONE);
        case MOB_KILLS -> new ItemStack(Material.PAPER);
        case NOTEBLOCK_PLAYED -> new ItemStack(Material.NOTE_BLOCK);
        case NOTEBLOCK_TUNED -> new ItemStack(Material.NOTE_BLOCK);
        case OPEN_BARREL -> new ItemStack(Material.BARREL);
        case PICKUP -> new ItemStack(Material.PAPER);
        case PIG_ONE_CM -> new ItemStack(Material.PIG_SPAWN_EGG);
        case PLAYER_KILLS -> new ItemStack(Material.PLAYER_HEAD);
        case PLAY_ONE_MINUTE -> new ItemStack(Material.CLOCK);
        case RAID_TRIGGER -> new ItemStack(Material.OMINOUS_BOTTLE);
        case RAID_WIN -> new ItemStack(Material.GOLD_INGOT);
        case RECORD_PLAYED -> new ItemStack(Material.MUSIC_DISC_BLOCKS);
        case SHULKER_BOX_OPENED -> new ItemStack(Material.SHULKER_BOX);
        case SLEEP_IN_BED -> new ItemStack(Material.RED_BED);
        case SNEAK_TIME -> new ItemStack(Material.PAPER);
        case SPRINT_ONE_CM -> new ItemStack(Material.PAPER);
        case STRIDER_ONE_CM -> new ItemStack(Material.STRIDER_SPAWN_EGG);
        case SWIM_ONE_CM -> new ItemStack(Material.WATER_BUCKET);
        case TALKED_TO_VILLAGER -> new ItemStack(Material.EMERALD);
        case TARGET_HIT -> new ItemStack(Material.TARGET);
        case TIME_SINCE_DEATH -> new ItemStack(Material.CLOCK);
        case TIME_SINCE_REST -> new ItemStack(Material.CLOCK);
        case TOTAL_WORLD_TIME -> new ItemStack(Material.CLOCK);
        case TRADED_WITH_VILLAGER -> new ItemStack(Material.EMERALD);
        case TRAPPED_CHEST_TRIGGERED -> new ItemStack(Material.TRAPPED_CHEST);
        case USE_ITEM -> new ItemStack(Material.STICK);
        case WALK_ONE_CM -> new ItemStack(Material.LEATHER_BOOTS);
        case WALK_ON_WATER_ONE_CM -> new ItemStack(Material.LILY_PAD);
        case WALK_UNDER_WATER_ONE_CM -> new ItemStack(Material.WATER_BUCKET);
        default -> new ItemStack(Material.PAPER);

        };
    }

    private Panel buildStatisticPanel(StatisticRec req) {

        PanelBuilder panelBuilder = new PanelBuilder().user(this.user);

        panelBuilder.name(this.user.getTranslation(Constants.TITLE + "statistic-selector"));

        PanelUtils.fillBorder(panelBuilder);

        panelBuilder.item(10, this.createStatisticRequirementButton(RequirementButton.STATISTIC, req));
        panelBuilder.item(19, this.createStatisticRequirementButton(RequirementButton.REMOVE_STATISTIC, req));

        panelBuilder.item(11, this.createStatisticRequirementButton(RequirementButton.STATISTIC_AMOUNT, req));
        switch (req.statistic().getType()) {
        case BLOCK:
            panelBuilder.item(13, this.createStatisticRequirementButton(RequirementButton.STATISTIC_BLOCKS, req));
            break;
        case ENTITY:
            panelBuilder.item(13, this.createStatisticRequirementButton(RequirementButton.STATISTIC_ENTITIES, req));
            break;
        case ITEM:
            panelBuilder.item(13, this.createStatisticRequirementButton(RequirementButton.STATISTIC_ITEMS, req));
            break;
        case UNTYPED:
            break;
        default:
            break;

        }
        panelBuilder.item(25, this.createStatisticRequirementButton(RequirementButton.REQUIRED_PERMISSIONS, req));
        panelBuilder.item(44, this.returnButton);
        return panelBuilder.build();
    }

    /**
     * Creates a button for statistic requirements.
     * 
     * @param button Button that must be created.
     * @param req 
     * @return PanelItem button.
     */
    private PanelItem createStatisticRequirementButton(RequirementButton button, StatisticRec req) {

        final String reference = Constants.BUTTON + button.name().toLowerCase(Locale.ENGLISH) + ".";
        final String name = this.user.getTranslation(reference + "name");
        final List<String> description = new ArrayList<>(3);
        description.add(this.user.getTranslation(reference + "description"));

        ItemStack icon;
        boolean glow;
        PanelItem.ClickHandler clickHandler;

        switch (button) {
        case STATISTIC -> {
            description.add(this.user.getTranslation(reference + "value", "[statistic]",
                    Utils.prettifyObject(req.statistic(), this.user)));

            icon = new ItemStack(req.statistic() == null ? Material.BARRIER : Material.PAPER);
            clickHandler = (panel, user, clickType, slot) -> {
                StatisticSelector.open(this.user, (status, statistic) -> {
                    if (status) {
                        // Replace the old with the new
                        statisticsList.removeIf(sr -> sr.equals(req));
                        statisticsList.add(new StatisticRec(statistic, null, null, 0, false));
                    }
                    this.build();
                });
                return true;
            };
            glow = false;

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
        }
        case STATISTIC_AMOUNT -> {
            description.add(this.user.getTranslation(reference + "value", Constants.PARAMETER_NUMBER,
                    String.valueOf(req.amount())));
            icon = new ItemStack(Material.CHEST);
            clickHandler = (panel, user, clickType, i) -> {
                Consumer<Number> numberConsumer = number -> {
                    if (number != null) {
                        // Replace the old with the new
                        statisticsList.removeIf(sr -> sr.equals(req));
                        statisticsList.add(new StatisticRec(req.statistic(), req.entity(), req.material(),
                                number.intValue(), req.reduceStatistic()));
                    }

                    // reopen panel
                    this.build();
                };
                ConversationUtils.createNumericInput(numberConsumer, this.user,
                        this.user.getTranslation(Constants.CONVERSATIONS + "input-number"), 0, Integer.MAX_VALUE);

                return true;
            };
            glow = false;

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
        }
        case REMOVE_STATISTIC -> {
            description.add(this.user.getTranslation(reference + (req.reduceStatistic() ? "enabled" : "disabled")));

            icon = new ItemStack(Material.LEVER);
            clickHandler = (panel, user, clickType, slot) -> {
                // Replace the old with the new
                statisticsList.removeIf(sr -> sr.equals(req));
                statisticsList.add(new StatisticRec(req.statistic(), req.entity(), req.material(), req.amount(),
                        !req.reduceStatistic()));

                this.build();
                return true;
            };
            glow = req.reduceStatistic();

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-toggle"));
        }
        case STATISTIC_BLOCKS -> {
            description.add(this.user.getTranslation(reference + "value", "[block]",
                    Utils.prettifyObject(req.material(), this.user)));

            icon = req.material() == null ? new ItemStack(Material.BARRIER) : new ItemStack(req.material());
            clickHandler = (panel, user, clickType, slot) -> {
                SingleBlockSelector.open(this.user, SingleBlockSelector.Mode.BLOCKS, (status, block) -> {
                    if (status) {
                        // Replace the old with the new
                        statisticsList.removeIf(sr -> sr.equals(req));
                        statisticsList.add(new StatisticRec(req.statistic(), req.entity(), block, req.amount(),
                                req.reduceStatistic()));

                    }

                    this.build();
                });

                return true;
            };
            glow = false;

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
        }
        case STATISTIC_ITEMS -> {
            description.add(this.user.getTranslation(reference + "value", "[item]",
                    Utils.prettifyObject(req.material(), this.user)));

            icon = req.material() == null ? new ItemStack(Material.BARRIER) : new ItemStack(req.material());
            clickHandler = (panel, user, clickType, slot) -> {
                SingleBlockSelector.open(this.user, SingleBlockSelector.Mode.ITEMS, (status, block) -> {
                    if (status) {
                        // Replace the old with the new
                        statisticsList.removeIf(sr -> sr.equals(req));
                        statisticsList.add(new StatisticRec(req.statistic(), req.entity(), block, req.amount(),
                                req.reduceStatistic()));
                    }

                    this.build();
                });

                return true;
            };
            glow = false;

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
        }
        case STATISTIC_ENTITIES -> {
            description.add(this.user.getTranslation(reference + "value", "[entity]",
                    Utils.prettifyObject(req.entity(), this.user)));

            icon = req.entity() == null ? new ItemStack(Material.BARRIER)
                    : new ItemStack(PanelUtils.getEntityEgg(req.entity()));
            clickHandler = (panel, user, clickType, slot) -> {
                SingleEntitySelector.open(this.user, true, (status, entity) -> {
                    if (status) {
                        // Replace the old with the new
                        statisticsList.removeIf(sr -> sr.equals(req));
                        statisticsList.add(new StatisticRec(req.statistic(), entity, req.material(), req.amount(),
                                req.reduceStatistic()));
                    }

                    this.build();
                });

                return true;
            };
            glow = false;

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
        }
        default -> {
            icon = new ItemStack(Material.PAPER);
            clickHandler = null;
            glow = false;
        }
        }
        return new PanelItemBuilder().icon(icon).name(name).description(description).glow(glow)
                .clickHandler(clickHandler).build();
    }


}
