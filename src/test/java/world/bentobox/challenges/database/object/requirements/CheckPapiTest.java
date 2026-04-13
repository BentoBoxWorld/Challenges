package world.bentobox.challenges.database.object.requirements;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;

import me.clip.placeholderapi.PlaceholderAPI;

public class CheckPapiTest {

    @Mock
    private Player player;

    private AutoCloseable closeable;
    private MockedStatic<PlaceholderAPI> mockedPapi;

    @BeforeEach
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        mockedPapi = Mockito.mockStatic(PlaceholderAPI.class, Mockito.RETURNS_MOCKS);
        mockedPapi.when(() -> PlaceholderAPI.setPlaceholders(eq(player), anyString()))
                .thenAnswer((Answer<String>) invocation -> invocation.getArgument(1, String.class));
    }

    @AfterEach
    public void tearDown() throws Exception {
        mockedPapi.closeOnDemand();
        closeable.close();
        Mockito.framework().clearInlineMocks();
    }

    @Test
    public void testNumericEquality() {
        assertTrue(CheckPapi.evaluate(player, "40 == 40"));
        assertFalse(CheckPapi.evaluate(player, "40 == 50"));
        assertTrue(CheckPapi.evaluate(player, "100 = 100"));
        assertFalse(CheckPapi.evaluate(player, "100 = 101"));
    }

    @Test
    public void testNumericComparison() {
        assertTrue(CheckPapi.evaluate(player, "40 > 20"));
        assertFalse(CheckPapi.evaluate(player, "20 > 40"));
        assertTrue(CheckPapi.evaluate(player, "20 < 40"));
        assertFalse(CheckPapi.evaluate(player, "40 < 20"));
        assertTrue(CheckPapi.evaluate(player, "30 <= 30"));
        assertFalse(CheckPapi.evaluate(player, "31 <= 30"));
        assertTrue(CheckPapi.evaluate(player, "30 >= 30"));
        assertFalse(CheckPapi.evaluate(player, "29 >= 30"));
        assertTrue(CheckPapi.evaluate(player, "40 > 20 extra"));
    }

    @Test
    public void testStringEquality() {
        assertTrue(CheckPapi.evaluate(player, "john smith == john smith"));
        assertFalse(CheckPapi.evaluate(player, "john smith == jane doe"));
        assertTrue(CheckPapi.evaluate(player, "john smith <> jane doe"));
        assertFalse(CheckPapi.evaluate(player, "john smith <> john smith"));
    }

    @Test
    public void testStringLexicographicalComparison() {
        assertTrue(CheckPapi.evaluate(player, "apple < banana"));
        assertTrue(CheckPapi.evaluate(player, "banana > apple"));
        assertTrue(CheckPapi.evaluate(player, "cat >= cat"));
        assertTrue(CheckPapi.evaluate(player, "cat <= cat"));
    }

    @Test
    public void testMultipleConditionsAndOr() {
        assertTrue(CheckPapi.evaluate(player, "john smith == john smith AND 40 > 20"));
        assertTrue(CheckPapi.evaluate(player, "john smith == jane doe OR 40 > 20"));
        assertFalse(CheckPapi.evaluate(player, "john smith == jane doe AND 40 > 20"));
        assertTrue(CheckPapi.evaluate(player, "john smith == jane doe OR 40 > 20 AND 10 < 20"));
    }

    @Test
    public void testInvalidFormula() {
        assertFalse(CheckPapi.evaluate(player, "40 40"));
        assertFalse(CheckPapi.evaluate(player, "40 >"));
    }

}
