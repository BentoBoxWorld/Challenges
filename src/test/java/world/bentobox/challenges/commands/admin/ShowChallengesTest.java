package world.bentobox.challenges.commands.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ShowChallengesTest extends AdminCommandTestBase {

    private ShowChallenges sc;

    @BeforeEach
    void setUp() {
        sc = new ShowChallenges(addon, parentCmd);
    }

    @Test
    void testSetup() {
        assertEquals("bskyblock.admin.challenges", sc.getPermission());
        assertEquals("challenges.commands.admin.show.parameters", sc.getParameters());
        assertEquals("challenges.commands.admin.show.description", sc.getDescription());
    }

    @Test
    void testExecutePlayerSendsNames() {
        when(chm.getAllChallengesNames(any())).thenReturn(Arrays.asList("chal_one", "chal_two"));
        assertTrue(sc.execute(user, "show", Collections.emptyList()));
        verify(user).sendRawMessage("chal_one");
        verify(user).sendRawMessage("chal_two");
    }

    @Test
    void testExecutePlayerNoChallenges() {
        when(chm.getAllChallengesNames(any())).thenReturn(Collections.emptyList());
        assertTrue(sc.execute(user, "show", Collections.emptyList()));
    }

    @Test
    void testExecuteConsole() {
        when(user.isPlayer()).thenReturn(false);
        when(chm.getAllChallengesNames(any())).thenReturn(List.of("chal_one"));
        assertTrue(sc.execute(user, "show", Collections.emptyList()));
        verify(addon).log("chal_one");
    }
}
