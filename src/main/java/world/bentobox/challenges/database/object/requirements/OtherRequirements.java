//
// Created by BONNe
// Copyright - 2019
//


package world.bentobox.challenges.database.object.requirements;


import com.google.gson.annotations.Expose;
import org.eclipse.jdt.annotation.NonNull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * This class contains all necessary requirements to complete other type challenge.
 */
public class OtherRequirements extends Requirements
{
	/**
	 * Constructor Requirements creates a new Requirements instance.
	 */
	public OtherRequirements()
	{
		// Empty constructor
	}


// ---------------------------------------------------------------------
// Section: Getters and Setters
// ---------------------------------------------------------------------


	/**
	 * Method OtherRequirements#getRequiredExperience returns the requiredExperience of this object.
	 *
	 * @return the requiredExperience (type int) of this object.
	 */
	public int getRequiredExperience()
	{
		return requiredExperience;
	}


	/**
	 * Method OtherRequirements#setRequiredExperience sets new value for the requiredExperience of this object.
	 * @param requiredExperience new value for this object.
	 *
	 */
	public void setRequiredExperience(int requiredExperience)
	{
		this.requiredExperience = requiredExperience;
	}


	/**
	 * Method OtherRequirements#isTakeExperience returns the takeExperience of this object.
	 *
	 * @return the takeExperience (type boolean) of this object.
	 */
	public boolean isTakeExperience()
	{
		return takeExperience;
	}


	/**
	 * Method OtherRequirements#setTakeExperience sets new value for the takeExperience of this object.
	 * @param takeExperience new value for this object.
	 *
	 */
	public void setTakeExperience(boolean takeExperience)
	{
		this.takeExperience = takeExperience;
	}


	/**
	 * Method OtherRequirements#getRequiredMoney returns the requiredMoney of this object.
	 *
	 * @return the requiredMoney (type double) of this object.
	 */
	public double getRequiredMoney()
	{
		return requiredMoney;
	}


	/**
	 * Method OtherRequirements#setRequiredMoney sets new value for the requiredMoney of this object.
	 * @param requiredMoney new value for this object.
	 *
	 */
	public void setRequiredMoney(double requiredMoney)
	{
		this.requiredMoney = requiredMoney;
	}


	/**
	 * Method OtherRequirements#isTakeMoney returns the takeMoney of this object.
	 *
	 * @return the takeMoney (type boolean) of this object.
	 */
	public boolean isTakeMoney()
	{
		return takeMoney;
	}


	/**
	 * Method OtherRequirements#setTakeMoney sets new value for the takeMoney of this object.
	 * @param takeMoney new value for this object.
	 *
	 */
	public void setTakeMoney(boolean takeMoney)
	{
		this.takeMoney = takeMoney;
	}


	/**
	 * Method OtherRequirements#getRequiredIslandLevel returns the requiredIslandLevel of this object.
	 *
	 * @return the requiredIslandLevel (type long) of this object.
	 */
	public long getRequiredIslandLevel()
	{
		return requiredIslandLevel;
	}


	/**
	 * Method OtherRequirements#setRequiredIslandLevel sets new value for the requiredIslandLevel of this object.
	 * @param requiredIslandLevel new value for this object.
	 *
	 */
	public void setRequiredIslandLevel(long requiredIslandLevel)
	{
		this.requiredIslandLevel = requiredIslandLevel;
	}


// ---------------------------------------------------------------------
// Section: Other methods
// ---------------------------------------------------------------------


	/**
	 * Method Requirements#clone allows to clone Requirements object, to avoid changing content when it is necessary
	 * to use it.
	 * @return OtherRequirements clone
	 */
	@Override
	public Requirements clone()
	{
		OtherRequirements clone = new OtherRequirements();
		clone.setRequiredPermissions(new HashSet<>(this.getRequiredPermissions()));

		clone.setRequiredExperience(this.requiredExperience);
		clone.setTakeExperience(this.takeExperience);
		clone.setRequiredMoney(this.requiredMoney);
		clone.setTakeMoney(this.takeMoney);
		clone.setRequiredIslandLevel(this.requiredIslandLevel);

		return clone;
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


	/**
	 * Necessary amount of experience for player to complete challenge.
	 */
	@Expose
	private int requiredExperience;

	/**
	 * Should take experience from player.
	 */
	@Expose
	private boolean takeExperience;

	/**
	 * Necessary amount of money in player account to complete challenge. Requires Economy plugin.
	 */
	@Expose
	private double requiredMoney;

	/**
	 * Should take money from player account. Requires Economy plugin.
	 */
	@Expose
	private boolean takeMoney;

	/**
	 * Necessary Island Level from Level Addon
	 */
	@Expose
	private long requiredIslandLevel;
}
