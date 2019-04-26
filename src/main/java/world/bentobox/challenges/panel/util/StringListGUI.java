package world.bentobox.challenges.panel.util;


import org.bukkit.Material;
import org.bukkit.conversations.*;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.wesjd.anvilgui.AnvilGUI;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.utils.GuiUtils;


/**
 * This GUI allows to edit List of strings. AnvilGUI has limited text space, so splitting
 * text in multiple rows allows to edit each row separately.
 */
public class StringListGUI
{
	public StringListGUI(User user, Collection<String> value, int lineLength, BiConsumer<Boolean, List<String>> consumer)
	{
		this(user, new ArrayList<>(value), lineLength, consumer);
	}


	public StringListGUI(User user, List<String> value, int lineLength, BiConsumer<Boolean, List<String>> consumer)
	{
		this.consumer = consumer;
		this.user = user;
		this.value = value;
		this.lineLength = lineLength;

		if (this.value.size() > 21)
		{
			// TODO: throw error that so large list cannot be edited.
			this.consumer.accept(false, this.value);
		}
		else
		{
			this.build();
		}
	}


	/**
	 * This method builds panel that allows to change given string value.
	 */
	private void build()
	{
		PanelBuilder panelBuilder = new PanelBuilder().user(this.user).
			name(this.user.getTranslation("challenges.gui.title.admin.edit-text-fields"));

		GuiUtils.fillBorder(panelBuilder, Material.BLACK_STAINED_GLASS_PANE);

		panelBuilder.item(1, this.getButton(Button.SAVE));
		panelBuilder.item(2, this.getButton(Button.VALUE));

		panelBuilder.item(4, this.getButton(Button.ADD));
		panelBuilder.item(5, this.getButton(Button.REMOVE));
		panelBuilder.item(6, this.getButton(Button.CLEAR));

		panelBuilder.item(8, this.getButton(Button.MODE));

		panelBuilder.item(44, this.getButton(Button.CANCEL));

		int slot = 10;

		for (int stringIndex = 0; stringIndex < this.value.size() && slot < 36; stringIndex++)
		{
			if (!panelBuilder.slotOccupied(slot))
			{
				panelBuilder.item(slot,
					this.createStringElement(this.value.get(stringIndex), stringIndex));
			}

			slot++;
		}

		panelBuilder.build();
	}


	/**
	 * This method create button that does some functionality in current gui.
	 * @param button Button functionality.
	 * @return PanelItem.
	 */
	private PanelItem getButton(Button button)
	{
		ItemStack icon;
		String name;
		List<String> description;
		PanelItem.ClickHandler clickHandler;

		switch (button)
		{
			case SAVE:
			{
				name = this.user.getTranslation("challenges.gui.buttons.admin.save");
				description = Collections.singletonList(this.user.getTranslation("challenges.gui.descriptions.admin.save"));
				icon = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
				clickHandler = (panel, user, clickType, slot) -> {
					this.consumer.accept(true, this.value);

					return true;
				};
				break;
			}
			case CANCEL:
			{
				name = this.user.getTranslation("challenges.gui.buttons.admin.cancel");
				description = Collections.singletonList(this.user.getTranslation("challenges.gui.descriptions.admin.cancel"));
				icon = new ItemStack(Material.OAK_DOOR);
				clickHandler = (panel, user, clickType, slot) -> {
					this.consumer.accept(false, this.value);

					return true;
				};
				break;
			}
			case VALUE:
			{
				name = this.user.getTranslation("challenges.gui.buttons.admin.value");
				description = new ArrayList<>();
				description.add(this.user.getTranslation("challenges.gui.descriptions.current-value", "[value]", ""));
				description.addAll(this.value);
				icon = new ItemStack(Material.PAPER);
				clickHandler = (panel, user, clickType, slot) -> true;
				break;
			}
			case ADD:
			{
				name = this.user.getTranslation("challenges.gui.buttons.admin.add");
				description = Collections.emptyList();
				icon = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
				clickHandler = (panel, user, clickType, slot) -> {

					if (this.useAnvil)
					{
						new AnvilGUI(BentoBox.getInstance(),
							this.user.getPlayer(),
							" ",
							(player, reply) -> {
								this.value.add(reply);
								this.build();
								return reply;
							});
					}
					else
					{
						this.startConversion(value ->
								this.value.add(value),
							this.user.getTranslation("challenges.gui.descriptions.admin.add-text-line"));
					}

					return true;
				};
				break;
			}
			case CLEAR:
			{
				name = this.user.getTranslation("challenges.gui.buttons.admin.clear");
				description = Collections.emptyList();
				icon = new ItemStack(Material.RED_STAINED_GLASS_PANE);
				clickHandler = (panel, user, clickType, slot) -> {
					this.value.clear();
					this.build();
					return true;
				};
				break;
			}
			case REMOVE:
			{
				name = this.user.getTranslation("challenges.gui.buttons.admin.remove-empty");
				description = Collections.emptyList();
				icon = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
				clickHandler = (panel, user, clickType, slot) -> {
					this.value.removeIf(String::isEmpty);

					this.build();
					return true;
				};
				break;
			}
			case MODE:
			{
				name = this.user.getTranslation("challenges.gui.buttons.admin.input-mode");
				description = Collections.singletonList(this.user.getTranslation("challenges.gui.descriptions.admin.input-mode"));
				icon = this.useAnvil ? new ItemStack(Material.ANVIL) : new ItemStack(Material.MAP);
				clickHandler = (panel, user, clickType, slot) -> {
					this.useAnvil = !this.useAnvil;
					panel.getInventory().setItem(slot, this.getButton(button).getItem());
					return true;
				};
				break;
			}
			default:
				return null;
		}

		return new PanelItemBuilder().
			icon(icon).
			name(name).
			description(GuiUtils.stringSplit(description, this.lineLength)).
			glow(false).
			clickHandler(clickHandler).
			build();
	}


	/**
	 * This method creates paper icon that represents single line from list.
	 * @param element Paper Icon name
	 * @return PanelItem.
	 */
	private PanelItem createStringElement(String element, int stringIndex)
	{
		return new PanelItemBuilder().
			name(element).
			icon(Material.PAPER).
			clickHandler((panel, user1, clickType, i) -> {

				if (this.useAnvil)
				{
					new AnvilGUI(BentoBox.getInstance(),
						this.user.getPlayer(),
						element,
						(player, reply) -> {
							this.value.set(stringIndex, reply);
							this.build();
							return reply;
						});
				}
				else
				{
					this.startConversion(
						value -> this.value.set(stringIndex, value),
						this.user.getTranslation("challenges.gui.descriptions.admin.edit-text-line"),
						element);
				}

			return true;
		}).build();
	}


	/**
	 * This method will close opened gui and writes inputText in chat. After players answers on inputText in
	 * chat, message will trigger consumer and gui will reopen.
	 * @param consumer Consumer that accepts player output text.
	 * @param question Message that will be displayed in chat when player triggers conversion.
	 */
	private void startConversion(Consumer<String> consumer, @NonNull String question)
	{
		this.startConversion(consumer, question, null);
	}


	/**
	 * This method will close opened gui and writes inputText in chat. After players answers on inputText in
	 * chat, message will trigger consumer and gui will reopen.
	 * @param consumer Consumer that accepts player output text.
	 * @param question Message that will be displayed in chat when player triggers conversion.
	 * @param message Message that will be set in player text field when clicked on question.
	 */
	private void startConversion(Consumer<String> consumer, @NonNull String question, @Nullable String message)
	{
		final User user = this.user;

		Conversation conversation =
			new ConversationFactory(BentoBox.getInstance()).withFirstPrompt(
				new StringPrompt()
				{
					/**
					 * @see Prompt#getPromptText(ConversationContext)
					 */
					@Override
					public String getPromptText(ConversationContext conversationContext)
					{
						// Close input GUI.
						user.closeInventory();

						if (message != null)
						{
							// Create Edit Text message.
							TextComponent component = new TextComponent(user.getTranslation("challenges.gui.descriptions.admin.click-to-edit"));
							component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, message));
							// Send question and message to player.
							user.getPlayer().spigot().sendMessage(component);
						}

						// There are no editable message. Just return question.
						return question;
					}


					/**
					 * @see Prompt#acceptInput(ConversationContext, String)
					 */
					@Override
					public Prompt acceptInput(ConversationContext conversationContext, String answer)
					{
						// Add answer to consumer.
						consumer.accept(answer);
						// Reopen GUI
						StringListGUI.this.build();
						// End conversation
						return Prompt.END_OF_CONVERSATION;
					}
				}).
				withLocalEcho(false).
				buildConversation(user.getPlayer());

		conversation.begin();
	}


// ---------------------------------------------------------------------
// Section: Enums
// ---------------------------------------------------------------------


	/**
	 * This enum holds all button values in current gui.
	 */
	private enum Button
	{
		VALUE,
		ADD,
		REMOVE,
		CANCEL,
		CLEAR,
		SAVE,
		MODE
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


	/**
	 * This variable stores consumer.
	 */
	private BiConsumer<Boolean, List<String>> consumer;

	/**
	 * User who runs GUI.
	 */
	private User user;

	/**
	 * Boolean that indicate if editing should happen in anvil.
	 */
	private boolean useAnvil;

	/**
	 * Current value.
	 */
	private List<String> value;

	/**
	 * This variable stores how large line can be, before warp it.
	 */
	private int lineLength;
}
