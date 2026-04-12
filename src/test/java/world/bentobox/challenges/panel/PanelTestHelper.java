package world.bentobox.challenges.panel;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.api.panels.TemplatedPanel;
import world.bentobox.bentobox.api.panels.reader.ItemTemplateRecord;
import world.bentobox.challenges.database.object.Challenge;
import world.bentobox.challenges.database.object.requirements.InventoryRequirements;

/**
 * Shared test utilities for panel tests.
 */
public class PanelTestHelper {

    /**
     * Set Bukkit.server via reflection.
     */
    public static void setServer(Server server) throws Exception {
        Field field = Bukkit.class.getDeclaredField("server");
        field.setAccessible(true);
        field.set(null, server);
    }

    /**
     * Set BentoBox.instance via reflection.
     */
    public static void setBentoBoxInstance(BentoBox instance) throws Exception {
        Field field = BentoBox.class.getDeclaredField("instance");
        field.setAccessible(true);
        field.set(null, instance);
    }

    /**
     * Set up user translation mocks to return the key for any number of args.
     * Uses Mockito.doAnswer to properly handle String... varargs.
     */
    public static void setupUserTranslations(User user) {
        // Use doAnswer for varargs - order matters: more specific first, then general

        // getTranslation(World, String, String...)
        Mockito.doAnswer((Answer<String>) inv -> inv.getArgument(1, String.class))
            .when(user).getTranslation(Mockito.any(org.bukkit.World.class),
                Mockito.anyString(), Mockito.<String>any());

        // getTranslation(String, String...)
        Mockito.doAnswer((Answer<String>) inv -> inv.getArgument(0, String.class))
            .when(user).getTranslation(Mockito.anyString(), Mockito.<String>any());

        // getTranslationOrNothing(String, String...)
        Mockito.doAnswer((Answer<String>) inv -> inv.getArgument(0, String.class))
            .when(user).getTranslationOrNothing(Mockito.anyString(), Mockito.<String>any());
    }

    /**
     * Set the server field on a BentoBox/JavaPlugin instance via reflection.
     * This is needed because getServer() is final in PluginBase.
     */
    public static void setPluginServer(Object plugin, org.bukkit.Server server) throws Exception {
        // Try JavaPlugin's server field, then PluginBase
        Class<?> clazz = plugin.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField("server");
                field.setAccessible(true);
                field.set(plugin, server);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        // If no server field found, try setting it via the Bukkit.server approach
        // as a fallback (ConversationFactory calls Bukkit.getServer() in some cases)
    }

    /**
     * Create an ItemTemplateRecord with the given parameters.
     */
    public static ItemTemplateRecord createTemplate(ItemStack icon, String title,
            String description, Map<String, Object> dataMap,
            List<ItemTemplateRecord.ActionRecords> actions) {
        return new ItemTemplateRecord(icon, title, description, actions, dataMap, null);
    }

    /**
     * Create a simple ItemTemplateRecord with basic settings.
     */
    public static ItemTemplateRecord createSimpleTemplate(Map<String, Object> dataMap) {
        return new ItemTemplateRecord(
            new ItemStack(Material.PAPER), "Title", "Description",
            Collections.emptyList(), dataMap, null);
    }

    /**
     * Create an ItemTemplateRecord with no icon/title/description.
     */
    public static ItemTemplateRecord createEmptyTemplate(Map<String, Object> dataMap) {
        return new ItemTemplateRecord(
            null, null, null,
            Collections.emptyList(), dataMap, null);
    }

    /**
     * Create a TemplatedPanel.ItemSlot using reflection.
     * Constructs a minimal TemplatedPanel stub with typeSlotMap.
     */
    public static TemplatedPanel.ItemSlot createItemSlot(int slot,
            Map<String, Integer> typeSlotMap) throws Exception {
        // Create a TemplatedPanel shell with just the typeSlotMap set via reflection
        // We can't construct it normally (needs builder + template file), so use Objenesis-style creation
        // Use sun.misc.Unsafe or similar
        TemplatedPanel panel = createTemplatedPanelStub(typeSlotMap);
        return new TemplatedPanel.ItemSlot(slot, panel);
    }

    private static TemplatedPanel createTemplatedPanelStub(Map<String, Integer> typeSlotMap)
            throws Exception {
        // Use sun.misc.Unsafe to create instance without calling constructor
        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
        Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        Object unsafe = unsafeField.get(null);

        java.lang.reflect.Method allocate = unsafeClass.getMethod("allocateInstance", Class.class);
        TemplatedPanel panel = (TemplatedPanel) allocate.invoke(unsafe, TemplatedPanel.class);

        // Set the typeSlotMap field
        Field mapField = TemplatedPanel.class.getDeclaredField("typeSlotMap");
        mapField.setAccessible(true);
        mapField.set(panel, typeSlotMap);

        return panel;
    }

    /**
     * Create a mock Challenge with all required fields populated.
     */
    public static Challenge createBasicChallenge(String name, boolean deployed) {
        Challenge challenge = Mockito.mock(Challenge.class);
        Mockito.when(challenge.getFriendlyName()).thenReturn(name);
        Mockito.when(challenge.getUniqueId()).thenReturn(
            "bskyblock_" + name.toLowerCase().replace(" ", "_"));
        Mockito.when(challenge.getChallengeType()).thenReturn(
            Challenge.ChallengeType.INVENTORY_TYPE);
        Mockito.when(challenge.getIcon()).thenReturn(new ItemStack(Material.DIAMOND));
        Mockito.when(challenge.isDeployed()).thenReturn(deployed);
        Mockito.when(challenge.isRepeatable()).thenReturn(false);
        Mockito.when(challenge.getMaxTimes()).thenReturn(0);
        Mockito.when(challenge.getTimeout()).thenReturn(0L);
        Mockito.when(challenge.getEnvironment()).thenReturn(new HashSet<>());
        Mockito.when(challenge.getDescription()).thenReturn(List.of("Test description"));
        Mockito.when(challenge.getRewardText()).thenReturn("");
        Mockito.when(challenge.getRepeatRewardText()).thenReturn("");
        Mockito.when(challenge.getRewardItems()).thenReturn(Collections.emptyList());
        Mockito.when(challenge.getRepeatItemReward()).thenReturn(Collections.emptyList());
        Mockito.when(challenge.getRewardCommands()).thenReturn(Collections.emptyList());
        Mockito.when(challenge.getRepeatRewardCommands()).thenReturn(Collections.emptyList());
        Mockito.when(challenge.getRewardMoney()).thenReturn(0.0);
        Mockito.when(challenge.getRepeatMoneyReward()).thenReturn(0.0);
        Mockito.when(challenge.getRewardExperience()).thenReturn(0);
        Mockito.when(challenge.getRepeatExperienceReward()).thenReturn(0);

        InventoryRequirements req = Mockito.mock(InventoryRequirements.class);
        Mockito.when(req.getRequiredPermissions()).thenReturn(new HashSet<>());
        Mockito.when(req.getRequiredItems()).thenReturn(Collections.emptyList());
        Mockito.when(challenge.getRequirements()).thenReturn(req);

        return challenge;
    }
}
