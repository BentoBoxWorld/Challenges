import { expect, test, PlayerWrapper } from '@drownek/plugwright';

// LiveGuiHandle / GuiItemLocator are not re-exported from the package entry point, so derive them
// from PlayerWrapper's public API instead of importing them directly.
type LiveGuiHandle = Awaited<ReturnType<PlayerWrapper['gui']>>;
type GuiItemLocator = ReturnType<LiveGuiHandle['locator']>;

/**
 * Baseline: bot joins the real Paper 1.21.11 server (BentoBox 3.14.0 + BSkyBlock + the staged
 * Challenges 1.8.0) and can run a command and receive its reply — proves the toolchain.
 */
test('bot can interact with the server', async ({ player }) => {
  await player.makeOp();
  player.chat('/help');
  await expect(player).toHaveReceivedMessage('Help');
});

/**
 * Feature validation for 1.8.0 (#329): confirmation prompts now append an instruction line
 * telling the player to type confirm/cancel. Open the Challenges admin GUI, left-click the
 * "Challenge Wipe" button (which starts a confirmation conversation) and assert the bot receives
 * the new instruction text in chat.
 */
test('confirmation prompts tell the player how to answer (#329)', async ({ player }) => {
  await player.makeOp();

  // Open the Challenges admin GUI (registered under the BSkyBlock admin command).
  player.chat('/bsbadmin challenges');
  const gui = await player.gui({ title: /Challenges Admin/i });

  // Left-click "Challenge Wipe" -> starts the confirmation conversation.
  await gui.locator(item => item.getDisplayName().includes('Challenge Wipe')).click();

  // The #329 change appends this instruction to every confirmation prompt.
  await expect(player).toHaveReceivedMessage('to proceed, or');
});

/**
 * Feature validation for 1.8.0 (#349): a new "Open GUI Anywhere" setting lets players open the
 * challenges GUI while off their island. It surfaces as an Elytra toggle in the admin settings
 * GUI. Confirm the button renders in a real server and that clicking it flips the Enabled/Disabled
 * state — proving the whole config -> Settings -> panel button -> click-handler -> saveSettings
 * chain is wired up, not just present in the JUnit tests.
 */
test('open-anywhere setting toggles in the admin settings GUI (#349)', async ({ player }) => {
  await player.makeOp();

  const settings = await openSettingsPanel(player);
  const openAnywhere = settings.locator(item => item.getDisplayName().includes('Open GUI Anywhere'));

  await expectToggleFlips(openAnywhere);
});

/**
 * Feature validation for 1.8.0 (#179): the include-undeployed option now ships in config.yml and
 * is editable from the admin settings GUI as an "Include Undeployed Challenges" barrel toggle
 * (it controls whether undeployed challenges count toward level completion). Same live wiring
 * check as above, on a different setting.
 */
test('include-undeployed setting toggles in the admin settings GUI (#179)', async ({ player }) => {
  await player.makeOp();

  const settings = await openSettingsPanel(player);
  const includeUndeployed = settings.locator(item => item.getDisplayName().includes('Include Undeployed'));

  await expectToggleFlips(includeUndeployed);
});

/**
 * Open the Challenges admin GUI and click through to the Settings sub-panel, returning a live
 * handle to it. The admin menu is registered under the BSkyBlock admin command; the "Settings"
 * button opens EditSettingsPanel (window title "Settings").
 */
async function openSettingsPanel(player: PlayerWrapper): Promise<LiveGuiHandle> {
  player.chat('/bsbadmin challenges');
  const admin = await player.gui({ title: /Challenges Admin/i });
  await admin.locator(item => item.getDisplayName().includes('Settings')).click();
  return player.gui({ title: /Settings/i });
}

/**
 * Assert a settings toggle button really toggles on a live server: read its current Enabled/Disabled
 * state, click it and assert the state flipped, then click again to restore it. Restoring keeps the
 * test independent of the persisted run-dir config and of the order tests run in.
 */
async function expectToggleFlips(button: GuiItemLocator): Promise<void> {
  // Wait for the panel to finish drawing this button with a known toggle state.
  await expect.poll(() => button.loreText()).toMatch(/Enabled|Disabled/);
  const wasEnabled = button.loreText().includes('Enabled');

  await button.click();
  await expect(button).toHaveLore(wasEnabled ? 'Disabled' : 'Enabled');

  // Put it back the way we found it.
  await button.click();
  await expect(button).toHaveLore(wasEnabled ? 'Enabled' : 'Disabled');
}
