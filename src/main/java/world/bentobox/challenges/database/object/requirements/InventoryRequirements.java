//
// Created by BONNe
// Copyright - 2019
//


package world.bentobox.challenges.database.object.requirements;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.google.gson.annotations.Expose;


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


	/**
	 * Gets ignore meta data.
	 *
	 * @return the ignore meta data
	 */
	public Set<Material> getIgnoreMetaData()
	{
		if (this.ignoreMetaData == null)
		{
			// Fixes null-pointer, that should not be possible, but may be.
			this.ignoreMetaData = new HashSet<>();
		}

		return this.ignoreMetaData;
	}


	/**
	 * Sets ignore meta data.
	 *
	 * @param ignoreMetaData the ignore meta data
	 */
	public void setIgnoreMetaData(Set<Material> ignoreMetaData)
	{
		this.ignoreMetaData = ignoreMetaData;
	}


// ---------------------------------------------------------------------
// Section: Other methods
// ---------------------------------------------------------------------


	/**
	 * Method isValid returns if given requirement data is valid or not.
	 *
	 * @return {@code true} if data is valid, {@code false} otherwise.
	 */
	@Override
	public boolean isValid()
	{
		return super.isValid() &&
			this.requiredItems != null && this.requiredItems.stream().noneMatch(Objects::isNull);
	}


	/**
	 * Method Requirements#copy allows copies Requirements object, to avoid changing content when it is necessary
	 * to use it.
	 * @return InventoryRequirements copy
	 */
	@Override
	public Requirements copy()
	{
		InventoryRequirements clone = new InventoryRequirements();
		clone.setRequiredPermissions(new HashSet<>(this.getRequiredPermissions()));

		clone.setRequiredItems(this.requiredItems.stream().
			map(ItemStack::clone).
			collect(Collectors.toCollection(() -> new ArrayList<>(this.requiredItems.size()))));
		clone.setTakeItems(this.takeItems);
		clone.setIgnoreMetaData(new HashSet<>(this.ignoreMetaData));

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
	 * Set of item stacks that should ignore metadata.
	 */
	@Expose
	private Set<Material> ignoreMetaData = new HashSet<>();

	/**
	 * Boolean that indicate if challenge completion should remove items from inventory.
	 */
	@Expose
	private boolean takeItems = true;
}
