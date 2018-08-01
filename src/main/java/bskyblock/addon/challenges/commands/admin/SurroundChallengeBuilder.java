package bskyblock.addon.challenges.commands.admin;

import java.util.EnumMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import bskyblock.addon.challenges.ChallengesAddon;
import world.bentobox.bentobox.api.user.User;

/**
 * Enables the state of a Surrounding Challenge to be stored as it is built
 * @author tastybento
 *
 */
public class SurroundChallengeBuilder {
    private ChallengesAddon addon;
    private String name;
    private User owner;
    private Map<Material, Integer> reqBlocks = new EnumMap<>(Material.class);
    private Map<EntityType, Integer> reqEntities = new EnumMap<>(EntityType.class);

    public SurroundChallengeBuilder(ChallengesAddon addon) {
        this.addon = addon;
    }

    SurroundChallengeBuilder name(String name) {
        this.name = name;
        return this;
    }

    SurroundChallengeBuilder owner(User user) {
        this.owner = user;
        return this;
    }

    SurroundChallengeBuilder addBlock(Material mat) {
        reqBlocks.computeIfPresent(mat, (material, amount) -> amount + 1);
        reqBlocks.putIfAbsent(mat, 1);
        return this;
    }

    SurroundChallengeBuilder addEntity(EntityType ent) {
        reqEntities.computeIfPresent(ent, (type, amount) -> amount + 1);
        reqEntities.putIfAbsent(ent, 1);
        return this;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the owner
     */
    public User getOwner() {
        return owner;
    }

    /**
     * @return the reqBlocks
     */
    public Map<Material, Integer> getReqBlocks() {
        return reqBlocks;
    }

    /**
     * @return the reqEntities
     */
    public Map<EntityType, Integer> getReqEntities() {
        return reqEntities;
    }

    public boolean build() {
        return addon.getChallengesManager().createSurroundingChallenge(this);
        
    }

}