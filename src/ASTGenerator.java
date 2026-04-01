// ============================================================
// ASTGenerator.java — Abstract Syntax Tree (text-based)
// Works for correct AND wrong input — always shows output
// ============================================================
import java.util.*;

public class ASTGenerator {

    public static String generate(List<Token> tokens) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Abstract Syntax Tree (AST) ===\n\n");

        if (tokens.isEmpty()) {
            sb.append("  ✘ ERROR: No tokens found. Cannot build AST.\n");
            return sb.toString();
        }

        // Extract with fallbacks
        Token tokLeft  = tokens.size() > 1 ? tokens.get(1) : null;
        Token tokOp    = tokens.size() > 2 ? tokens.get(2) : null;
        Token tokRight = tokens.size() > 3 ? tokens.get(3) : null;
        Token tokTgt   = tokens.size() > 5 ? tokens.get(5) : null;
        Token tokVal   = tokens.size() > 7 ? tokens.get(7) : null;

        String left  = tokLeft  != null ? tokLeft.getValue()  : "?";
        String op    = tokOp    != null ? tokOp.getValue()    : "?";
        String right = tokRight != null ? tokRight.getValue() : "?";
        String tgt   = tokTgt   != null ? tokTgt.getValue()   : "?";
        String val   = tokVal   != null ? tokVal.getValue()   : "?";

        boolean incomplete = left.equals("?") || op.equals("?") || right.equals("?")
                          || tgt.equals("?") || val.equals("?");
        if (incomplete) {
            sb.append("  ⚠ Input is incomplete. AST shown with '?' for missing parts.\n\n");
        }

        // ── Draw AST ─────────────────────────────────────────
        sb.append("  IF_STATEMENT\n");

        // Check if 'if' is present
        boolean hasIf = tokens.stream().anyMatch(
            t -> t.getType() == Token.Type.KEYWORD && t.getValue().equals("if"));
        boolean hasThen = tokens.stream().anyMatch(
            t -> t.getType() == Token.Type.KEYWORD && t.getValue().equals("then"));

        if (!hasIf) sb.append("  │   [Missing keyword 'if']\n");

        sb.append("  ├── CONDITION\n");
        sb.append("  │   └── BINARY_EXPR\n");
        sb.append("  │       ├── OPERATOR  : \"").append(op).append("\"");
        if (op.equals("?")) sb.append("  ← missing relational operator");
        sb.append("\n");
        sb.append("  │       ├── LEFT\n");
        sb.append("  │       │   └── ").append(nodeLabel(tokLeft))
          .append(" : \"").append(left).append("\"");
        if (left.equals("?")) sb.append("  ← missing left operand");
        sb.append("\n");
        sb.append("  │       └── RIGHT\n");
        sb.append("  │           └── ").append(nodeLabel(tokRight))
          .append(" : \"").append(right).append("\"");
        if (right.equals("?")) sb.append("  ← missing right operand");
        sb.append("\n");

        if (!hasThen) sb.append("  │   [Missing keyword 'then']\n");

        sb.append("  └── THEN_BRANCH\n");
        sb.append("      └── ASSIGN_STMT\n");
        sb.append("          ├── TARGET\n");
        sb.append("          │   └── IDENTIFIER : \"").append(tgt).append("\"");
        if (tgt.equals("?")) sb.append("  ← missing assignment target");
        sb.append("\n");
        sb.append("          └── VALUE\n");
        sb.append("              └── ").append(nodeLabel(tokVal))
          .append("  : \"").append(val).append("\"");
        if (val.equals("?")) sb.append("  ← missing assigned value");
        sb.append("\n\n");

        // ── Node Legend ──────────────────────────────────────
        sb.append("  Node Type Legend:\n");
        sb.append("  ").append("─".repeat(42)).append("\n");
        sb.append("  IF_STATEMENT  : Root — the entire if construct\n");
        sb.append("  CONDITION     : The boolean test (left op right)\n");
        sb.append("  BINARY_EXPR   : Expression with 2 operands + operator\n");
        sb.append("  OPERATOR      : Relational op used in condition (").append(op).append(")\n");
        sb.append("  IDENTIFIER    : Variable name node\n");
        sb.append("  NUMBER        : Numeric literal node\n");
        sb.append("  THEN_BRANCH   : Code executed when condition is true\n");
        sb.append("  ASSIGN_STMT   : Assignment statement node\n\n");

        // ── Depth Analysis ───────────────────────────────────
        sb.append("  Tree Depth Analysis:\n");
        sb.append("  ").append("─".repeat(42)).append("\n");
        sb.append("  Level 0 : IF_STATEMENT (root)\n");
        sb.append("  Level 1 : CONDITION, THEN_BRANCH\n");
        sb.append("  Level 2 : BINARY_EXPR, ASSIGN_STMT\n");
        sb.append("  Level 3 : OPERATOR, LEFT, RIGHT, TARGET, VALUE\n");
        sb.append("  Level 4 : Leaf nodes (identifiers/literals)\n\n");

        // ── Node count ───────────────────────────────────────
        sb.append("  Tree Statistics:\n");
        sb.append("  ").append("─".repeat(42)).append("\n");
        sb.append("  Total nodes    : 13 (in a complete tree)\n");
        sb.append("  Leaf nodes     : 5 (left, op, right, tgt, val)\n");
        sb.append("  Internal nodes : 8\n");
        sb.append("  Tree height    : 4\n");

        if (!incomplete) {
            sb.append("\n  ✔ AST generation complete.\n");
        } else {
            sb.append("\n  ⚠ AST generated with placeholders for missing parts.\n");
            sb.append("  → Fix the input for a complete tree.\n");
        }
        return sb.toString();
    }

    private static String nodeLabel(Token t) {
        if (t == null) return "MISSING   ";
        return t.getType() == Token.Type.NUMBER ? "NUMBER    " : "IDENTIFIER";
    }
}
