package world.bentobox.challenges.panel.admin;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.database.objects.Players;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.database.object.Challenge;
import world.bentobox.challenges.panel.CommonPagedPanel;
import world.bentobox.challenges.panel.CommonPanel;
import world.bentobox.challenges.panel.util.ChallengeSelector;
import world.bentobox.challenges.utils.Constants;
import world.bentobox.challenges.utils.Utils;


/**
 * This class contains methods that allows to select specific user.
 */
public class ListUsersPanel extends CommonPagedPanel<Player>
{
    // ---------------------------------------------------------------------
    // Section: Constructors
    // ---------------------------------------------------------------------


    /**
     * @param addon Addon where panel operates.
     * @param world World from which panel was created.
     * @param user User who created panel.
     * @param topLabel Command top label which creates panel (f.e. island or ai)
     * @param permissionPrefix Command permission prefix (f.e. bskyblock.)
     * @param operationMode Indicate what should happen on player icon click.
     */
    private ListUsersPanel(ChallengesAddon addon,
        User user,
        World world,
        String topLabel,
        String permissionPrefix,
        Mode operationMode)
    {
        super(addon, user, world, topLabel, permissionPrefix);
        this.onlineUsers = this.collectUsers(ViewMode.IN_WORLD);
        this.operationMode = operationMode;
        this.filterElements = this.onlineUsers;
    }


    /**
     * @param operationMode Indicate what should happen on player icon click.
     */
    private ListUsersPanel(CommonPanel panel, Mode operationMode)
    {
        super(panel);
        this.onlineUsers = this.collectUsers(ViewMode.IN_WORLD);
        this.operationMode = operationMode;
        this.filterElements = this.onlineUsers;
    }


    /**
     * Open the Challenges Admin GUI.
     *
     * @param addon the addon
     * @param world the world
     * @param user the user
     * @param topLabel the top label
     * @param permissionPrefix the permission prefix
     */
    public static void open(ChallengesAddon addon,
        World world,
        User user,
        String topLabel,
        String permissionPrefix,
        Mode mode)
    {
        new ListUsersPanel(addon, user, world, topLabel, permissionPrefix, mode).build();
    }


    /**
     * Open the Challenges Admin GUI.
     */
    public static void open(CommonPanel parentGUI, Mode mode)
    {
        new ListUsersPanel(parentGUI, mode).build();
    }


    // ---------------------------------------------------------------------
    // Section: Methods
    // ---------------------------------------------------------------------


    /**
     * This method is called when filter value is updated.
     */
    @Override
    protected void updateFilters()
    {
        if (this.searchString == null || this.searchString.isBlank())
        {
            this.filterElements = this.onlineUsers;
        }
        else
        {
            this.filterElements = this.onlineUsers.stream().
                filter(element -> {
                    // If element name is set and name contains search field, then do not filter out.
                    return element.getDisplayName().toLowerCase().contains(this.searchString.toLowerCase());
                }).
                distinct().
                collect(Collectors.toList());
        }
    }


    @Override
    protected void build()
    {
        PanelBuilder panelBuilder = new PanelBuilder().user(this.user).name(
            this.user.getTranslation(Constants.TITLE + "choose-player"));

        PanelUtils.fillBorder(panelBuilder);

        this.populateElements(panelBuilder, this.filterElements);

        // Add button that allows to toggle different player lists.
        panelBuilder.item( 4, this.createToggleButton());
        panelBuilder.item(44, this.returnButton);

        panelBuilder.build();
    }


    /**
     * This method creates button for given user. If user has island it will add valid click handler.
     * @param player Player which button must be created.
     * @return Player button.
     */
    @Override
    protected PanelItem createElementButton(Player player)
    {
        final String reference = Constants.BUTTON + "player.";

        Island island = this.addon.getIslands().getIsland(this.world, player.getUniqueId());

        if (island == null)
        {
            return new PanelItemBuilder().
                name(this.user.getTranslation(reference + "name", Constants.PARAMETER_NAME, player.getName())).
                icon(Material.BARRIER).
                description(this.user.getTranslation(reference + "no-island")).
                build();
        }

        List<String> description = new ArrayList<>(4);
        description.add(this.user.getTranslation(reference + "description",
            Constants.PARAMETER_OWNER, this.addon.getPlayers().getName(island.getOwner())));

        // Is owner in his own island member set? I assume yes. Need testing.
        if (island.getMemberSet().size() > 1)
        {
            description.add(this.user.getTranslation(reference + "members"));
            island.getMemberSet().forEach(member -> {
                if (member != island.getOwner())
                {
                    description.add(this.user.getTranslation(reference + "member",
                        Constants.PARAMETER_NAME, this.addon.getPlayers().getName(member)));
                }
            });
        }

        description.add("");

        if (this.operationMode == Mode.RESET_ALL && this.selectedPlayer != null)
        {
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-reset-all"));
        }
        else
        {
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-choose"));
        }

        return new PanelItemBuilder().
            name(this.user.getTranslation(reference + "name", Constants.PARAMETER_NAME, player.getName())).
            icon(player.getName()).
            description(description).
            glow(this.operationMode == Mode.RESET_ALL && this.selectedPlayer == player).
            clickHandler((panel, user1, clickType, i) -> {
                switch (this.operationMode)
                {
                    case COMPLETE -> {
                        // Get all challenge that is in current level.
                        List<Challenge> challengeList = this.manager.getAllChallenges(this.world);

                        // Generate descriptions for these challenges
                        Map<Challenge, List<String>> challengeDescriptionMap = challengeList.stream().
                            filter(challenge -> !this.manager.isChallengeComplete(player.getUniqueId(), this.world, challenge)).
                            collect(Collectors.toMap(challenge -> challenge,
                                challenge -> this.generateChallengeDescription(challenge, User.getInstance(player)),
                                (a, b) -> b,
                                () -> new LinkedHashMap<>(challengeList.size())));

                        // Open select gui
                        ChallengeSelector.open(this.user,
                            Material.LIME_STAINED_GLASS_PANE,
                            challengeDescriptionMap,
                            (status, valueSet) -> {
                                if (status)
                                {
                                    valueSet.forEach(challenge ->
                                        manager.setChallengeComplete(player.getUniqueId(),
                                            this.world,
                                            challenge,
                                            this.user.getUniqueId()));
                                }

                            this.build();
                        });
                    }
                    case RESET -> {
                        // Get all challenge that is in current level.
                        List<Challenge> challengeList = this.manager.getAllChallenges(this.world);

                        // Generate descriptions for these challenges
                        Map<Challenge, List<String>> challengeDescriptionMap = challengeList.stream().
                            filter(challenge -> this.manager.isChallengeComplete(player.getUniqueId(), this.world, challenge)).
                            collect(Collectors.toMap(challenge -> challenge,
                                challenge -> this.generateChallengeDescription(challenge, User.getInstance(player)),
                                (a, b) -> b,
                                () -> new LinkedHashMap<>(challengeList.size())));

                        // Open select gui
                        ChallengeSelector.open(this.user,
                            Material.ORANGE_STAINED_GLASS_PANE,
                            challengeDescriptionMap,
                            (status, valueSet) -> {
                                if (status)
                                {
                                    valueSet.forEach(challenge ->
                                        this.manager.resetChallenge(player.getUniqueId(),
                                            this.world,
                                            challenge,
                                            this.user.getUniqueId()));
                                }

                            this.build();
                        });
                    }
                    case RESET_ALL -> {
                        if (this.selectedPlayer == null)
                        {
                            this.selectedPlayer = player;
                        }
                        else
                        {
                            this.manager.resetAllChallenges(player.getUniqueId(), this.world, this.user.getUniqueId());
                            this.selectedPlayer = null;
                        }

                        this.build();
                    }
                }

                return true;
            }).
            build();
    }


    /**
     * This method collects users based on view mode.
     * @param mode Given view mode.
     * @return List with players in necessary view mode.
     */
    private List<Player> collectUsers(ViewMode mode)
    {
        return switch (mode) {
            case ONLINE -> new ArrayList<>(Bukkit.getOnlinePlayers());
            case WITH_ISLAND -> this.addon.getPlayers().getPlayers().stream().
                filter(player -> this.addon.getIslands().getIsland(this.world, player.getPlayerUUID()) != null).
                map(Players::getPlayer).
                collect(Collectors.toList());
            default -> new ArrayList<>(this.world.getPlayers());
        };
    }


    /**
     * This method creates Player List view Mode toggle button.
     * @return Button that toggles through player view mode.
     */
    private PanelItem createToggleButton()
    {
        final String reference = Constants.BUTTON + "player_list.";

        List<String> description = new ArrayList<>(5);

        description.add(this.user.getTranslation(reference + "description"));
        description.add(this.user.getTranslation(reference +
            (ViewMode.ONLINE == this.mode ? "enabled" : "disabled")) +
            this.user.getTranslation(reference + "online"));
        description.add(this.user.getTranslation(reference +
            (ViewMode.WITH_ISLAND == this.mode ? "enabled" : "disabled")) +
            this.user.getTranslation(reference + "with_island"));
        description.add(this.user.getTranslation(reference +
            (ViewMode.IN_WORLD == this.mode ? "enabled" : "disabled")) +
            this.user.getTranslation(reference + "in_world"));

        description.add("");
        description.add(this.user.getTranslation(Constants.TIPS + "left-click-to-cycle"));
        description.add(this.user.getTranslation(Constants.TIPS + "right-click-to-cycle"));

        return new PanelItemBuilder().
            name(this.user.getTranslation(reference + "name")).
            icon(Material.STONE_BUTTON).
            description(description).
            clickHandler((panel, user1, clickType, slot) -> {
                if (clickType.isRightClick())
                {
                    this.mode = Utils.getPreviousValue(ViewMode.values(), this.mode);
                }
                else
                {
                    this.mode = Utils.getNextValue(ViewMode.values(), this.mode);
                }
                this.onlineUsers = this.collectUsers(this.mode);

                // Reset search
                this.searchString = "";
                this.updateFilters();

                this.build();
                return true;
            }).build();
    }


// ---------------------------------------------------------------------
// Section: Enums
// ---------------------------------------------------------------------


    /**
     * This allows to switch which users should be in the list.
     */
    private enum ViewMode
    {
        ONLINE,
        WITH_ISLAND,
        IN_WORLD
    }

    /**
     * This allows to decide what User Icon should do.
     */
    public enum Mode
    {
        COMPLETE,
        RESET,
        RESET_ALL
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

    /**
     * List with players that should be in GUI.
     */
    private List<Player> onlineUsers;

    /**
     * List with players that should be in GUI.
     */
    private List<Player> filterElements;

    /**
     * Current operation mode.
     */
    private final Mode operationMode;

    /**
     * Current index of view mode
     */
    private ViewMode mode = ViewMode.ONLINE;

    /**
     * Stores clicked player.
     */
    private Player selectedPlayer;
}