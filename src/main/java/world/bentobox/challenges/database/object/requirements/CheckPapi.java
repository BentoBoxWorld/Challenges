package world.bentobox.challenges.database.object.requirements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.entity.Player;

import me.clip.placeholderapi.PlaceholderAPI;

public class CheckPapi {

    /**
     * Evaluates the given formula by first replacing PAPI placeholders using the provided Player,
     * then parsing and evaluating one or more conditions.
     * <p>
     * The formula may contain conditions comparing numeric or string values.
     * Operands may contain spaces. The grammar for a condition is:
     * <pre>
     *     leftOperand operator rightOperand
     * </pre>
     * where the leftOperand is a sequence of tokens (separated by whitespace) until a valid
     * operator is found, and the rightOperand is a sequence of tokens until a boolean operator
     * ("AND" or "OR") is encountered or the end of the formula is reached.
     * <p>
     * Supported comparison operators (case sensitive) are:
     * <ul>
     *     <li>"=" or "==" for equality</li>
     *     <li>"<>" or "!=" for inequality</li>
     *     <li>"<=" and ">=" for less than or equal and greater than or equal</li>
     *     <li>"<" and ">" for less than and greater than</li>
     * </ul>
     * 
     * For strings:
     * <ul>
     *     <li>"=" for case insensitive equality</li>
     *     <li>"==" for case-sensitive equality</li>
     *     <li>"<>" for case-insensitive inequality</li>
     *     <li>"!=" for case sensitive inequality</li>
     * </ul>
     * Boolean connectors "AND" and "OR" (case insensitive) combine multiple conditions;
     * AND has higher precedence than OR.
     * <p>
     * Examples:
     * <pre>
     *     "%aoneblock_my_island_lifetime_count% >= 1000 AND %aoneblock_my_island_level% >= 100"
     *     "john smith == tasty bento AND 40 > 20"
     * </pre>
     *
     * @param player  the Player used for placeholder replacement
     * @param formula the formula to evaluate
     * @return true if the formula evaluates to true, false otherwise.
     */
    public static boolean evaluate(Player player, String formula) {
        // Replace PAPI placeholders with actual values.
        String parsedFormula = PlaceholderAPI.setPlaceholders(player, formula);

        // Tokenize the resulting formula by whitespace.
        List<String> tokens = tokenize(parsedFormula);
        if (tokens.isEmpty()) {
            return false;
        }

        try {
            Parser parser = new Parser(tokens);
            boolean result = parser.parseExpression();
            // If there are extra tokens after parsing the full expression, the formula is malformed.
            if (parser.hasNext()) {
                return false;
            }
            return result;
        } catch (Exception e) {
            // Any error in parsing or evaluating the expression results in false.
            return false;
        }
    }

    /**
     * Splits a string into tokens using whitespace as the delimiter.
     *
     * @param s the string to tokenize.
     * @return a list of tokens.
     */
    private static List<String> tokenize(String s) {
        return new ArrayList<>(Arrays.asList(s.split("\\s+")));
    }

    /**
     * A simple recursive descent parser that evaluates the formula.
     * It supports multi-token operands for conditions.
     */
    private static class Parser {
        private final List<String> tokens;
        private int pos = 0;

        public Parser(List<String> tokens) {
            this.tokens = tokens;
        }

        public boolean hasNext() {
            return pos < tokens.size();
        }

        public String peek() {
            return tokens.get(pos);
        }

        public String next() {
            return tokens.get(pos++);
        }

        /**
         * Parses an Expression:
         * Expression -> Term { OR Term }
         *
         * @return the boolean value of the expression.
         */
        public boolean parseExpression() {
            boolean value = parseTerm();
            while (hasNext() && isOr(peek())) {
                next(); // consume "OR"
                boolean termValue = parseTerm();
                value = value || termValue;
            }
            return value;
        }

        /**
         * Parses a Term:
         * Term -> Condition { AND Condition }
         *
         * @return the boolean value of the term.
         */
        public boolean parseTerm() {
            boolean value = parseCondition();
            while (hasNext() && isAnd(peek())) {
                next(); // consume "AND"
                boolean conditionValue = parseCondition();
                value = value && conditionValue;
            }
            return value;
        }

        /**
         * Parses a single condition of the form:
         * leftOperand operator rightOperand
         * <p>
         * The left operand is built by collecting tokens until a valid operator is found.
         * The right operand is built by collecting tokens until a boolean operator ("AND" or "OR")
         * is encountered or the end of the token list is reached.
         *
         * @return the boolean result of the condition.
         */
        public boolean parseCondition() {
            // Parse left operand.
            StringBuilder leftSB = new StringBuilder();
            if (!hasNext()) {
                throw new RuntimeException("Expected left operand but reached end of expression");
            }
            // Collect tokens for the left operand until an operator is encountered.
            while (hasNext() && !isOperator(peek())) {
                if (leftSB.length() > 0) {
                    leftSB.append(" ");
                }
                leftSB.append(next());
            }
            if (!hasNext()) {
                throw new RuntimeException("Operator expected after left operand");
            }
            // Next token should be an operator.
            String operator = next();
            if (!isValidOperator(operator)) {
                throw new RuntimeException("Invalid operator: " + operator);
            }
            // Parse right operand.
            StringBuilder rightSB = new StringBuilder();
            while (hasNext() && !isBooleanOperator(peek())) {
                if (rightSB.length() > 0) {
                    rightSB.append(" ");
                }
                rightSB.append(next());
            }
            String leftOperand = leftSB.toString().trim();
            String rightOperand = rightSB.toString().trim();

            // Evaluate the condition:
            // If both operands can be parsed as numbers, use numeric comparison;
            // otherwise, perform string comparison.
            Double leftNum = tryParseDouble(leftOperand);
            Double rightNum = tryParseDouble(rightOperand);
            if (leftNum != null && rightNum != null) {
                // Numeric comparison.
                switch (operator) {
                case "=":
                case "==":
                    return Double.compare(leftNum, rightNum) == 0;
                case "<>":
                case "!=":
                    return Double.compare(leftNum, rightNum) != 0;
                case "<=":
                    return leftNum <= rightNum;
                case ">=":
                    return leftNum >= rightNum;
                case "<":
                    return leftNum < rightNum;
                case ">":
                    return leftNum > rightNum;
                default:
                    throw new RuntimeException("Unsupported operator: " + operator);
                }
            } else {
                // String comparison.
                switch (operator) {
                case "=":
                    return leftOperand.equalsIgnoreCase(rightOperand);
                case "==":
                    return leftOperand.equals(rightOperand);
                case "<>":
                    return !leftOperand.equalsIgnoreCase(rightOperand);
                case "!=":
                    return !leftOperand.equals(rightOperand);
                case "<=":
                    return leftOperand.compareTo(rightOperand) <= 0;
                case ">=":
                    return leftOperand.compareTo(rightOperand) >= 0;
                case "<":
                    return leftOperand.compareTo(rightOperand) < 0;
                case ">":
                    return leftOperand.compareTo(rightOperand) > 0;
                default:
                    throw new RuntimeException("Unsupported operator: " + operator);
                }
            }
        }

        /**
         * Checks if the given token is one of the valid comparison operators.
         */
        private boolean isValidOperator(String token) {
            return token.equals("=") || token.equals("==") || token.equals("<>") || token.equals("!=")
                    || token.equals("<=") || token.equals(">=") || token.equals("<") || token.equals(">");
        }

        /**
         * Returns true if the token is a comparison operator.
         */
        private boolean isOperator(String token) {
            return isValidOperator(token);
        }

        /**
         * Returns true if the token is a boolean operator ("AND" or "OR").
         */
        private boolean isBooleanOperator(String token) {
            return token.equalsIgnoreCase("AND") || token.equalsIgnoreCase("OR");
        }

        private boolean isAnd(String token) {
            return token.equalsIgnoreCase("AND");
        }

        private boolean isOr(String token) {
            return token.equalsIgnoreCase("OR");
        }

        /**
         * Tries to parse the given string as a Double.
         * Returns the Double if successful, or null if parsing fails.
         */
        private Double tryParseDouble(String s) {
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }
}