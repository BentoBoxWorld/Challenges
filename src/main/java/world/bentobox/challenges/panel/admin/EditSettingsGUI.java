package world.bentobox.challenges.panel.admin;


import org.bukkit.Material;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.panel.CommonGUI;
import world.bentobox.challenges.panel.util.NumberGUI;
import world.bentobox.challenges.utils.GuiUtils;


/**
 * This Class creates GUI that allows to change Challenges Addon Settings via in-game
 * menu.
 */
public class EditSettingsGUI extends CommonGUI
{
// ---------------------------------------------------------------------
// Section: Constructors
// ---------------------------------------------------------------------


	/**
	 * {@inheritDoc}
	 */
	public EditSettingsGUI(ChallengesAddon addon,
		World world,
		User user,
		String topLabel,
		String permissionPrefix)
	{
		this(addon, world, user, topLabel, permissionPrefix, null);
	}


	/**
	 * {@inheritDoc}
	 */
	public EditSettingsGUI(ChallengesAddon addon,
		World world,
		User user,
		String topLabel,
		String permissionPrefix,
		CommonGUI parentGUI)
	{
		super(addon, world, user, topLabel, permissionPrefix, parentGUI);
	}


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


	@Override
	public void build()
	{
		PanelBuilder panelBuilder = new PanelBuilder().user(this.user).name(
			this.user.getTranslation("challenges.gui.title.admin.settings-title"));

		final int lineLength = this.addon.getChallengesSettings().getLoreLineLength();
		GuiUtils.fillBorder(panelBuilder);

		// resetChallenges

		List<String> description = new ArrayList<>(2);
		description.add(this.user.getTranslation("challenges.gui.descriptions.admin.reset-on-new"));
		description.add(this.user.getTranslation("challenges.gui.descriptions.current-value",
			"[value]",
			this.addon.getChallengesSettings().isResetChallenges() ?
				this.user.getTranslation("challenges.gui.descriptions.enabled") :
				this.user.getTranslation("challenges.gui.descriptions.disabled")));

		panelBuilder.item(19, new PanelItemBuilder().
			name(this.user.getTranslation("challenges.gui.buttons.admin.reset-on-new")).
			description(GuiUtils.stringSplit(description, lineLength)).
			icon(Material.LAVA_BUCKET).
			clickHandler((panel, user1, clickType, i) -> {
				this.addon.getChallengesSettings().setResetChallenges(
					!this.addon.getChallengesSettings().isResetChallenges());
				this.build();
				return true;
			}).
			glow(this.addon.getChallengesSettings().isResetChallenges()).
			build());

		// broadcastMessages
		description.clear();
		description.add(this.user.getTranslation("challenges.gui.descriptions.admin.broadcast"));
		description.add(this.user.getTranslation("challenges.gui.descriptions.current-value",
			"[value]",
			this.addon.getChallengesSettings().isBroadcastMessages() ?
				this.user.getTranslation("challenges.gui.descriptions.enabled") :
				this.user.getTranslation("challenges.gui.descriptions.disabled")));

		panelBuilder.item(20, new PanelItemBuilder().
			name(this.user.getTranslation("challenges.gui.buttons.admin.broadcast")).
			description(GuiUtils.stringSplit(description, lineLength)).
			icon(Material.JUKEBOX).
			clickHandler((panel, user1, clickType, i) -> {
				this.addon.getChallengesSettings().setBroadcastMessages(
					!this.addon.getChallengesSettings().isBroadcastMessages());
				this.build();
				return true;
			}).
			glow(this.addon.getChallengesSettings().isBroadcastMessages()).
			build());

		// removeCompleteOneTimeChallenges
		description.clear();
		description.add(this.user.getTranslation("challenges.gui.descriptions.admin.remove-completed"));
		description.add(this.user.getTranslation("challenges.gui.descriptions.current-value",
			"[value]",
			this.addon.getChallengesSettings().isRemoveCompleteOneTimeChallenges() ?
				this.user.getTranslation("challenges.gui.descriptions.enabled") :
				this.user.getTranslation("challenges.gui.descriptions.disabled")));

		panelBuilder.item(21, new PanelItemBuilder().
			name(this.user.getTranslation("challenges.gui.buttons.admin.remove-completed")).
			description(GuiUtils.stringSplit(description, lineLength)).
			icon(Material.MAGMA_BLOCK).
			clickHandler((panel, user1, clickType, i) -> {
				this.addon.getChallengesSettings().setRemoveCompleteOneTimeChallenges(
					!this.addon.getChallengesSettings().isRemoveCompleteOneTimeChallenges());
				this.build();
				return true;
			}).
			glow(this.addon.getChallengesSettings().isRemoveCompleteOneTimeChallenges()).
			build());

		// addCompletedGlow
		description.clear();
		description.add(this.user.getTranslation("challenges.gui.descriptions.admin.glow"));
		description.add(this.user.getTranslation("challenges.gui.descriptions.current-value",
			"[value]",
			this.addon.getChallengesSettings().isAddCompletedGlow() ?
				this.user.getTranslation("challenges.gui.descriptions.enabled") :
				this.user.getTranslation("challenges.gui.descriptions.disabled")));

		panelBuilder.item(22, new PanelItemBuilder().
			name(this.user.getTranslation("challenges.gui.buttons.admin.glow")).
			description(GuiUtils.stringSplit(description, lineLength)).
			icon(Material.GLOWSTONE).
			clickHandler((panel, user1, clickType, i) -> {
				this.addon.getChallengesSettings().setAddCompletedGlow(
					!this.addon.getChallengesSettings().isAddCompletedGlow());
				this.build();
				return true;
			}).
			glow(this.addon.getChallengesSettings().isAddCompletedGlow()).
			build());

		// freeChallengesAtTheTop
		description.clear();
		description.add(this.user.getTranslation("challenges.gui.descriptions.admin.free-at-top"));
		description.add(this.user.getTranslation("challenges.gui.descriptions.current-value",
			"[value]",
			this.addon.getChallengesSettings().isAddCompletedGlow() ?
				this.user.getTranslation("challenges.gui.descriptions.enabled") :
				this.user.getTranslation("challenges.gui.descriptions.disabled")));

		panelBuilder.item(23, new PanelItemBuilder().
			name(this.user.getTranslation("challenges.gui.buttons.admin.free-at-top")).
			description(GuiUtils.stringSplit(description, lineLength)).
			icon(Material.FILLED_MAP).
			clickHandler((panel, user1, clickType, i) -> {
				this.addon.getChallengesSettings().setFreeChallengesFirst(
					!this.addon.getChallengesSettings().isFreeChallengesFirst());
				this.build();
				return true;
			}).
			glow(this.addon.getChallengesSettings().isFreeChallengesFirst()).
			build());

		// Lore line length
		description = new ArrayList<>(2);
		description.add(this.user.getTranslation("challenges.gui.descriptions.admin.line-length"));
		description.add(this.user.getTranslation("challenges.gui.descriptions.current-value",
			"[value]", Integer.toString(this.addon.getChallengesSettings().getLoreLineLength())));
		panelBuilder.item(24, new PanelItemBuilder().
			name(this.user.getTranslation("challenges.gui.buttons.admin.line-length")).
			description(GuiUtils.stringSplit(description, lineLength)).
			icon(Material.ANVIL).
			clickHandler((panel, user1, clickType, i) -> {
				new NumberGUI(this.user,
					this.addon.getChallengesSettings().getLoreLineLength(),
					0,
					(status, value) -> {
						if (status)
						{
							this.addon.getChallengesSettings().setLoreLineLength(value);
						}

						this.build();
					});

				return true;
			}).
			glow(this.addon.getChallengesSettings().isFreeChallengesFirst()).
			build());

		// Return Button
		panelBuilder.item(44, this.returnButton);

		panelBuilder.build();
	}
}
