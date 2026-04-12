package world.bentobox.challenges.commands.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;
import world.bentobox.challenges.database.object.Challenge;
import world.bentobox.challenges.utils.Constants;
import world.bentobox.challenges.utils.Utils;

public class CompleteCommandTest extends AdminCommandTestBase {

    private CompleteCommand cc;
    private Challenge challenge;

    @BeforeEach
    public void setUp() {
        challenge = mock(Challenge.class);
        when(challenge.getFriendlyName()).thenReturn("Test Challenge");
        when(chm.getChallenge(anyString())).thenReturn(challenge);

        cc = new CompleteCommand(addon, parentCmd);
    }

    @Test
    public void testSetup() {
        assertEquals("bskyblock.complete", cc.getPermission());
        assertEquals("challenges.commands.admin.complete.parameters", cc.getParameters());
        assertEquals("challenges.commands.admin.complete.description", cc.getDescription());
    }

    // --- execute: no args ---

    @Test
    public void testExecuteNoArgsPlayer() {
        assertFalse(cc.execute(user, "complete", Collections.emptyList()));
        mockedUtils.verify(() ->
                Utils.sendMessage(eq(user), any(World.class),
                        eq(Constants.ERRORS + "no-name")));
    }

    @Test
    public void testExecuteNoArgsConsole() {
        when(user.isPlayer()).thenReturn(false);
        assertFalse(cc.execute(user, "complete", Collections.emptyList()));
        verify(addon).logError("Missing parameters");
    }

    // --- execute: one arg (missing player target) ---

    @Test
    public void testExecuteOneArgPlayer() {
        assertFalse(cc.execute(user, "complete", List.of("someplayer")));
        mockedUtils.verify(() ->
                Utils.sendMessage(eq(user), any(World.class),
                        eq(Constants.ERRORS + "missing-arguments")));
    }

    @Test
    public void testExecuteOneArgConsole() {
        when(user.isPlayer()).thenReturn(false);
        assertFalse(cc.execute(user, "complete", List.of("someplayer")));
        verify(addon).logError("Missing parameters");
    }

    // --- execute: unknown player ---

    @Test
    public void testExecuteUnknownPlayer() {
        when(pm.getUUID(anyString())).thenReturn(null);
        assertFalse(cc.execute(user, "complete", Arrays.asList("unknown", "mychal")));
    }

    @Test
    public void testExecuteUnknownPlayerConsole() {
        when(user.isPlayer()).thenReturn(false);
        when(pm.getUUID(anyString())).thenReturn(null);
        assertFalse(cc.execute(user, "complete", Arrays.asList("unknown", "mychal")));
        verify(addon).logError("Unknown player name unknown");
    }

    // --- execute: unknown challenge ---

    @Test
    public void testExecuteUnknownChallenge() {
        UUID targetUUID = UUID.randomUUID();
        when(pm.getUUID("tastybento")).thenReturn(targetUUID);
        when(chm.getChallenge(anyString())).thenReturn(null);

        assertFalse(cc.execute(user, "complete", Arrays.asList("tastybento", "badchal")));
        mockedUtils.verify(() ->
                Utils.sendMessage(eq(user), any(World.class),
                        eq(Constants.ERRORS + "unknown-challenge")));
    }

    @Test
    public void testExecuteUnknownChallengeConsole() {
        when(user.isPlayer()).thenReturn(false);
        UUID targetUUID = UUID.randomUUID();
        when(pm.getUUID("tastybento")).thenReturn(targetUUID);
        when(chm.getChallenge(anyString())).thenReturn(null);

        assertFalse(cc.execute(user, "complete", Arrays.asList("tastybento", "badchal")));
        verify(addon).logError("Unknown challenge badchal");
    }

    // --- execute: challenge already completed ---

    @Test
    public void testExecuteAlreadyCompleted() {
        UUID targetUUID = UUID.randomUUID();
        when(pm.getUUID("tastybento")).thenReturn(targetUUID);
        when(chm.isChallengeComplete(any(UUID.class), any(World.class), any(Challenge.class))).thenReturn(true);

        assertTrue(cc.execute(user, "complete", Arrays.asList("tastybento", "mychal")));
        verify(chm, never()).setChallengeComplete(any(UUID.class), any(World.class), any(Challenge.class), any(UUID.class));
        mockedUtils.verify(() ->
                Utils.sendMessage(eq(user), any(World.class),
                        eq(Constants.MESSAGES + "already-completed")));
    }

    // --- execute: success ---

    @Test
    public void testExecuteSuccess() {
        UUID targetUUID = UUID.randomUUID();
        when(pm.getUUID("tastybento")).thenReturn(targetUUID);
        when(chm.isChallengeComplete(any(UUID.class), any(World.class), any(Challenge.class))).thenReturn(false);

        assertTrue(cc.execute(user, "complete", Arrays.asList("tastybento", "mychal")));
        verify(chm).setChallengeComplete(eq(targetUUID), any(World.class), eq(challenge), eq(userUUID));
    }

    @Test
    public void testExecuteSuccessConsole() {
        when(user.isPlayer()).thenReturn(false);
        UUID targetUUID = UUID.randomUUID();
        when(pm.getUUID("tastybento")).thenReturn(targetUUID);
        when(chm.isChallengeComplete(any(UUID.class), any(World.class), any(Challenge.class))).thenReturn(false);

        assertTrue(cc.execute(user, "complete", Arrays.asList("tastybento", "mychal")));
        verify(chm).setChallengeComplete(eq(targetUUID), any(World.class), eq(challenge), eq(userUUID));
    }

    // --- tabComplete ---

    @Test
    public void testTabCompletePlayerNames() {
        mockedUtil.when(() -> Util.getOnlinePlayerList(any(User.class))).thenReturn(List.of("alice", "bob"));
        // args size 3 = player name position (label + "complete" + partial)
        Optional<List<String>> result = cc.tabComplete(user, "complete", Arrays.asList("complete", "x", "a"));
        assertTrue(result.isPresent());
        assertTrue(result.get().contains("alice"));
    }

    @Test
    public void testTabCompleteChallengeNames() {
        when(chm.getAllChallengesNames(any())).thenReturn(List.of("BSkyBlock_mychal", "BSkyBlock_otherchal"));
        mockedUtil.when(() -> Util.tabLimit(any(), any()))
                .thenAnswer((Answer<List<String>>) inv -> inv.getArgument(0, List.class));
        Optional<List<String>> result = cc.tabComplete(user, "complete", Arrays.asList("complete", "x", "alice", "m"));
        assertTrue(result.isPresent());
        assertTrue(result.get().contains("mychal"));
        assertTrue(result.get().contains("otherchal"));
    }

    @Test
    public void testTabCompleteDefault() {
        mockedUtil.when(() -> Util.tabLimit(any(), any()))
                .thenAnswer((Answer<List<String>>) inv -> inv.getArgument(0, List.class));
        Optional<List<String>> result = cc.tabComplete(user, "complete",
                Arrays.asList("complete", "x", "alice", "chal", "extra"));
        assertTrue(result.isPresent());
        assertEquals("help", result.get().get(0));
    }
}
