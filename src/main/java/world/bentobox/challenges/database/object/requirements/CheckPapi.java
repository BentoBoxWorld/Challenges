package world.bentobox.challenges.database.object.requirements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.entity.Player;

import me.clip.placeholderapi.PlaceholderAPI;

public class CheckPapi {

    /**
     * Evaluates the formula by first replacing PAPI placeholders (using the provided Player)
     * and then evaluating the resulting expression. The expression is expected to be a series
     * of numeric comparisons (using =, <>, <=, >=, <, >) joined by Boolean operators AND and OR.
     *
     * For example:
     *   "%aoneblock_my_island_lifetime_count% >= 1000 AND %Level_aoneblock_island_level% >= 100"
     *
     * If any placeholder evaluates to a non-numeric value or the formula is malformed, false is returned.
     *
     * @param player  the Player used for placeholder replacement.
     * @param formula the formula to evaluate.
     * @return true if the formula evaluates to true, false otherwise.
     */
    public static boolean evaluate(Player player, String formula) {
        // Replace PAPI placeholders with actual values using the provided Player.
        String parsedFormula = PlaceholderAPI.setPlaceholders(player, formula);

        // Tokenize the parsed formula (tokens are assumed to be separated by whitespace).
        List<String> tokens = tokenize(parsedFormula);
        if (tokens.isEmpty()) {
            return false;
        }

        try {
            Parser parser = new Parser(tokens);
            boolean result = parser.parseExpression();
            // If there are leftover tokens, the expression is malformed.
            if (parser.hasNext()) {
                return false;
            }
            return result;
        } catch (Exception e) {
            // Any error in parsing or evaluation results in false.
            return false;
        }
    }

    /**
     * Splits the given string into tokens using whitespace as the delimiter.
     *
     * @param s the string to tokenize.
     * @return a list of tokens.
     */
    private static List<String> tokenize(String s) {
        return new ArrayList<>(Arrays.asList(s.split("\\s+")));
    }

    /**
     * A simple recursive descent parser that evaluates expressions according to the following grammar:
     *
     * <pre>
     * Expression -> Term { OR Term }
     * Term       -> Factor { AND Factor }
     * Factor     -> operand operator operand
     * </pre>
     *
     * A Factor is expected to be a numeric condition in the form:
     *   number operator number
     * where operator is one of: =, <>, <=, >=, <, or >.
     */
    private static class Parser {
        private final List<String> tokens;
        private int pos = 0;

        public Parser(List<String> tokens) {
            this.tokens = tokens;
        }

        /**
         * Returns true if there are more tokens to process.
         */
        public boolean hasNext() {
            return pos < tokens.size();
        }

        /**
         * Returns the next token without advancing.
         */
        public String peek() {
            return tokens.get(pos);
        }

        /**
         * Returns the next token and advances the position.
         */
        public String next() {
            return tokens.get(pos++);
        }

        /**
         * Parses an Expression:
         *   Expression -> Term { OR Term }
         */
        public boolean parseExpression() {
            boolean value = parseTerm();
            while (hasNext() && peek().equalsIgnoreCase("OR")) {
                next(); // consume "OR"
                boolean termValue = parseTerm();
                value = value || termValue;
            }
            return value;
        }

        /**
         * Parses a Term:
         *   Term -> Factor { AND Factor }
         */
        public boolean parseTerm() {
            boolean value = parseFactor();
            while (hasNext() && peek().equalsIgnoreCase("AND")) {
                next(); // consume "AND"
                boolean factorValue = parseFactor();
                value = value && factorValue;
            }
            return value;
        }

        /**
         * Parses a Factor, which is a single condition in the form:
         *   operand operator operand
         *
         * For example: "1234 >= 1000"
         *
         * @return the boolean result of the condition.
         */
        public boolean parseFactor() {
            // There must be at least three tokens remaining.
            if (pos + 2 >= tokens.size()) {
                throw new RuntimeException("Incomplete condition");
            }

            String leftOperand = next();
            String operator = next();
            String rightOperand = next();

            // Validate operator.
            if (!operator.equals("=") && !operator.equals("<>") && !operator.equals("<=") && !operator.equals(">=")
                    && !operator.equals("<") && !operator.equals(">")) {
                throw new RuntimeException("Invalid operator: " + operator);
            }

            double leftVal, rightVal;
            try {
                leftVal = Double.parseDouble(leftOperand);
                rightVal = Double.parseDouble(rightOperand);
            } catch (NumberFormatException e) {
                // If either operand is not numeric, return false.
                return false;
            }
            // Evaluate the condition.
            switch (operator) {
            case "=":
                return Double.compare(leftVal, rightVal) == 0;
            case "<>":
                return Double.compare(leftVal, rightVal) != 0;
            case "<=":
                return leftVal <= rightVal;
            case ">=":
                return leftVal >= rightVal;
            case "<":
                return leftVal < rightVal;
            case ">":
                return leftVal > rightVal;
            default:
                // This case is never reached.
                return false;
            }
        }
    }
}