package world.bentobox.challenges.database.object.requirements;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import me.clip.placeholderapi.PlaceholderAPI;

@RunWith(PowerMockRunner.class)
@PrepareForTest(PlaceholderAPI.class)
public class CheckPapiTest {

    @Mock
    private Player player;

    @Before
    public void setUp() {
        PowerMockito.mockStatic(PlaceholderAPI.class, Mockito.RETURNS_MOCKS);
        // Return back the input string
        when(PlaceholderAPI.setPlaceholders(eq(player), anyString()))
                .thenAnswer((Answer<String>) invocation -> invocation.getArgument(1, String.class));
    }

    @Test
    public void testNumericEquality() {
        // Using numeric equality comparisons.
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
        // Extra tokens beyond a valid expression.
        assertTrue(CheckPapi.evaluate(player, "40 > 20 extra"));

    }

    @Test
    public void testStringEquality() {
        // String comparisons with multi-word operands.
        assertTrue(CheckPapi.evaluate(player, "john smith == john smith"));
        assertFalse(CheckPapi.evaluate(player, "john smith == jane doe"));
        // Using inequality operators.
        assertTrue(CheckPapi.evaluate(player, "john smith <> jane doe"));
        assertFalse(CheckPapi.evaluate(player, "john smith <> john smith"));
    }

    @Test
    public void testStringLexicographicalComparison() {
        // Lexicographical comparison using string compareTo semantics.
        assertTrue(CheckPapi.evaluate(player, "apple < banana"));
        assertTrue(CheckPapi.evaluate(player, "banana > apple"));
        assertTrue(CheckPapi.evaluate(player, "cat >= cat"));
        assertTrue(CheckPapi.evaluate(player, "cat <= cat"));
    }

    @Test
    public void testMultipleConditionsAndOr() {
        // AND has higher precedence than OR.
        // "john smith == john smith AND 40 > 20" should be true.
        assertTrue(CheckPapi.evaluate(player, "john smith == john smith AND 40 > 20"));
        // "john smith == jane doe OR 40 > 20" should be true because second condition is true.
        assertTrue(CheckPapi.evaluate(player, "john smith == jane doe OR 40 > 20"));
        // "john smith == jane doe AND 40 > 20" should be false because first condition fails.
        assertFalse(CheckPapi.evaluate(player, "john smith == jane doe AND 40 > 20"));
        // Mixed AND and OR: AND is evaluated first.
        // Equivalent to: (john smith == jane doe) OR ((40 > 20) AND (10 < 20))
        assertTrue(CheckPapi.evaluate(player, "john smith == jane doe OR 40 > 20 AND 10 < 20"));
    }

    @Test
    public void testInvalidFormula() {
        // Missing operator between operands.
        assertFalse(CheckPapi.evaluate(player, "40 40"));
        // Incomplete condition.
        assertFalse(CheckPapi.evaluate(player, "40 >"));
    }

}
