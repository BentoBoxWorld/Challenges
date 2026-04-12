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

public class ResetCommandTest extends AdminCommandTestBase {

    private ResetCommand rc;
    private Challenge challenge;

    @BeforeEach
    public void setUp() {
        challenge = mock(Challenge.class);
        when(challenge.getFriendlyName()).thenReturn("Test Challenge");
        when(chm.getChallenge(anyString())).thenReturn(challenge);

        rc = new ResetCommand(addon, parentCmd);
    }

    @Test
    public void testSetup() {
        assertEquals("bskyblock.reset", rc.getPermission());
        assertEquals("challenges.commands.admin.reset.parameters", rc.getParameters());
        assertEquals("challenges.commands.admin.reset.description", rc.getDescription());
    }

    // --- execute: no args ---

    @Test
    public void testExecuteNoArgsPlayer() {
        assertFalse(rc.execute(user, "reset", Collections.emptyList()));
        mockedUtils.verify(() ->
                Utils.sendMessage(eq(user), any(World.class),
                        eq(Constants.ERRORS + "no-name")));
    }

    @Test
    public void testExecuteNoArgsConsole() {
        when(user.isPlayer()).thenReturn(false);
        assertFalse(rc.execute(user, "reset", Collections.emptyList()));
        verify(addon).logError("Missing parameters");
    }

    // --- execute: one arg (missing challenge/all) ---

    @Test
    public void testExecuteOneArgPlayer() {
        assertFalse(rc.execute(user, "reset", List.of("someplayer")));
        mockedUtils.verify(() ->
                Utils.sendMessage(eq(user), any(World.class),
                        eq(Constants.ERRORS + "missing-arguments")));
    }

    // --- execute: unknown player ---

    @Test
    public void testExecuteUnknownPlayer() {
        when(pm.getUUID(anyString())).thenReturn(null);
        assertFalse(rc.execute(user, "reset", Arrays.asList("unknown", "mychal")));
    }

    @Test
    public void testExecuteUnknownPlayerConsole() {
        when(user.isPlayer()).thenReturn(false);
        when(pm.getUUID(anyString())).thenReturn(null);
        assertFalse(rc.execute(user, "reset", Arrays.asList("unknown", "mychal")));
        verify(addon).logError("Unknown player name unknown");
    }

    // --- execute: reset all ---

    @Test
    public void testExecuteResetAll() {
        UUID targetUUID = UUID.randomUUID();
        when(pm.getUUID("tastybento")).thenReturn(targetUUID);

        assertTrue(rc.execute(user, "reset", Arrays.asList("tastybento", "all")));
        verify(chm).resetAllChallenges(eq(targetUUID), any(World.class), eq(userUUID));
    }

    @Test
    public void testExecuteResetAllConsole() {
        when(user.isPlayer()).thenReturn(false);
        UUID targetUUID = UUID.randomUUID();
        when(pm.getUUID("tastybento")).thenReturn(targetUUID);

        assertTrue(rc.execute(user, "reset", Arrays.asList("tastybento", "all")));
        verify(chm).resetAllChallenges(eq(targetUUID), any(World.class), eq(userUUID));
    }

    // --- execute: unknown challenge ---

    @Test
    public void testExecuteUnknownChallenge() {
        UUID targetUUID = UUID.randomUUID();
        when(pm.getUUID("tastybento")).thenReturn(targetUUID);
        when(chm.getChallenge(anyString())).thenReturn(null);

        assertFalse(rc.execute(user, "reset", Arrays.asList("tastybento", "badchal")));
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

        assertFalse(rc.execute(user, "reset", Arrays.asList("tastybento", "badchal")));
        verify(addon).logError("Unknown challenge badchal");
    }

    // --- execute: challenge completed -> reset ---

    @Test
    public void testExecuteResetCompletedChallenge() {
        UUID targetUUID = UUID.randomUUID();
        when(pm.getUUID("tastybento")).thenReturn(targetUUID);
        when(chm.isChallengeComplete(any(UUID.class), any(World.class), any(Challenge.class))).thenReturn(true);

        assertTrue(rc.execute(user, "reset", Arrays.asList("tastybento", "mychal")));
        verify(chm).resetChallenge(eq(targetUUID), any(World.class), eq(challenge), eq(userUUID));
    }

    @Test
    public void testExecuteResetCompletedChallengeConsole() {
        when(user.isPlayer()).thenReturn(false);
        UUID targetUUID = UUID.randomUUID();
        when(pm.getUUID("tastybento")).thenReturn(targetUUID);
        when(chm.isChallengeComplete(any(UUID.class), any(World.class), any(Challenge.class))).thenReturn(true);

        assertTrue(rc.execute(user, "reset", Arrays.asList("tastybento", "mychal")));
        verify(chm).resetChallenge(eq(targetUUID), any(World.class), eq(challenge), eq(userUUID));
    }

    // --- execute: challenge not completed ---

    @Test
    public void testExecuteResetNotCompletedChallenge() {
        UUID targetUUID = UUID.randomUUID();
        when(pm.getUUID("tastybento")).thenReturn(targetUUID);
        when(chm.isChallengeComplete(any(UUID.class), any(World.class), any(Challenge.class))).thenReturn(false);

        assertTrue(rc.execute(user, "reset", Arrays.asList("tastybento", "mychal")));
        verify(chm, never()).resetChallenge(any(), any(), any(), any());
        mockedUtils.verify(() ->
                Utils.sendMessage(eq(user), any(World.class),
                        eq(Constants.MESSAGES + "not-completed")));
    }

    @Test
    public void testExecuteResetNotCompletedConsole() {
        when(user.isPlayer()).thenReturn(false);
        UUID targetUUID = UUID.randomUUID();
        when(pm.getUUID("tastybento")).thenReturn(targetUUID);
        when(chm.isChallengeComplete(any(UUID.class), any(World.class), any(Challenge.class))).thenReturn(false);

        assertTrue(rc.execute(user, "reset", Arrays.asList("tastybento", "mychal")));
        verify(addon).log("Challenge is not completed yet");
    }

    // --- tabComplete ---

    @Test
    public void testTabCompletePlayerNames() {
        mockedUtil.when(() -> Util.getOnlinePlayerList(any(User.class))).thenReturn(List.of("alice", "bob"));
        Optional<List<String>> result = rc.tabComplete(user, "reset", Arrays.asList("reset", "x", "a"));
        assertTrue(result.isPresent());
        assertTrue(result.get().contains("alice"));
    }

    @Test
    public void testTabCompleteChallengeNamesAndAll() {
        when(chm.getAllChallengesNames(any())).thenReturn(List.of("BSkyBlock_mychal"));
        mockedUtil.when(() -> Util.tabLimit(any(), any()))
                .thenAnswer((Answer<List<String>>) inv -> inv.getArgument(0, List.class));
        Optional<List<String>> result = rc.tabComplete(user, "reset", Arrays.asList("reset", "x", "alice", "m"));
        assertTrue(result.isPresent());
        assertTrue(result.get().contains("mychal"));
        assertTrue(result.get().contains("all"));
    }

    @Test
    public void testTabCompleteDefault() {
        mockedUtil.when(() -> Util.tabLimit(any(), any()))
                .thenAnswer((Answer<List<String>>) inv -> inv.getArgument(0, List.class));
        Optional<List<String>> result = rc.tabComplete(user, "reset",
                Arrays.asList("reset", "x", "alice", "chal", "extra"));
        assertTrue(result.isPresent());
        assertEquals("help", result.get().get(0));
    }
}
