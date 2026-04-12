package world.bentobox.challenges.commands.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ChallengesAdminCommandTest extends AdminCommandTestBase {

    private ChallengesAdminCommand cac;

    @BeforeEach
    public void setUp() {
        cac = new ChallengesAdminCommand(addon, parentCmd);
    }

    @Test
    public void testSetup() {
        assertEquals("bskyblock.admin.challenges", cac.getPermission());
        assertEquals("challenges.commands.admin.main.parameters", cac.getParameters());
        assertEquals("challenges.commands.admin.main.description", cac.getDescription());
        // Sub-commands: reload, complete, reset, show, migrate
        assertEquals(5, cac.getSubCommands(true).size());
    }

    @Test
    public void testExecutePlayer() {
        // Player should get the admin panel opened (returns true)
        assertTrue(cac.execute(user, "challenges", Collections.emptyList()));
    }

    @Test
    public void testExecuteConsole() {
        when(user.isPlayer()).thenReturn(false);
        // Console gets help shown (returns false)
        assertFalse(cac.execute(user, "challenges", Collections.emptyList()));
    }
}
