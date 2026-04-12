package world.bentobox.challenges.commands.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.util.Collections;

import org.bukkit.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import world.bentobox.bentobox.api.user.User;

public class MigrateCommandTest extends AdminCommandTestBase {

    private MigrateCommand mc;

    @BeforeEach
    public void setUp() {
        mc = new MigrateCommand(addon, parentCmd);
    }

    @Test
    public void testSetup() {
        assertEquals("bskyblock.challenges.admin", mc.getPermission());
        assertEquals("challenges.commands.admin.migrate.parameters", mc.getParameters());
        assertEquals("challenges.commands.admin.migrate.description", mc.getDescription());
    }

    @Test
    public void testExecute() {
        assertTrue(mc.execute(user, "migrate", Collections.emptyList()));
        verify(chm).migrateDatabase(any(User.class), any(World.class));
    }
}
