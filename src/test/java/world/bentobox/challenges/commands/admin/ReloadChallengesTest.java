package world.bentobox.challenges.commands.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReloadChallengesTest extends AdminCommandTestBase {

    private ReloadChallenges rc;

    @BeforeEach
    void setUp() {
        rc = new ReloadChallenges(addon, parentCmd);
    }

    @Test
    void testSetup() {
        assertEquals("bskyblock.admin.challenges", rc.getPermission());
        assertEquals("challenges.commands.admin.reload.parameters", rc.getParameters());
        assertEquals("challenges.commands.admin.reload.description", rc.getDescription());
    }

    @Test
    void testExecuteSoftReload() {
        assertTrue(rc.execute(user, "reload", Collections.emptyList()));
        verify(chm).load();
    }

    @Test
    void testExecuteHardReload() {
        assertTrue(rc.execute(user, "reload", List.of("hard")));
        verify(chm).reload();
    }

    @Test
    void testExecuteUnknownArg() {
        assertFalse(rc.execute(user, "reload", List.of("unknown")));
    }
}
