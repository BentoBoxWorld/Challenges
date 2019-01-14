package world.bentobox.challenges.panel.admin;


import org.bukkit.Material;
import org.bukkit.World;

import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.panel.CommonGUI;


/**
 * This Class creates GUI that allows to change Challenges Addon Settings via in-game
 * menu.
 */
public class ChallengesSettingsGUI extends CommonGUI
{
// ---------------------------------------------------------------------
// Section: Constructors
// ---------------------------------------------------------------------


	/**
	 * {@inheritDoc}
	 */
	public ChallengesSettingsGUI(ChallengesAddon addon,
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
	public ChallengesSettingsGUI(ChallengesAddon addon,
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

		// resetChallenges
		panelBuilder.item(0, new PanelItemBuilder().
			name(this.user.getTranslation("challenges.gui.admin.buttons.reset")).
			description(this.user.getTranslation("challenges.gui.admin.descriptions.reset")).
			icon(Material.LAVA_BUCKET).
			clickHandler((panel, user1, clickType, i) -> {
				this.addon.getChallengesSettings().setResetChallenges(
					!this.addon.getChallengesSettings().isResetChallenges());
				return true;
			}).
			glow(this.addon.getChallengesSettings().isResetChallenges()).
			build());

		// broadcastMessages
		panelBuilder.item(1, new PanelItemBuilder().
			name(this.user.getTranslation("challenges.gui.admin.buttons.broadcast")).
			description(this.user.getTranslation("challenges.gui.admin.descriptions.broadcast")).
			icon(Material.JUKEBOX).
			clickHandler((panel, user1, clickType, i) -> {
				this.addon.getChallengesSettings().setBroadcastMessages(
					!this.addon.getChallengesSettings().isBroadcastMessages());
				return true;
			}).
			glow(this.addon.getChallengesSettings().isBroadcastMessages()).
			build());

		// removeCompleteOneTimeChallenges
		panelBuilder.item(2, new PanelItemBuilder().
			name(this.user.getTranslation("challenges.gui.admin.buttons.remove-on-complete")).
			description(this.user.getTranslation("challenges.gui.admin.descriptions.remove-on-complete")).
			icon(Material.MAGMA_BLOCK).
			clickHandler((panel, user1, clickType, i) -> {
				this.addon.getChallengesSettings().setRemoveCompleteOneTimeChallenges(
					!this.addon.getChallengesSettings().isRemoveCompleteOneTimeChallenges());
				return true;
			}).
			glow(this.addon.getChallengesSettings().isRemoveCompleteOneTimeChallenges()).
			build());

		// addCompletedGlow
		panelBuilder.item(3, new PanelItemBuilder().
			name(this.user.getTranslation("challenges.gui.admin.buttons.glow")).
			description(this.user.getTranslation("challenges.gui.admin.descriptions.glow")).
			icon(Material.GLOWSTONE).
			clickHandler((panel, user1, clickType, i) -> {
				this.addon.getChallengesSettings().setAddCompletedGlow(
					!this.addon.getChallengesSettings().isAddCompletedGlow());
				return true;
			}).
			glow(this.addon.getChallengesSettings().isAddCompletedGlow()).
			build());

		// Return Button
		panelBuilder.item(8, this.returnButton);

		panelBuilder.build();
	}
}
