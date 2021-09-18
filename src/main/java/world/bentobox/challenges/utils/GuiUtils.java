package world.bentobox.challenges.utils;


import java.util.*;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;


/**
 * This class contains static methods that is used through multiple GUIs.
 */
public class GuiUtils
{
// ---------------------------------------------------------------------
// Section: Border around GUIs
// ---------------------------------------------------------------------


	/**
	 * This method creates border of black panes around given panel with 5 rows.
	 * @param panelBuilder PanelBuilder which must be filled with border blocks.
	 */
	public static void fillBorder(PanelBuilder panelBuilder)
	{
		GuiUtils.fillBorder(panelBuilder, 5, Material.BLACK_STAINED_GLASS_PANE);
	}


	/**
	 * This method sets black stained glass pane around Panel with given row count.
	 * @param panelBuilder object that builds Panel.
	 * @param rowCount in Panel.
	 */
	public static void fillBorder(PanelBuilder panelBuilder, int rowCount)
	{
		GuiUtils.fillBorder(panelBuilder, rowCount, Material.BLACK_STAINED_GLASS_PANE);
	}


	/**
	 * This method sets blocks with given Material around Panel with 5 rows.
	 * @param panelBuilder object that builds Panel.
	 * @param material that will be around Panel.
	 */
	public static void fillBorder(PanelBuilder panelBuilder, Material material)
	{
		GuiUtils.fillBorder(panelBuilder, 5, material);
	}


	/**
	 * This method sets blocks with given Material around Panel with given row count.
	 * @param panelBuilder object that builds Panel.
	 * @param rowCount in Panel.
	 * @param material that will be around Panel.
	 */
	public static void fillBorder(PanelBuilder panelBuilder, int rowCount, Material material)
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
				panelBuilder.item(i, BorderBlock.getPanelBorder(material));
			}
		}
	}


// ---------------------------------------------------------------------
// Section: ItemStack transformations
// ---------------------------------------------------------------------

	/**
	 * This method transforms entity into egg or block that corresponds given entity.
	 * If entity egg is not found, then it is replaced by block that represents entity or
	 * barrier block.
	 * @param entity Entity which egg must be returned.
	 * @return ItemStack that may be egg for given entity.
	 */
	public static ItemStack getEntityEgg(EntityType entity)
	{
		return GuiUtils.getEntityEgg(entity, 1);
	}


	/**
	 * This method transforms entity into egg or block that corresponds given entity.
	 * If entity egg is not found, then it is replaced by block that represents entity or
	 * barrier block.
	 * @param entity Entity which egg must be returned.
	 * @param amount Amount of ItemStack elements.
	 * @return ItemStack that may be egg for given entity.
	 */
	public static ItemStack getEntityEgg(EntityType entity, int amount)
	{
		ItemStack itemStack;

		switch (entity)
		{
			case ENDER_DRAGON:
				itemStack = new ItemStack(Material.DRAGON_EGG);
				break;
			case WITHER:
				itemStack = new ItemStack(Material.SOUL_SAND);
				break;
			case PLAYER:
				itemStack = new ItemStack(Material.PLAYER_HEAD);
				break;
			case MUSHROOM_COW:
				itemStack = new ItemStack(Material.MOOSHROOM_SPAWN_EGG);
				break;
			case SNOWMAN:
				itemStack = new ItemStack(Material.CARVED_PUMPKIN);
				break;
			case IRON_GOLEM:
				itemStack = new ItemStack(Material.IRON_BLOCK);
				break;
			case ARMOR_STAND:
				itemStack = new ItemStack(Material.ARMOR_STAND);
				break;
			default:
				Material material = Material.getMaterial(entity.name() + "_SPAWN_EGG");

				if (material == null)
				{
					itemStack = new ItemStack(Material.BARRIER);
				}
				else
				{
					itemStack = new ItemStack(material);
				}

				break;
		}

		if (entity.name().equals("PIG_ZOMBIE"))
		{
			// If pig zombie exist, then pigman spawn egg exists too.
			itemStack = new ItemStack(Material.getMaterial("ZOMBIE_PIGMAN_SPAWN_EGG"));
		}

		itemStack.setAmount(amount);

		return itemStack;
	}


	/**
	 * This method transforms entity into player head with skin that corresponds given
	 * entity. If entity head is not found, then it is replaced by barrier block.
	 * @param entity Entity which head must be returned.
	 * @return ItemStack that may be head for given entity.
	 */
	public static ItemStack getEntityHead(EntityType entity)
	{
		return GuiUtils.getEntityHead(entity, 1);
	}


	/**
	 * This method transforms entity into player head with skin that corresponds given
	 * entity. If entity head is not found, then it is replaced by barrier block.
	 * @param entity Entity which head must be returned.
	 * @param amount Amount of ItemStack elements.
	 * @return ItemStack that may be head for given entity.
	 */
	public static ItemStack getEntityHead(EntityType entity, int amount)
	{
		ItemStack itemStack;

		switch (entity)
		{
			case PLAYER:
				itemStack = new ItemStack(Material.PLAYER_HEAD);
				break;
			case WITHER_SKELETON:
				itemStack = new ItemStack(Material.WITHER_SKELETON_SKULL);
				break;
			case ARMOR_STAND:
				itemStack = new ItemStack(Material.ARMOR_STAND);
				break;
			case SKELETON:
				itemStack = new ItemStack(Material.SKELETON_SKULL);
				break;
			case GIANT:
			case ZOMBIE:
				itemStack = new ItemStack(Material.ZOMBIE_HEAD);
				break;
			case CREEPER:
				itemStack = new ItemStack(Material.CREEPER_HEAD);
				break;
			case ENDER_DRAGON:
				itemStack = new ItemStack(Material.DRAGON_HEAD);
				break;
			default:
				HeadLib head = HeadLib.getHead(entity.name());

				if (head == null)
				{
					itemStack = new ItemStack(Material.BARRIER);
				}
				else
				{
					itemStack = head.toItemStack();
				}
				break;
		}

		itemStack.setAmount(amount);

		return itemStack;
	}


	/**
	 * This method transforms material into item stack that can be displayed in users
	 * inventory.
	 * @param material Material which item stack must be returned.
	 * @return ItemStack that represents given material.
	 */
	public static ItemStack getMaterialItem(Material material)
	{
		return GuiUtils.getMaterialItem(material, 1);
	}


	/**
	 * This method transforms material into item stack that can be displayed in users
	 * inventory.
	 * @param material Material which item stack must be returned.
	 * @param amount Amount of ItemStack elements.
	 * @return ItemStack that represents given material.
	 */
	public static ItemStack getMaterialItem(Material material, int amount)
	{
		ItemStack itemStack;

		// Process items that cannot be item-stacks.
		if (material.name().contains("WALL_"))
		{
			// Materials that is attached to wall cannot be showed in GUI. But they should be in list.
			itemStack = new ItemStack(Material.getMaterial(material.name().replace("WALL_", "")));
		}
		else if (material.name().startsWith("POTTED_"))
		{
			// Materials Potted elements cannot be in inventory.
			itemStack = new ItemStack(Material.getMaterial(material.name().replace("POTTED_", "")));
		}
		else if (material.equals(Material.MELON_STEM) || material.equals(Material.ATTACHED_MELON_STEM))
		{
			itemStack = new ItemStack(Material.MELON_SEEDS);
		}
		else if (material.equals(Material.PUMPKIN_STEM) || material.equals(Material.ATTACHED_PUMPKIN_STEM))
		{
			itemStack = new ItemStack(Material.PUMPKIN_SEEDS);
		}
		else if (material.equals(Material.TALL_SEAGRASS))
		{
			itemStack = new ItemStack(Material.SEAGRASS);
		}
		else if (material.equals(Material.CARROTS))
		{
			itemStack = new ItemStack(Material.CARROT);
		}
		else if (material.equals(Material.BEETROOTS))
		{
			itemStack = new ItemStack(Material.BEETROOT);
		}
		else if (material.equals(Material.POTATOES))
		{
			itemStack = new ItemStack(Material.POTATO);
		}
		else if (material.equals(Material.COCOA))
		{
			itemStack = new ItemStack(Material.COCOA_BEANS);
		}
		else if (material.equals(Material.KELP_PLANT))
		{
			itemStack = new ItemStack(Material.KELP);
		}
		else if (material.equals(Material.REDSTONE_WIRE))
		{
			itemStack = new ItemStack(Material.REDSTONE);
		}
		else if (material.equals(Material.TRIPWIRE))
		{
			itemStack = new ItemStack(Material.STRING);
		}
		else if (material.equals(Material.FROSTED_ICE))
		{
			itemStack = new ItemStack(Material.ICE);
		}
		else if (material.equals(Material.END_PORTAL) || material.equals(Material.END_GATEWAY) || material.equals(Material.NETHER_PORTAL))
		{
			itemStack = new ItemStack(Material.PAPER);
		}
		else if (material.equals(Material.BUBBLE_COLUMN) || material.equals(Material.WATER))
		{
			itemStack = new ItemStack(Material.WATER_BUCKET);
		}
		else if (material.equals(Material.LAVA))
		{
			itemStack = new ItemStack(Material.LAVA_BUCKET);
		}
		else if (material.equals(Material.FIRE))
		{
			itemStack = new ItemStack(Material.FIRE_CHARGE);
		}
		else if (material.equals(Material.AIR) || material.equals(Material.CAVE_AIR) || material.equals(Material.VOID_AIR))
		{
			itemStack = new ItemStack(Material.GLASS_BOTTLE);
		}
		else if (material.equals(Material.PISTON_HEAD) || material.equals(Material.MOVING_PISTON))
		{
			itemStack = new ItemStack(Material.PISTON);
		}
		else
		{
			itemStack = new ItemStack(material);
		}

		itemStack.setAmount(amount);

		return itemStack;
	}


	/**
	 * This BorderBlock is simple PanelItem but without item meta data.
	 */
	private static class BorderBlock extends PanelItem
	{
		private BorderBlock(ItemStack icon)
		{
			super(new PanelItemBuilder().
				icon(icon.clone()).
				name(" ").
				description(Collections.emptyList()).
				glow(false).
				clickHandler(null));
		}


		/**
		 * This method retunrs BorderBlock with requested item stack.
		 * @param material of which broder must be created.
		 * @return PanelItem that acts like border.
		 */
		private static BorderBlock getPanelBorder(Material material)
		{
			ItemStack itemStack = new ItemStack(material);
			itemStack.getItemMeta().setDisplayName("&r&8");

			return new BorderBlock(itemStack);
		}
	}
}