package world.bentobox.challenges.panel.util;


import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;


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
			name(this.user.getTranslation("challenges.gui.admin.select-block"));


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
		int index = 9;

		while (entitiesIndex < ((correctPage + 1) * MAX_ELEMENTS) &&
			entitiesIndex < this.elements.size())
		{
			if (!panelBuilder.slotOccupied(index))
			{
				panelBuilder.item(index, this.createMaterialButton(this.elements.get(entitiesIndex++)));
			}

			index++;
		}

		// Add navigation Buttons
		panelBuilder.item(3,
			new PanelItemBuilder().
				icon(Material.SIGN).
				name(this.user.getTranslation("challenges.gui.buttons.previous")).
				clickHandler( (panel, user1, clickType, slot) -> {
					this.build(correctPage - 1);
					return true;
				}).build());

		panelBuilder.item(4,
			new PanelItemBuilder().
				icon(Material.OAK_DOOR).
				name(this.user.getTranslation("challenges.gui.buttons.return")).
				clickHandler( (panel, user1, clickType, slot) -> {
					this.consumer.accept(false, null);
					return true;
				}).build());

		panelBuilder.item(5,
			new PanelItemBuilder().
				icon(Material.SIGN).
				name(this.user.getTranslation("challenges.gui.buttons.next")).
				clickHandler( (panel, user1, clickType, slot) -> {
					this.build(correctPage + 1);
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
		PanelItemBuilder builder = new PanelItemBuilder().
			name(WordUtils.capitalize(material.name().toLowerCase().replace("_", " ")));

		// Process items that cannot be item-stacks.
		if (material.name().contains("_WALL"))
		{
			// Materials that is attached to wall cannot be showed in GUI. But they should be in list.
			builder.icon(Material.getMaterial(material.name().replace("WALL_", "")));
			builder.glow(true);
		}
		else if (material.name().startsWith("POTTED_"))
		{
			// Materials Potted elements cannot be in inventory.
			builder.icon(Material.getMaterial(material.name().replace("POTTED_", "")));
			builder.glow(true);
		}
		else if (material.name().startsWith("POTTED_"))
		{
			// Materials Potted elements cannot be in inventory.
			builder.icon(Material.getMaterial(material.name().replace("POTTED_", "")));
			builder.glow(true);
		}
		else if (material.equals(Material.MELON_STEM) || material.equals(Material.ATTACHED_MELON_STEM))
		{
			builder.icon(Material.MELON_SEEDS);
			builder.glow(true);
		}
		else if (material.equals(Material.PUMPKIN_STEM) || material.equals(Material.ATTACHED_PUMPKIN_STEM))
		{
			builder.icon(Material.PUMPKIN_SEEDS);
			builder.glow(true);
		}
		else if (material.equals(Material.TALL_SEAGRASS))
		{
			builder.icon(Material.SEAGRASS);
			builder.glow(true);
		}
		else if (material.equals(Material.CARROTS))
		{
			builder.icon(Material.CARROT);
			builder.glow(true);
		}
		else if (material.equals(Material.BEETROOTS))
		{
			builder.icon(Material.BEETROOT);
			builder.glow(true);
		}
		else if (material.equals(Material.POTATOES))
		{
			builder.icon(Material.POTATO);
			builder.glow(true);
		}
		else if (material.equals(Material.COCOA))
		{
			builder.icon(Material.COCOA_BEANS);
			builder.glow(true);
		}
		else if (material.equals(Material.KELP_PLANT))
		{
			builder.icon(Material.KELP);
			builder.glow(true);
		}
		else if (material.equals(Material.REDSTONE_WIRE))
		{
			builder.icon(Material.REDSTONE);
			builder.glow(true);
		}
		else if (material.equals(Material.TRIPWIRE))
		{
			builder.icon(Material.STRING);
			builder.glow(true);
		}
		else if (material.equals(Material.FROSTED_ICE))
		{
			builder.icon(Material.ICE);
			builder.glow(true);
		}
		else if (material.equals(Material.END_PORTAL) || material.equals(Material.END_GATEWAY) || material.equals(Material.NETHER_PORTAL))
		{
			builder.icon(Material.PAPER);
			builder.glow(true);
		}
		else if (material.equals(Material.BUBBLE_COLUMN) || material.equals(Material.WATER))
		{
			builder.icon(Material.WATER_BUCKET);
			builder.glow(true);
		}
		else if (material.equals(Material.LAVA))
		{
			builder.icon(Material.LAVA_BUCKET);
			builder.glow(true);
		}
		else if (material.equals(Material.FIRE))
		{
			builder.icon(Material.FIRE_CHARGE);
			builder.glow(true);
		}
		else
		{
			builder.icon(material);
			builder.glow(false);
		}

		builder.clickHandler((panel, user1, clickType, slot) -> {
			this.consumer.accept(true, material);
			return true;
		});

		return builder.build();
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
