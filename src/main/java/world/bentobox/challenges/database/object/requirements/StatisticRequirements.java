//
// Created by BONNe
// Copyright - 2021
//


package world.bentobox.challenges.database.object.requirements;


import com.google.gson.annotations.Expose;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.eclipse.jdt.annotation.Nullable;


public class StatisticRequirements extends Requirements
{
    /**
     * Constructor Requirements creates a new Requirements instance.
     */
    public StatisticRequirements()
    {
        // Empty constructor
    }


    /**
     * This method copies given statistic object.
     * @return Copy of this object.
     */
    @Override
    public Requirements copy()
    {
        StatisticRequirements requirements = new StatisticRequirements();
        requirements.setStatistic(this.statistic);
        requirements.setEntity(this.entity);
        requirements.setMaterial(this.material);
        requirements.setAmount(this.amount);
        requirements.setReduceStatistic(this.reduceStatistic);

        return requirements;
    }


    @Override
    public boolean isValid()
    {
        if (!super.isValid())
        {
            return false;
        }

        if (this.statistic == null)
        {
            return false;
        }

        return switch (this.statistic.getType())
        {
            case ITEM -> this.material != null && this.material.isItem();
            
            case BLOCK -> this.material != null && this.material.isBlock();
            
            case ENTITY -> this.entity != null;
            
            case UNTYPED -> true;
        
        };

    }


    // ---------------------------------------------------------------------
// Section: Getters and setters
// ---------------------------------------------------------------------


    /**
     * Gets statistic.
     *
     * @return the statistic
     */
    @Nullable
    public Statistic getStatistic()
    {
        return statistic;
    }


    /**
     * Sets statistic.
     *
     * @param statistic the statistic
     */
    public void setStatistic(@Nullable Statistic statistic)
    {
        this.statistic = statistic;
    }


    /**
     * Gets entity.
     *
     * @return the entity
     */
    @Nullable
    public EntityType getEntity()
    {
        return entity;
    }


    /**
     * Sets entity.
     *
     * @param entity the entity
     */
    public void setEntity(@Nullable EntityType entity)
    {
        this.entity = entity;
    }


    /**
     * Gets material.
     *
     * @return the material
     */
    @Nullable
    public Material getMaterial()
    {
        return material;
    }


    /**
     * Sets material.
     *
     * @param material the material
     */
    public void setMaterial(@Nullable Material material)
    {
        this.material = material;
    }


    /**
     * Gets amount.
     *
     * @return the amount
     */
    public int getAmount()
    {
        return amount;
    }


    /**
     * Sets amount.
     *
     * @param amount the amount
     */
    public void setAmount(int amount)
    {
        this.amount = amount;
    }


    /**
     * Is reduce statistic boolean.
     *
     * @return the boolean
     */
    public boolean isReduceStatistic()
    {
        return reduceStatistic;
    }


    /**
     * Sets reduce statistic.
     *
     * @param reduceStatistic the reduce statistic
     */
    public void setReduceStatistic(boolean reduceStatistic)
    {
        this.reduceStatistic = reduceStatistic;
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

    /**
     * Type of the statistic field.
     */
    @Expose
    @Nullable
    private Statistic statistic;

    /**
     * Type of entity for entity related statistics.
     */
    @Expose
    @Nullable
    private EntityType entity;

    /**
     * Type of material for block and item related statistics.
     */
    @Expose
    @Nullable
    private Material material;

    /**
     * Amount of the stats.
     */
    @Expose
    private int amount;

    /**
     * Indicate that player statistic fields must be adjusted after completing challenges.
     */
    @Expose
    private boolean reduceStatistic;
}
