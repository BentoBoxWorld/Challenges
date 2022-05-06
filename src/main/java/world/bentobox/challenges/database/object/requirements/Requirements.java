//
// Created by BONNe
// Copyright - 2019
//


package world.bentobox.challenges.database.object.requirements;


import java.util.HashSet;
import java.util.Set;

import com.google.gson.annotations.Expose;


/**
 * This abstract class allows to define requirements for each challenge.
 */
public abstract class Requirements
{
	/**
	 * Constructor Requirements creates a new Requirements instance.
	 */
	public Requirements()
	{
		// Empty Constructor
	}


// ---------------------------------------------------------------------
// Section: Getters and Setters
// ---------------------------------------------------------------------


	/**
	 * Method Requirements#getRequiredPermissions returns the requiredPermissions of this object.
	 *
	 * @return the requiredPermissions (type Set<String>) of this object.
	 */
	public Set<String> getRequiredPermissions()
	{
		return requiredPermissions;
	}


	/**
	 * Method Requirements#setRequiredPermissions sets new value for the requiredPermissions of this object.
	 * @param requiredPermissions new value for this object.
	 *
	 */
	public void setRequiredPermissions(Set<String> requiredPermissions)
	{
		this.requiredPermissions = requiredPermissions;
	}


// ---------------------------------------------------------------------
// Section: Other Methods
// ---------------------------------------------------------------------


	/**
	 * Method isValid returns if given requirement data is valid or not.
	 * @return {@code true} if data is valid, {@code false} otherwise.
	 */
	public boolean isValid()
	{
		return this.requiredPermissions != null;
	}


	/**
	 * Method Requirements#copy allows to copy Requirements object, to avoid changing content when it is necessary
	 * to use it.
	 * @return Requirements copy
	 */
	public abstract Requirements copy();


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


	/**
	 * This set contains all permission strings that ir required for player to complete challenge.
	 */
	@Expose
	private Set<String> requiredPermissions = new HashSet<>();
}

