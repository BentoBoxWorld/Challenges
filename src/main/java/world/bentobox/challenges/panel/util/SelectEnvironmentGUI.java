package world.bentobox.challenges.panel.util;


import org.bukkit.Material;
import org.bukkit.World;
import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;

import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.utils.GuiUtils;


/**
 * This class creates panel that allows to select and deselect World Environments. On save it runs
 * input consumer with true and selected values.
 */
public class SelectEnvironmentGUI
{
	public SelectEnvironmentGUI(User user, Set<World.Environment> values, BiConsumer<Boolean, Set<World.Environment>> consumer)
	{
		this.user = user;
		this.values = values;
		this.consumer = consumer;

		this.build();
	}


	/**
	 * This method builds environment select panel.
	 */
	private void build()
	{
		PanelBuilder panelBuilder = new PanelBuilder().user(this.user).name(this.user.getTranslation("challenges.gui.admin.environment-title"));

		GuiUtils.fillBorder(panelBuilder, Material.BLUE_STAINED_GLASS_PANE);

		panelBuilder.item(3, new PanelItemBuilder().
			name(this.user.getTranslation("challenges.gui.admin.buttons.save")).
			icon(Material.GREEN_STAINED_GLASS_PANE).
			clickHandler((panel, user1, clickType, index) -> {
				this.consumer.accept(true, this.values);
				return true;
			}).
			build());

		panelBuilder.item(5, new PanelItemBuilder().
			name(this.user.getTranslation("challenges.gui.admin.buttons.cancel")).
			icon(Material.RED_STAINED_GLASS_PANE).
			clickHandler((panel, user1, clickType, i) -> {
				this.consumer.accept(false, Collections.emptySet());
				return true;
			}).
			build());

		panelBuilder.item(20, new PanelItemBuilder().
			name(this.user.getTranslation("challenges.gui.admin.buttons.nether")).
			icon(Material.NETHERRACK).
			clickHandler((panel, user1, clickType, i) -> {
				if (this.values.contains(World.Environment.NETHER))
				{
					this.values.remove(World.Environment.NETHER);
				}
				else
				{
					this.values.add(World.Environment.NETHER);
				}

				this.build();
				return true;
			}).
			glow(this.values.contains(World.Environment.NETHER)).
			build());
		panelBuilder.item(22, new PanelItemBuilder().
			name(this.user.getTranslation("challenges.gui.admin.buttons.normal")).
			icon(Material.DIRT).
			clickHandler((panel, user1, clickType, i) -> {
				if (this.values.contains(World.Environment.NORMAL))
				{
					this.values.remove(World.Environment.NORMAL);
				}
				else
				{
					this.values.add(World.Environment.NORMAL);
				}

				this.build();
				return true;
			}).
			glow(this.values.contains(World.Environment.NORMAL)).
			build());
		panelBuilder.item(24, new PanelItemBuilder().
			name(this.user.getTranslation("challenges.gui.admin.buttons.end")).
			icon(Material.END_STONE).
			clickHandler((panel, user1, clickType, i) -> {
				if (this.values.contains(World.Environment.THE_END))
				{
					this.values.remove(World.Environment.THE_END);
				}
				else
				{
					this.values.add(World.Environment.THE_END);
				}

				this.build();
				return true;
			}).
			glow(this.values.contains(World.Environment.THE_END)).
			build());


		panelBuilder.item(44, new PanelItemBuilder().
			name(this.user.getTranslation("challenges.gui.admin.buttons.return")).
			icon(Material.OAK_DOOR).
			clickHandler((panel, user1, clickType, i) -> {
				this.consumer.accept(false, Collections.emptySet());
				return true;
			}).
			build());

		panelBuilder.build();
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

	/**
	 * User who wants to run command.
	 */
	private User user;

	/**
	 * List with selected environments.
	 */
	private Set<World.Environment> values;

	/**
	 * Stores current Consumer
	 */
	private BiConsumer<Boolean, Set<World.Environment>> consumer;

}
