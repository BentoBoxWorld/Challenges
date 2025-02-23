//
// Created by BONNe
// Copyright - 2021
//
// Enhanced by tastybento


package world.bentobox.challenges.database.object.requirements;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.Expose;


/**
 * Requirements for statistics based challenges
 */
public class StatisticRequirements extends Requirements
{
    /**
     * Record for this requirement
     * @param Statistic statistic
     * @param EntityType entity
     * @param Material material
     *  @param Integer amount
     *  @param Boolean reduceStatistic
     */
    public record StatisticRec(@Expose Statistic statistic, @Expose EntityType entity, @Expose Material material,
            @Expose Integer amount, @Expose Boolean reduceStatistic) {
    }

    /**
     * Type of the statistic field.
     * @deprecated Shifting to a list
     */
    @Expose
    @Nullable
    private Statistic statistic;

    /**
     * Type of entity for entity related statistics.
     * @deprecated Shifting to a list
     */
    @Expose
    @Nullable
    private EntityType entity;

    /**
     * Type of material for block and item related statistics.
     * @deprecated Shifting to a list
     */
    @Expose
    @Nullable
    private Material material;

    /**
     * Amount of the stats.
     * @deprecated Shifting to a list
     */
    @Expose
    private Integer amount;

    /**
     * Indicate that player statistic fields must be adjusted after completing challenges.
     * @deprecated Shifting to a list
     */
    @Expose
    private Boolean reduceStatistic;

    /**
     * List of statistics that must be done for this challenge
     */
    @Expose
    @Nullable
    private List<StatisticRec> statisticList;


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
        requirements.setStatisticList(this.getRequiredStatistics());
        return requirements;
    }


    @Override
    public boolean isValid()
    {
        // TODO - do something here?
        return super.isValid();
    }

    /**
     * @return the statisticList
     */
    public List<StatisticRec> getRequiredStatistics() {
        if (statisticList == null) {
            statisticList = new ArrayList<>();
            // Convert old single statistic entries to new list of records
            if (statistic != null) {
                StatisticRec rec = new StatisticRec(this.statistic, this.entity, this.material, this.amount,
                        this.reduceStatistic);
                statisticList.add(rec);
            }
        }
        return statisticList;
    }

    /**
     * @param value the statisticList to set
     */
    public void setStatisticList(Collection<StatisticRec> value) {
        // If value is null, assign null; otherwise, create a new ArrayList from value.
        this.statisticList = (value == null) ? null : new ArrayList<>(value);
    }


}
