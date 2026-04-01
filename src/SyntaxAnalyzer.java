// ============================================================
// SyntaxAnalyzer.java — Phase 2: Syntax Analysis
// Works for correct AND wrong input — always shows output
// Grammar: if <id> <relop> <id|num> then <id> = <id|num>
// ============================================================
import java.util.*;

public class SyntaxAnalyzer {

    private static final Set<String> REL_OPS =
        new HashSet<>(Arrays.asList(">", "<", ">=", "<=", "==", "!="));

    public static String analyze(List<Token> tokens) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Syntax Analysis ===\n\n");
        sb.append("  Expected Grammar:\n");
        sb.append("  if <identifier> <relop> <identifier|number>\n");
        sb.append("  then <identifier> = <identifier|number>\n\n");

        if (tokens.isEmpty()) {
            sb.append("  ✘ SYNTAX ERROR: Input is empty. Nothing to parse.\n");
            return sb.toString();
        }

        // Show what was received
        sb.append("  Received Tokens: ");
        List<String> vals = new ArrayList<>();
        for (Token t : tokens) vals.add("\"" + t.getValue() + "\"");
        sb.append(String.join("  ", vals)).append("\n\n");

        List<String> errors = new ArrayList<>();

        // ── Position-by-position checks ─────────────────────
        // [0] must be keyword 'if'
        checkKeyword(tokens, 0, "if", errors,
            "[Pos 1] Expected keyword 'if'");

        // [1] must be identifier (left operand of condition)
        checkType(tokens, 1, Token.Type.IDENTIFIER, errors,
            "[Pos 2] Expected identifier (left operand)");

        // [2] must be relational operator
        checkRelOp(tokens, 2, errors,
            "[Pos 3] Expected relational operator (>, <, >=, <=, ==, !=)");

        // [3] must be identifier or number (right operand of condition)
        checkIdOrNum(tokens, 3, errors,
            "[Pos 4] Expected identifier or number (right operand)");

        // [4] must be keyword 'then'
        checkKeyword(tokens, 4, "then", errors,
            "[Pos 5] Expected keyword 'then'");

        // [5] must be identifier (assignment target)
        checkType(tokens, 5, Token.Type.IDENTIFIER, errors,
            "[Pos 6] Expected identifier (assignment target, left of '=')");

        // [6] must be assignment operator '='
        checkAssign(tokens, 6, errors,
            "[Pos 7] Expected assignment operator '='");

        // [7] must be identifier or number (assigned value)
        if (tokens.size() > 7) {
            checkIdOrNum(tokens, 7, errors,
                "[Pos 8] Expected identifier or number (value to assign)");
        } else {
            errors.add("[Pos 8] Missing value after '=' — what should be assigned?");
        }

        // Extra tokens beyond position 7
        if (tokens.size() > 8) {
            sb.append("  ⚠ Extra tokens found after position 8 (will be ignored):\n");
            for (int i = 8; i < tokens.size(); i++) {
                sb.append("    Token ").append(i + 1).append(": ")
                  .append(tokens.get(i).getType()).append(" \"")
                  .append(tokens.get(i).getValue()).append("\"\n");
            }
            sb.append("\n");
        }

        // ── Output ──────────────────────────────────────────
        if (errors.isEmpty()) {
            sb.append("  ✔ SYNTAX VALID — Statement parsed successfully!\n\n");
            sb.append("  Parse Tree:\n");
            sb.append("  IF_STATEMENT\n");
            sb.append("  ├── CONDITION\n");
            sb.append("  │   └── BINARY_EXPR\n");
            sb.append("  │       ├── LEFT  : ").append(get(tokens, 1)).append("\n");
            sb.append("  │       ├── OP    : ").append(get(tokens, 2)).append("\n");
            sb.append("  │       └── RIGHT : ").append(get(tokens, 3)).append("\n");
            sb.append("  └── THEN_BRANCH\n");
            sb.append("      └── ASSIGN_STMT\n");
            sb.append("          ├── TARGET : ").append(get(tokens, 5)).append("\n");
            sb.append("          └── VALUE  : ").append(get(tokens, 7)).append("\n");
        } else {
            sb.append("  ✘ SYNTAX ERROR(S) FOUND: ").append(errors.size()).append(" issue(s)\n\n");
            for (String e : errors) {
                sb.append("    ✘ ").append(e).append("\n");
            }
            sb.append("\n  What was parsed (partial):\n");
            for (int i = 0; i < Math.min(tokens.size(), 8); i++) {
                Token t = tokens.get(i);
                sb.append(String.format("    Pos %-2d : %-12s \"%s\"\n",
                    i + 1, t.getType(), t.getValue()));
            }
            sb.append("\n  Fix: Use exact format:\n");
            sb.append("    if <var> <op> <var|num> then <var> = <var|num>\n");
            sb.append("  Example:\n");
            sb.append("    if a > b then x = a\n");
        }
        return sb.toString();
    }

    // ── Helpers ─────────────────────────────────────────────
    private static void checkKeyword(List<Token> tokens, int pos, String word,
                                      List<String> errors, String msg) {
        if (pos >= tokens.size()) {
            errors.add(msg + " — token missing"); return;
        }
        Token t = tokens.get(pos);
        if (t.getType() != Token.Type.KEYWORD || !t.getValue().equalsIgnoreCase(word)) {
            errors.add(msg + " — got: " + t.getType() + " \"" + t.getValue() + "\"");
        }
    }

    private static void checkType(List<Token> tokens, int pos, Token.Type expected,
                                   List<String> errors, String msg) {
        if (pos >= tokens.size()) {
            errors.add(msg + " — token missing"); return;
        }
        Token t = tokens.get(pos);
        if (t.getType() != expected) {
            errors.add(msg + " — got: " + t.getType() + " \"" + t.getValue() + "\"");
        }
    }

    private static void checkRelOp(List<Token> tokens, int pos,
                                    List<String> errors, String msg) {
        if (pos >= tokens.size()) {
            errors.add(msg + " — token missing"); return;
        }
        Token t = tokens.get(pos);
        if (t.getType() != Token.Type.OPERATOR || !REL_OPS.contains(t.getValue())) {
            errors.add(msg + " — got: " + t.getType() + " \"" + t.getValue() + "\"");
        }
    }

    private static void checkAssign(List<Token> tokens, int pos,
                                     List<String> errors, String msg) {
        if (pos >= tokens.size()) {
            errors.add(msg + " — token missing"); return;
        }
        Token t = tokens.get(pos);
        if (t.getType() != Token.Type.OPERATOR || !t.getValue().equals("=")) {
            errors.add(msg + " — got: " + t.getType() + " \"" + t.getValue() + "\"");
        }
    }

    private static void checkIdOrNum(List<Token> tokens, int pos,
                                      List<String> errors, String msg) {
        if (pos >= tokens.size()) {
            errors.add(msg + " — token missing"); return;
        }
        Token t = tokens.get(pos);
        if (t.getType() != Token.Type.IDENTIFIER && t.getType() != Token.Type.NUMBER) {
            errors.add(msg + " — got: " + t.getType() + " \"" + t.getValue() + "\"");
        }
    }

    private static String get(List<Token> tokens, int pos) {
        return pos < tokens.size() ? tokens.get(pos).getValue() : "?";
    }
}
