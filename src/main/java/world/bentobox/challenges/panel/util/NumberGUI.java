package world.bentobox.challenges.panel.util;


import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

import net.wesjd.anvilgui.AnvilGUI;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.utils.GuiUtils;


/**
 * This gui allows to change current number and returns it to previous GUI
 */
public class NumberGUI
{
	public NumberGUI(User user, int value, BiConsumer<Boolean, Integer> consumer)
	{
		this(user, value, Integer.MIN_VALUE, Integer.MAX_VALUE, consumer);
	}


	public NumberGUI(User user, int value, int minValue, BiConsumer<Boolean, Integer> consumer)
	{
		this(user, value, minValue, Integer.MAX_VALUE, consumer);
	}


	public NumberGUI(User user, int value, int minValue, int maxValue, BiConsumer<Boolean, Integer> consumer)
	{
		this.user = user;
		this.value = value;
		this.consumer = consumer;

		this.minValue = minValue;
		this.maxValue = maxValue;

		this.currentOperation = Button.SET;

		this.build();
	}


	/**
	 * This method builds panel that allows to change given number value.
	 */
	private void build()
	{
		PanelBuilder panelBuilder = new PanelBuilder().user(this.user).name(this.user.getTranslation("challenges.gui.edit-number-title"));

		GuiUtils.fillBorder(panelBuilder);

		// Others
		panelBuilder.item(1, this.getButton(Button.SAVE));

		panelBuilder.item(19, this.getButton(Button.VALUE));
		panelBuilder.item(44, this.getButton(Button.CANCEL));

		panelBuilder.item(2, this.getButton(Button.INPUT));

		// operations
		panelBuilder.item(3, this.getButton(Button.SET));
		panelBuilder.item(4, this.getButton(Button.INCREASE));
		panelBuilder.item(5, this.getButton(Button.REDUCE));
		panelBuilder.item(6, this.getButton(Button.MULTIPLY));

		// Numbers
		panelBuilder.item(11, this.createNumberButton(1));
		panelBuilder.item(12, this.createNumberButton(10));
		panelBuilder.item(13, this.createNumberButton(100));
		panelBuilder.item(14, this.createNumberButton(1000));
		panelBuilder.item(15, this.createNumberButton(10000));

		panelBuilder.item(20, this.createNumberButton(2));
		panelBuilder.item(21, this.createNumberButton(20));
		panelBuilder.item(22, this.createNumberButton(200));
		panelBuilder.item(23, this.createNumberButton(2000));
		panelBuilder.item(24, this.createNumberButton(20000));

		panelBuilder.item(29, this.createNumberButton(5));
		panelBuilder.item(30, this.createNumberButton(50));
		panelBuilder.item(31, this.createNumberButton(500));
		panelBuilder.item(32, this.createNumberButton(5000));
		panelBuilder.item(33, this.createNumberButton(50000));

		panelBuilder.build();
	}


	/**
	 * This method creates PanelItem with required functionality.
	 * @param button Functionality requirement.
	 * @return PanelItem with functionality.
	 */
	private PanelItem getButton(Button button)
	{
		ItemStack icon;
		String name;
		List<String> description;
		PanelItem.ClickHandler clickHandler;
		boolean glow;

		switch (button)
		{
			case SAVE:
			{
				name = this.user.getTranslation("challenges.gui.buttons.save");
				description = Collections.emptyList();
				icon = new ItemStack(Material.COMMAND_BLOCK);
				clickHandler = (panel, user, clickType, slot) -> {
					this.consumer.accept(true, this.value);
					return true;
				};
				glow = false;
				break;
			}
			case CANCEL:
			{
				name = this.user.getTranslation("challenges.gui.buttons.cancel");
				description = Collections.emptyList();
				icon = new ItemStack(Material.OAK_DOOR);
				clickHandler = (panel, user, clickType, slot) -> {
					this.consumer.accept(false, this.value);
					return true;
				};
				glow = false;
				break;
			}
			case INPUT:
			{
				name = this.user.getTranslation("challenges.gui.buttons.input");
				description = Collections.emptyList();
				icon = new ItemStack(Material.ANVIL);
				clickHandler = (panel, user, clickType, slot) -> {
					new AnvilGUI(BentoBox.getInstance(),
						this.user.getPlayer(),
						Integer.toString(this.value),
						(player, reply) -> {
							try
							{
								this.value = Integer.parseInt(reply);

								if (this.value > this.maxValue || this.value < this.minValue)
								{
									this.user.sendMessage("challenges.error.not-valid-integer",
										"[value]", reply,
										"[min]", Integer.toString(this.minValue),
										"[max]", Integer.toString(this.maxValue));
								}
								else
								{
									this.build();
								}
							}
							catch (Exception e)
							{
								reply = Integer.toString(this.value);
								this.user.sendMessage("challenges.error.not-a-integer", "[value]", reply);
							}

							return reply;
						});

					return true;
				};
				glow = false;
				break;
			}
			case VALUE:
			{
				name = this.user.getTranslation("challenges.gui.buttons.value");
				description = Collections.singletonList(Integer.toString(this.value));
				icon = new ItemStack(Material.PAPER);
				clickHandler = (panel, user, clickType, slot) -> true;
				glow = false;
				break;
			}
			case SET:
			{
				name = this.user.getTranslation("challenges.gui.buttons.set");
				description = Collections.emptyList();
				icon = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
				clickHandler = (panel, user, clickType, slot) -> {
					this.currentOperation = Button.SET;
					this.build();
					return true;
				};
				glow = this.currentOperation.equals(Button.SET);
				break;
			}
			case INCREASE:
			{
				name = this.user.getTranslation("challenges.gui.buttons.increase");
				description = Collections.emptyList();
				icon = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
				clickHandler = (panel, user, clickType, slot) -> {
					this.currentOperation = Button.INCREASE;
					this.build();
					return true;
				};
				glow = this.currentOperation.equals(Button.INCREASE);
				break;
			}
			case REDUCE:
			{
				name = this.user.getTranslation("challenges.gui.buttons.reduce");
				description = Collections.emptyList();
				icon = new ItemStack(Material.RED_STAINED_GLASS_PANE);
				clickHandler = (panel, user, clickType, slot) -> {
					this.currentOperation = Button.REDUCE;
					this.build();
					return true;
				};
				glow = this.currentOperation.equals(Button.REDUCE);
				break;
			}
			case MULTIPLY:
			{
				name = this.user.getTranslation("challenges.gui.buttons.multiply");
				description = Collections.emptyList();
				icon = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
				clickHandler = (panel, user, clickType, slot) -> {
					this.currentOperation = Button.MULTIPLY;
					this.build();
					return true;
				};
				glow = this.currentOperation.equals(Button.MULTIPLY);
				break;
			}
			default:
				return null;
		}

		return new PanelItem(icon, name, description, glow, clickHandler, false);
	}


	/**
	 * This method creates Number Button based on input number.
	 * @param number Number which button must be created.
	 * @return PanelItem that represents number button.
	 */
	private PanelItem createNumberButton(int number)
	{
		PanelItemBuilder itemBuilder = new PanelItemBuilder();

		switch (this.currentOperation)
		{
			case SET:
			{
				itemBuilder.name(this.user.getTranslation("biomes.gui.buttons.set","[number]", Integer.toString(number)));
				itemBuilder.icon(Material.WHITE_STAINED_GLASS_PANE);
				itemBuilder.clickHandler((panel, user1, clickType, i) -> {
					this.value = number;

					if (this.value > this.maxValue)
					{
						this.user.sendMessage("challenges.error.not-valid-integer",
							"[value]", Integer.toString(this.value),
							"[min]", Integer.toString(this.minValue),
							"[max]", Integer.toString(this.maxValue));

						this.value = this.maxValue;
					}

					if (this.value < this.minValue)
					{
						this.user.sendMessage("challenges.error.not-valid-integer",
							"[value]", Integer.toString(this.value),
							"[min]", Integer.toString(this.minValue),
							"[max]", Integer.toString(this.maxValue));

						this.value = this.minValue;
					}

					this.build();
					return true;
				});

				break;
			}
			case INCREASE:
			{
				itemBuilder.name(this.user.getTranslation("biomes.gui.buttons.increase","[number]", Integer.toString(number)));
				itemBuilder.icon(Material.GREEN_STAINED_GLASS_PANE);
				itemBuilder.clickHandler((panel, user1, clickType, i) -> {
					this.value += number;

					if (this.value > this.maxValue)
					{
						this.user.sendMessage("challenges.error.not-valid-integer",
							"[value]", Integer.toString(this.value),
							"[min]", Integer.toString(this.minValue),
							"[max]", Integer.toString(this.maxValue));

						this.value = this.maxValue;
					}

					this.build();
					return true;
				});

				break;
			}
			case REDUCE:
			{
				itemBuilder.name(this.user.getTranslation("biomes.gui.buttons.reduce","[number]", Integer.toString(number)));
				itemBuilder.icon(Material.RED_STAINED_GLASS_PANE);
				itemBuilder.clickHandler((panel, user1, clickType, i) -> {
					this.value -= number;

					if (this.value < this.minValue)
					{
						this.user.sendMessage("challenges.error.not-valid-integer",
							"[value]", Integer.toString(this.value),
							"[min]", Integer.toString(this.minValue),
							"[max]", Integer.toString(this.maxValue));

						this.value = this.minValue;
					}

					this.build();
					return true;
				});

				break;
			}
			case MULTIPLY:
			{
				itemBuilder.name(this.user.getTranslation("biomes.gui.buttons.multiply","[number]", Integer.toString(number)));
				itemBuilder.icon(Material.BLUE_STAINED_GLASS_PANE);
				itemBuilder.clickHandler((panel, user1, clickType, i) -> {
					this.value *= number;

					if (this.value > this.maxValue)
					{
						this.user.sendMessage("challenges.error.not-valid-integer",
							"[value]", Integer.toString(this.value),
							"[min]", Integer.toString(this.minValue),
							"[max]", Integer.toString(this.maxValue));

						this.value = this.maxValue;
					}

					this.build();
					return true;
				});

				break;
			}
		}

		return itemBuilder.build();
	}


// ---------------------------------------------------------------------
// Section: Enums
// ---------------------------------------------------------------------


	/**
	 * This enum contains all button types.
	 */
	private enum Button
	{
		SAVE,
		CANCEL,
		INPUT,

		VALUE,

		SET,
		INCREASE,
		REDUCE,
		MULTIPLY
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

	/**
	 * This variable stores current GUI consumer.
	 */
	private BiConsumer<Boolean, Integer> consumer;

	/**
	 * User who runs GUI.
	 */
	private User user;

	/**
	 * Current value.
	 */
	private int value;

	/**
	 * Minimal value that is allowed to set.
	 */
	private int minValue;

	/**
	 * Maximal value that is allowed to set.
	 */
	private int maxValue;

	/**
	 * This variable holds which operation now is processed.
	 */
	private Button currentOperation;
}
