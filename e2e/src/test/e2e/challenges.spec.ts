import { expect, test } from '@drownek/plugwright';

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
