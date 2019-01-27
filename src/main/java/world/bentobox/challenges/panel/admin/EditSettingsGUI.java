package world.bentobox.challenges.panel.admin;


import org.bukkit.Material;
import org.bukkit.World;

import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.panel.CommonGUI;
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
			this.user.getTranslation("challenges.gui.admin.settings-title"));

		GuiUtils.fillBorder(panelBuilder);

		// resetChallenges
		panelBuilder.item(19, new PanelItemBuilder().
			name(this.user.getTranslation("challenges.gui.admin.buttons.reset")).
			description(GuiUtils.stringSplit(this.user.getTranslation("challenges.gui.admin.descriptions.reset"))).
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
		panelBuilder.item(20, new PanelItemBuilder().
			name(this.user.getTranslation("challenges.gui.admin.buttons.broadcast")).
			description(GuiUtils.stringSplit(this.user.getTranslation("challenges.gui.admin.descriptions.broadcast"))).
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
		panelBuilder.item(21, new PanelItemBuilder().
			name(this.user.getTranslation("challenges.gui.admin.buttons.remove-on-complete")).
			description(GuiUtils.stringSplit(this.user.getTranslation("challenges.gui.admin.descriptions.remove-on-complete"))).
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
		panelBuilder.item(22, new PanelItemBuilder().
			name(this.user.getTranslation("challenges.gui.admin.buttons.glow")).
			description(GuiUtils.stringSplit(this.user.getTranslation("challenges.gui.admin.descriptions.glow"))).
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
		panelBuilder.item(23, new PanelItemBuilder().
			name(this.user.getTranslation("challenges.gui.admin.buttons.free-challenges")).
			description(GuiUtils.stringSplit(this.user.getTranslation("challenges.gui.admin.descriptions.free-challenges"))).
			icon(Material.FILLED_MAP).
			clickHandler((panel, user1, clickType, i) -> {
				this.addon.getChallengesSettings().setFreeChallengesFirst(
					!this.addon.getChallengesSettings().isFreeChallengesFirst());
				this.build();
				return true;
			}).
			glow(this.addon.getChallengesSettings().isFreeChallengesFirst()).
			build());

		// Return Button
		panelBuilder.item(44, this.returnButton);

		panelBuilder.build();
	}
}
