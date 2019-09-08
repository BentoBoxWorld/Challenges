//
// Created by BONNe
// Copyright - 2019
//


package world.bentobox.challenges.database.object.requirements;


import com.google.gson.annotations.Expose;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;


/**
 * This class contains all necessary requirements to complete inventory type challenge.
 */
public class InventoryRequirements extends Requirements
{
	/**
	 * Constructor Requirements creates a new Requirements instance.
	 */
	public InventoryRequirements()
	{
		// Empty constructor
	}


// ---------------------------------------------------------------------
// Section: Getters and Setters
// ---------------------------------------------------------------------


	/**
	 * Method InventoryRequirements#getRequiredItems returns the requiredItems of this object.
	 *
	 * @return the requiredItems (type List<ItemStack>) of this object.
	 */
	public List<ItemStack> getRequiredItems()
	{
		return requiredItems;
	}


	/**
	 * Method InventoryRequirements#setRequiredItems sets new value for the requiredItems of this object.
	 * @param requiredItems new value for this object.
	 *
	 */
	public void setRequiredItems(List<ItemStack> requiredItems)
	{
		this.requiredItems = requiredItems;
	}


	/**
	 * Method InventoryRequirements#isTakeItems returns the takeItems of this object.
	 *
	 * @return the takeItems (type boolean) of this object.
	 */
	public boolean isTakeItems()
	{
		return takeItems;
	}


	/**
	 * Method InventoryRequirements#setTakeItems sets new value for the takeItems of this object.
	 * @param takeItems new value for this object.
	 *
	 */
	public void setTakeItems(boolean takeItems)
	{
		this.takeItems = takeItems;
	}


// ---------------------------------------------------------------------
// Section: Other methods
// ---------------------------------------------------------------------


	/**
	 * Method Requirements#clone allows to clone Requirements object, to avoid changing content when it is necessary
	 * to use it.
	 * @return InventoryRequirements clone
	 */
	@Override
	public Requirements clone()
	{
		InventoryRequirements clone = new InventoryRequirements();
		clone.setRequiredPermissions(new HashSet<>(this.getRequiredPermissions()));

		clone.setRequiredItems(this.requiredItems.stream().
			map(ItemStack::clone).
			collect(Collectors.toCollection(() -> new ArrayList<>(this.requiredItems.size()))));
		clone.setTakeItems(this.takeItems);

		return clone;
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

	/**
	 * List of required ItemStacks in players inventory to complete this challenge.
	 */
	@Expose
	private List<ItemStack> requiredItems = new ArrayList<>();

	/**
	 * Boolean that indicate if challenge completion should remove items from inventory.
	 */
	@Expose
	private boolean takeItems = true;
}
