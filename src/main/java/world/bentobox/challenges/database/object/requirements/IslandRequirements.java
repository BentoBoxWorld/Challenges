//
// Created by BONNe
// Copyright - 2019
//


package world.bentobox.challenges.database.object.requirements;


import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

import org.bukkit.Fluid;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.EntityType;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.JsonAdapter;

import world.bentobox.challenges.database.object.adapters.EntityCompatibilityAdapter;


/**
 * This class contains all necessary requirements to complete island type challenge.
 */
public class IslandRequirements extends Requirements
{
    /**
     * Constructor Requirements creates a new Requirements instance.
     */
    public IslandRequirements() {
        // Empty constructor for data loader
    }

    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------


    /**
     * Map that contains which materials and how many is necessary around player to complete challenge.
     */
    @Expose
    private Map<Material, Integer> requiredBlocks = new EnumMap<>(Material.class);

    @Expose
    private Map<Tag<Material>, Integer> requiredMaterialTags = new HashMap<>();
    @Expose
    private Map<Tag<Fluid>, Integer> requiredFluidTags = new HashMap<>();
    @Expose
    private Map<Tag<EntityType>, Integer> requiredEntityTypeTags = new HashMap<>();

    /**
     * Boolean that indicate if blocks should be removed from world after completion.
     */
    @Expose
    private boolean removeBlocks;

    /**
     * Map that contains which entities and how many is necessary around player to complete challenge.
     */
    @Expose
    @JsonAdapter(EntityCompatibilityAdapter.class)
    private Map<EntityType, Integer> requiredEntities = new EnumMap<>(EntityType.class);

    /**
     * Boolean that indicate if entities should be removed from world after completion.
     */
    @Expose
    private boolean removeEntities;

    /**
     * Radius for searching distance for blocks and entities.
     */
    @Expose
    private int searchRadius = 10;

    // ---------------------------------------------------------------------
    // Section: Getters and Setters
    // ---------------------------------------------------------------------


    /**
     * Method IslandRequirements#getRequiredBlocks returns the requiredBlocks of this object.
     *
     * @return the requiredBlocks (type {@code Map<Material, Integer>}) of this object.
     */
    public Map<Material, Integer> getRequiredBlocks() {
        return requiredBlocks;
    }


    /**
     * Method IslandRequirements#setRequiredBlocks sets new value for the requiredBlocks of this object.
     * @param requiredBlocks new value for this object.
     *
     */
    public void setRequiredBlocks(Map<Material, Integer> requiredBlocks) {
        this.requiredBlocks = requiredBlocks;
    }


    /**
     * Method IslandRequirements#isRemoveBlocks returns the removeBlocks of this object.
     *
     * @return the removeBlocks (type boolean) of this object.
     */
    public boolean isRemoveBlocks() {
        return removeBlocks;
    }


    /**
     * Method IslandRequirements#setRemoveBlocks sets new value for the removeBlocks of this object.
     * @param removeBlocks new value for this object.
     *
     */
    public void setRemoveBlocks(boolean removeBlocks) {
        this.removeBlocks = removeBlocks;
    }


    /**
     * Method IslandRequirements#getRequiredEntities returns the requiredEntities of this object.
     *
     * @return the requiredEntities (type {@code Map<EntityType, Integer>}) of this object.
     */
    public Map<EntityType, Integer> getRequiredEntities() {
        return requiredEntities;
    }


    /**
     * Method IslandRequirements#setRequiredEntities sets new value for the requiredEntities of this object.
     * @param requiredEntities new value for this object.
     *
     */
    public void setRequiredEntities(Map<EntityType, Integer> requiredEntities) {
        this.requiredEntities = requiredEntities;
    }

    /**
     * Method IslandRequirements#isRemoveEntities returns the removeEntities of this object.
     *
     * @return the removeEntities (type boolean) of this object.
     */
    public boolean isRemoveEntities() {
        return removeEntities;
    }


    /**
     * Method IslandRequirements#setRemoveEntities sets new value for the removeEntities of this object.
     * @param removeEntities new value for this object.
     *
     */
    public void setRemoveEntities(boolean removeEntities) {
        this.removeEntities = removeEntities;
    }


    /**
     * Method IslandRequirements#getSearchRadius returns the searchRadius of this object.
     *
     * @return the searchRadius (type int) of this object.
     */
    public int getSearchRadius() {
        return searchRadius;
    }


    /**
     * Method IslandRequirements#setSearchRadius sets new value for the searchRadius of this object.
     * @param searchRadius new value for this object.
     *
     */
    public void setSearchRadius(int searchRadius) {
        this.searchRadius = searchRadius;
    }

    /**
     * Method isValid returns if given requirement data is valid or not.
     *
     * @return {@code true} if data is valid, {@code false} otherwise.
     */
    @Override
    public boolean isValid() {
        return super.isValid() && this.requiredBlocks != null
                && this.requiredBlocks.keySet().stream().noneMatch(Objects::isNull) && this.requiredEntities != null
                && this.requiredEntities.keySet().stream().noneMatch(Objects::isNull);
    }


    /**
     * Method Requirements#copy allows copies Requirements object, to avoid changing content when it is necessary
     * to use it.
     * @return IslandRequirements copy
     */
    @Override
    public Requirements copy() {
        IslandRequirements clone = new IslandRequirements();
        clone.setRequiredPermissions(new HashSet<>(this.getRequiredPermissions()));
        clone.setRequiredMaterialTags(new HashMap<>(this.requiredMaterialTags));
        clone.setRequiredFluidTags(new HashMap<>(this.requiredFluidTags));
        clone.setRequiredEntityTypeTags(new HashMap<>(this.requiredEntityTypeTags));
        clone.setRequiredBlocks(new HashMap<>(this.requiredBlocks));
        clone.setRemoveBlocks(this.removeBlocks);
        clone.setRequiredEntities(new HashMap<>(this.requiredEntities));
        clone.setRemoveEntities(this.removeEntities);

        clone.setSearchRadius(this.searchRadius);

        return clone;
    }


    /**
     * @return the requiredMaterialTags
     */
    public Map<Tag<Material>, Integer> getRequiredMaterialTags() {
        return requiredMaterialTags;
    }

    /**
     * @param requiredMaterialTags the requiredMaterialTags to set
     */
    public void setRequiredMaterialTags(Map<Tag<Material>, Integer> requiredMaterialTags) {
        this.requiredMaterialTags = requiredMaterialTags;
    }

    /**
     * @return the requiredFluidTags
     */
    public Map<Tag<Fluid>, Integer> getRequiredFluidTags() {
        return requiredFluidTags;
    }

    /**
     * @param requiredFluidTags the requiredFluidTags to set
     */
    public void setRequiredFluidTags(Map<Tag<Fluid>, Integer> requiredFluidTags) {
        this.requiredFluidTags = requiredFluidTags;
    }

    /**
     * @return the requiredEntityTypeTags
     */
    public Map<Tag<EntityType>, Integer> getRequiredEntityTypeTags() {
        return requiredEntityTypeTags;
    }

    /**
     * @param requiredEntityTypeTags the requiredEntityTypeTags to set
     */
    public void setRequiredEntityTypeTags(Map<Tag<EntityType>, Integer> requiredEntityTypeTags) {
        this.requiredEntityTypeTags = requiredEntityTypeTags;
    }

}
