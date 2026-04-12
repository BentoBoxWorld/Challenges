package world.bentobox.challenges.panel;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import world.bentobox.bentobox.api.panels.TemplatedPanel;
import world.bentobox.bentobox.api.panels.reader.ItemTemplateRecord;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.database.object.Challenge;
import world.bentobox.challenges.database.object.requirements.InventoryRequirements;

/**
 * Shared test utilities for panel tests.
 */
public class PanelTestHelper {

    /**
     * Set up user translation mocks to return the key for any invocation.
     * Uses a lenient default answer that returns the first String argument
     * for any method that returns String.
     */
    public static void setupUserTranslations(User user) {
        // Catch-all for getTranslation(String, String...) — returns the first arg
        Mockito.lenient().when(user.getTranslation(Mockito.anyString()))
            .thenAnswer((Answer<String>) inv -> inv.getArgument(0, String.class));

        // For varargs overloads, Mockito 5 needs any(String[].class) to match the whole varargs array
        Mockito.lenient().doAnswer((Answer<String>) inv -> inv.getArgument(0, String.class))
            .when(user).getTranslation(Mockito.anyString(), Mockito.any(String[].class));

        // getTranslation(World, String, String...)
        Mockito.lenient().doAnswer((Answer<String>) inv -> inv.getArgument(1, String.class))
            .when(user).getTranslation(Mockito.any(org.bukkit.World.class),
                Mockito.anyString(), Mockito.any(String[].class));

        // getTranslationOrNothing(String, String...) — catch-all
        Mockito.lenient().when(user.getTranslationOrNothing(Mockito.anyString()))
            .thenAnswer((Answer<String>) inv -> inv.getArgument(0, String.class));

        Mockito.lenient().doAnswer((Answer<String>) inv -> inv.getArgument(0, String.class))
            .when(user).getTranslationOrNothing(Mockito.anyString(), Mockito.any(String[].class));
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
