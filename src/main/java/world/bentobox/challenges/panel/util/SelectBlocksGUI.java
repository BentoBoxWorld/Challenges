package world.bentobox.challenges.panel.util;


import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.utils.GuiUtils;


/**
 * This class contains all necessary things that allows to select single block from all ingame blocks. Selected
 * block will be returned via BiConsumer.
 */
public class SelectBlocksGUI
{
	public SelectBlocksGUI(User user, BiConsumer<Boolean, Material> consumer)
	{
		this(user, Collections.emptySet(), consumer);
	}


	public SelectBlocksGUI(User user, Set<Material> excludedMaterial, BiConsumer<Boolean, Material> consumer)
	{
		this.consumer = consumer;
		this.user = user;

		// Current GUI cannot display air blocks. It crashes with null-pointer
		excludedMaterial.add(Material.AIR);
		excludedMaterial.add(Material.CAVE_AIR);
		excludedMaterial.add(Material.VOID_AIR);

		// Piston head and moving piston is not necessary. useless.
		excludedMaterial.add(Material.PISTON_HEAD);
		excludedMaterial.add(Material.MOVING_PISTON);

		// Barrier cannot be accessible to user.
		excludedMaterial.add(Material.BARRIER);

		this.elements = new ArrayList<>();

		for (Material material : Material.values())
		{
			if (material.isBlock() && !material.isLegacy() && !excludedMaterial.contains(material))
			{
				this.elements.add(material);
			}
		}

		this.build(0);
	}


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


	/**
	 * This method builds all necessary elements in GUI panel.
	 */
	public void build(int pageIndex)
	{
		PanelBuilder panelBuilder = new PanelBuilder().user(this.user).
			name(this.user.getTranslation("challenges.gui.title.admin.select-block"));

		GuiUtils.fillBorder(panelBuilder, Material.BLUE_STAINED_GLASS_PANE);

		final int MAX_ELEMENTS = 21;
		final int correctPage;

		if (pageIndex < 0)
		{
			correctPage = this.elements.size() / MAX_ELEMENTS;
		}
		else if (pageIndex > (this.elements.size() / MAX_ELEMENTS))
		{
			correctPage = 0;
		}
		else
		{
			correctPage = pageIndex;
		}

		int entitiesIndex = MAX_ELEMENTS * correctPage;

		// I want first row to be only for navigation and return button.
		int index = 10;

		while (entitiesIndex < ((correctPage + 1) * MAX_ELEMENTS) &&
			entitiesIndex < this.elements.size())
		{
			if (!panelBuilder.slotOccupied(index))
			{
				panelBuilder.item(index, this.createMaterialButton(this.elements.get(entitiesIndex++)));
			}

			index++;
		}

		panelBuilder.item(4,
			new PanelItemBuilder().
				icon(Material.RED_STAINED_GLASS_PANE).
				name(this.user.getTranslation("challenges.gui.buttons.admin.cancel")).
				clickHandler( (panel, user1, clickType, slot) -> {
					this.consumer.accept(false, null);
					return true;
				}).build());

		if (this.elements.size() > MAX_ELEMENTS)
		{
			// Navigation buttons if necessary

			panelBuilder.item(18,
				new PanelItemBuilder().
					icon(Material.SIGN).
					name(this.user.getTranslation("challenges.gui.buttons.previous")).
					clickHandler((panel, user1, clickType, slot) -> {
						this.build(correctPage - 1);
						return true;
					}).build());

			panelBuilder.item(26,
				new PanelItemBuilder().
					icon(Material.SIGN).
					name(this.user.getTranslation("challenges.gui.buttons.next")).
					clickHandler((panel, user1, clickType, slot) -> {
						this.build(correctPage + 1);
						return true;
					}).build());
		}

		panelBuilder.item(44,
			new PanelItemBuilder().
				icon(Material.OAK_DOOR).
				name(this.user.getTranslation("challenges.gui.buttons.return")).
				clickHandler( (panel, user1, clickType, slot) -> {
					this.consumer.accept(false, null);
					return true;
				}).build());

		panelBuilder.build();
	}


	/**
	 * This method creates PanelItem that represents given material.
	 * Some materials is not displayable in Inventory GUI, so they are replaced with "placeholder" items.
	 * @param material Material which icon must be created.
	 * @return PanelItem that represents given material.
	 */
	private PanelItem createMaterialButton(Material material)
	{
		ItemStack itemStack = GuiUtils.getMaterialItem(material);

		return new PanelItemBuilder().
			name(WordUtils.capitalize(material.name().toLowerCase().replace("_", " "))).
			icon(itemStack).
			clickHandler((panel, user1, clickType, slot) -> {
				this.consumer.accept(true, material);
				return true;
			}).
			glow(!itemStack.getType().equals(material)).
			build();
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

	/**
	 * List with elements that will be displayed in current GUI.
	 */
	private List<Material> elements;

	/**
	 * This variable stores consumer.
	 */
	private BiConsumer<Boolean, Material> consumer;

	/**
	 * User who runs GUI.
	 */
	private User user;
}
