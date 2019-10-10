package world.bentobox.challenges.panel.util;


import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.NonNull;

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
	public NumberGUI(User user, int value, int lineLength, BiConsumer<Boolean, Integer> consumer)
	{
		this(user, value, Integer.MIN_VALUE, Integer.MAX_VALUE, lineLength, consumer);
	}


	public NumberGUI(User user, int value, int minValue, int lineLength, BiConsumer<Boolean, Integer> consumer)
	{
		this(user, value, minValue, Integer.MAX_VALUE, lineLength, consumer);
	}


	public NumberGUI(User user, int value, int minValue, int maxValue, int lineLength, BiConsumer<Boolean, Integer> consumer)
	{
		this.user = user;
		this.value = value;
		this.consumer = consumer;

		this.minValue = minValue;
		this.maxValue = maxValue;

		this.currentOperation = Button.SET;

		this.lineLength = lineLength;

		this.build();
	}


	/**
	 * This method builds panel that allows to change given number value.
	 */
	private void build()
	{
		PanelBuilder panelBuilder = new PanelBuilder().user(this.user).name(this.user.getTranslation("challenges.gui.title.admin.manage-numbers"));

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
		String description;
		PanelItem.ClickHandler clickHandler;
		boolean glow;

		switch (button)
		{
			case SAVE:
			{
				name = this.user.getTranslation("challenges.gui.buttons.admin.save");
				description = this.user.getTranslation("challenges.gui.descriptions.admin.save");
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
				name = this.user.getTranslation("challenges.gui.buttons.admin.cancel");
				description = this.user.getTranslation("challenges.gui.descriptions.admin.cancel");
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
				name = this.user.getTranslation("challenges.gui.buttons.admin.input");
				description = this.user.getTranslation("challenges.gui.descriptions.admin.input");
				icon = new ItemStack(Material.ANVIL);
				clickHandler = (panel, user, clickType, slot) -> {

					this.getNumberInput(number -> {
						this.value = number.intValue();
						this.build();
						},
						this.user.getTranslation("challenges.gui.questions.admin.number"));

					return true;
				};
				glow = false;
				break;
			}
			case VALUE:
			{
				name = this.user.getTranslation("challenges.gui.buttons.admin.value");
				description = this.user.getTranslation("challenges.gui.descriptions.current-value", "[value]", Integer.toString(this.value));
				icon = new ItemStack(Material.PAPER);
				clickHandler = (panel, user, clickType, slot) -> true;
				glow = false;
				break;
			}
			case SET:
			{
				name = this.user.getTranslation("challenges.gui.buttons.admin.set");
				description = this.user.getTranslation("challenges.gui.descriptions.admin.set");
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
				name = this.user.getTranslation("challenges.gui.buttons.admin.increase");
				description = this.user.getTranslation("challenges.gui.descriptions.admin.increase");
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
				name = this.user.getTranslation("challenges.gui.buttons.admin.reduce");
				description = this.user.getTranslation("challenges.gui.descriptions.admin.reduce");
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
				name = this.user.getTranslation("challenges.gui.buttons.admin.multiply");
				description = this.user.getTranslation("challenges.gui.descriptions.admin.multiply");
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

		return new PanelItemBuilder().
			icon(icon).
			name(name).
			description(GuiUtils.stringSplit(description, this.lineLength)).
			glow(glow).
			clickHandler(clickHandler).
			build();
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
				itemBuilder.name(this.user.getTranslation("challenges.gui.buttons.admin.number","[number]", Integer.toString(number)));
				itemBuilder.icon(Material.WHITE_STAINED_GLASS_PANE);
				itemBuilder.clickHandler((panel, user1, clickType, i) -> {
					this.value = number;

					if (this.value > this.maxValue)
					{
						this.user.sendMessage("challenges.errors.not-valid-integer",
							"[value]", Integer.toString(this.value),
							"[min]", Integer.toString(this.minValue),
							"[max]", Integer.toString(this.maxValue));

						this.value = this.maxValue;
					}

					if (this.value < this.minValue)
					{
						this.user.sendMessage("challenges.errors.not-valid-integer",
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
				itemBuilder.name(this.user.getTranslation("challenges.gui.buttons.admin.number","[number]", Integer.toString(number)));
				itemBuilder.icon(Material.GREEN_STAINED_GLASS_PANE);
				itemBuilder.clickHandler((panel, user1, clickType, i) -> {
					this.value += number;

					if (this.value > this.maxValue)
					{
						this.user.sendMessage("challenges.errors.not-valid-integer",
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
				itemBuilder.name(this.user.getTranslation("challenges.gui.buttons.admin.number","[number]", Integer.toString(number)));
				itemBuilder.icon(Material.RED_STAINED_GLASS_PANE);
				itemBuilder.clickHandler((panel, user1, clickType, i) -> {
					this.value -= number;

					if (this.value < this.minValue)
					{
						this.user.sendMessage("challenges.errors.not-valid-integer",
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
				itemBuilder.name(this.user.getTranslation("challenges.gui.buttons.admin.number","[number]", Integer.toString(number)));
				itemBuilder.icon(Material.BLUE_STAINED_GLASS_PANE);
				itemBuilder.clickHandler((panel, user1, clickType, i) -> {
					this.value *= number;

					if (this.value > this.maxValue)
					{
						this.user.sendMessage("challenges.errors.not-valid-integer",
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
// Section: Conversation
// ---------------------------------------------------------------------


	/**
	 * This method will close opened gui and writes inputText in chat. After players answers on
	 * inputText in chat, message will trigger consumer and gui will reopen.
	 * @param consumer Consumer that accepts player output text.
	 * @param question Message that will be displayed in chat when player triggers conversion.
	 */
	private void getNumberInput(Consumer<Number> consumer, @NonNull String question)
	{
		final User user = this.user;

		Conversation conversation =
			new ConversationFactory(BentoBox.getInstance()).withFirstPrompt(
				new NumericPrompt()
				{
					/**
					 * Override this method to perform some action
					 * with the user's integer response.
					 *
					 * @param context Context information about the
					 * conversation.
					 * @param input The user's response as a {@link
					 * Number}.
					 * @return The next {@link Prompt} in the prompt
					 * graph.
					 */
					@Override
					protected Prompt acceptValidatedInput(ConversationContext context, Number input)
					{
						// Add answer to consumer.
						consumer.accept(input);
						// Reopen GUI
						NumberGUI.this.build();
						// End conversation
						return Prompt.END_OF_CONVERSATION;
					}


					/**
					 * Override this method to do further validation on the numeric player
					 * input after the input has been determined to actually be a number.
					 *
					 * @param context Context information about the conversation.
					 * @param input The number the player provided.
					 * @return The validity of the player's input.
					 */
					protected boolean isNumberValid(ConversationContext context, Number input)
					{
						return input.intValue() >= NumberGUI.this.minValue &&
							input.intValue() <= NumberGUI.this.maxValue;
					}


					/**
					 * Optionally override this method to display an additional message if the
					 * user enters an invalid number.
					 *
					 * @param context Context information about the conversation.
					 * @param invalidInput The invalid input provided by the user.
					 * @return A message explaining how to correct the input.
					 */
					@Override
					protected String getInputNotNumericText(ConversationContext context,
						String invalidInput)
					{
						return NumberGUI.this.user.getTranslation("challenges.errors.not-a-integer", "[value]", invalidInput);
					}


					/**
					 * Optionally override this method to display an additional message if the
					 * user enters an invalid numeric input.
					 *
					 * @param context Context information about the conversation.
					 * @param invalidInput The invalid input provided by the user.
					 * @return A message explaining how to correct the input.
					 */
					@Override
					protected String getFailedValidationText(ConversationContext context,
						Number invalidInput)
					{
						return NumberGUI.this.user.getTranslation("challenges.errors.not-valid-integer",
							"[value]", invalidInput.toString(),
							"[min]", Integer.toString(NumberGUI.this.minValue),
							"[max]", Integer.toString(NumberGUI.this.maxValue));
					}


					/**
					 * @see Prompt#getPromptText(ConversationContext)
					 */
					@Override
					public String getPromptText(ConversationContext conversationContext)
					{
						// Close input GUI.
						user.closeInventory();

						// There are no editable message. Just return question.
						return question;
					}
				}).
				withLocalEcho(false).
				withPrefix(context ->
					NumberGUI.this.user.getTranslation("challenges.gui.questions.prefix")).
				buildConversation(user.getPlayer());

		conversation.begin();
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

	/**
	 * This variable stores how large line can be, before warp it.
	 */
	private int lineLength;
}
