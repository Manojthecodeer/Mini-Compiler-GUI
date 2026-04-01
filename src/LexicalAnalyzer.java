// ============================================================
// LexicalAnalyzer.java — Phase 1: Tokenization
// Works for ANY input — correct or wrong
// ============================================================
import java.util.*;

public class LexicalAnalyzer {

    private static final Set<String> KEYWORDS =
        new HashSet<>(Arrays.asList("if", "then", "else", "true", "false"));

    private static final Set<String> REL_OPS =
        new HashSet<>(Arrays.asList("==", "!=", ">=", "<=", ">", "<"));

    public static List<Token> tokenize(String input) {
        List<Token> tokens = new ArrayList<>();
        if (input == null || input.trim().isEmpty()) return tokens;
        String[] parts = input.trim().split("\\s+");
        for (String part : parts) {
            if (!part.isEmpty()) tokens.addAll(classifyPart(part));
        }
        return tokens;
    }

    private static List<Token> classifyPart(String part) {
        List<Token> result = new ArrayList<>();
        if (KEYWORDS.contains(part.toLowerCase())) {
            result.add(new Token(Token.Type.KEYWORD, part.toLowerCase())); return result;
        }
        if (REL_OPS.contains(part)) {
            result.add(new Token(Token.Type.OPERATOR, part)); return result;
        }
        if (part.equals("=")) {
            result.add(new Token(Token.Type.OPERATOR, part)); return result;
        }
        if (part.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
            result.add(new Token(Token.Type.IDENTIFIER, part)); return result;
        }
        if (part.matches("-?\\d+(\\.\\d+)?")) {
            result.add(new Token(Token.Type.NUMBER, part)); return result;
        }
        List<Token> split = splitCompound(part);
        if (!split.isEmpty()) { result.addAll(split); return result; }
        result.add(new Token(Token.Type.UNKNOWN, part));
        return result;
    }

    private static List<Token> splitCompound(String s) {
        List<Token> result = new ArrayList<>();
        for (String op : new String[]{"==", "!=", ">=", "<="}) {
            int i = s.indexOf(op);
            if (i >= 0) {
                String left = s.substring(0, i), right = s.substring(i + 2);
                if (!left.isEmpty())  result.addAll(classifyPart(left));
                result.add(new Token(Token.Type.OPERATOR, op));
                if (!right.isEmpty()) result.addAll(classifyPart(right));
                return result;
            }
        }
        for (String op : new String[]{">", "<", "="}) {
            int i = s.indexOf(op);
            if (i >= 0) {
                String left = s.substring(0, i), right = s.substring(i + 1);
                if (!left.isEmpty())  result.addAll(classifyPart(left));
                result.add(new Token(Token.Type.OPERATOR, op));
                if (!right.isEmpty()) result.addAll(classifyPart(right));
                return result;
            }
        }
        return result;
    }

    public static String buildOutput(List<Token> tokens) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Lexical Analysis ===\n\n");

        if (tokens.isEmpty()) {
            sb.append("  ✘ ERROR: No input provided — nothing to tokenize.\n");
            return sb.toString();
        }

        sb.append(String.format("  %-5s  %-12s  %-14s  %s\n", "No.", "Token Type", "Value", "Description"));
        sb.append("  ").append("─".repeat(58)).append("\n");

        int i = 1;
        boolean hasUnknown = false;
        for (Token t : tokens) {
            sb.append(String.format("  %-5d  %-12s  %-14s  %s\n",
                i++, t.getType(), "\"" + t.getValue() + "\"", describe(t)));
            if (t.getType() == Token.Type.UNKNOWN) hasUnknown = true;
        }

        sb.append("\n  Total tokens found: ").append(tokens.size()).append("\n\n");

        Map<Token.Type, Integer> counts = new LinkedHashMap<>();
        for (Token.Type ty : Token.Type.values()) counts.put(ty, 0);
        for (Token t : tokens) counts.merge(t.getType(), 1, Integer::sum);
        sb.append("  Token Count Summary:\n");
        for (Map.Entry<Token.Type, Integer> e : counts.entrySet())
            if (e.getValue() > 0)
                sb.append(String.format("  %-12s : %d\n", e.getKey(), e.getValue()));

        sb.append("\n");
        if (hasUnknown) {
            sb.append("  ⚠ WARNING: Unknown token(s) detected!\n");
            sb.append("  → These will cause errors in later phases.\n");
            sb.append("  → Check for special characters or unsupported symbols.\n");
        } else {
            sb.append("  ✔ Tokenization complete. No unknown tokens found.\n");
        }
        return sb.toString();
    }

    private static String describe(Token t) {
        switch (t.getType()) {
            case KEYWORD:    return "Reserved word";
            case IDENTIFIER: return "Variable name";
            case NUMBER:     return "Numeric literal";
            case OPERATOR:   return t.getValue().equals("=") ? "Assignment operator" : "Relational operator";
            case UNKNOWN:    return "⚠ Unrecognized symbol";
        }
        return "";
    }
}
