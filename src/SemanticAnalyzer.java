// ============================================================
// SemanticAnalyzer.java — Phase 3: Semantic Analysis
// Works for correct AND wrong input — always shows output
// ============================================================
import java.util.*;

public class SemanticAnalyzer {

    public static String analyze(List<Token> tokens) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Semantic Analysis ===\n\n");

        if (tokens.isEmpty()) {
            sb.append("  ✘ ERROR: No tokens to analyze.\n");
            return sb.toString();
        }

        // ── Build Symbol Table from ALL identifiers found ────
        Map<String, String> symbolTable = new LinkedHashMap<>();
        for (Token t : tokens) {
            if (t.getType() == Token.Type.IDENTIFIER) {
                symbolTable.put(t.getValue(), "int");
            }
        }

        sb.append("  Symbol Table (all identifiers found):\n");
        sb.append("  ").append("─".repeat(32)).append("\n");
        if (symbolTable.isEmpty()) {
            sb.append("  (no identifiers found)\n");
        } else {
            sb.append(String.format("  %-14s  %-8s  %s\n", "Variable", "Type", "Role"));
            sb.append("  ").append("─".repeat(32)).append("\n");
            // Assign roles based on position
            String leftOp  = tokens.size() > 1 ? tokens.get(1).getValue() : null;
            String rightOp = tokens.size() > 3 ? tokens.get(3).getValue() : null;
            String tgt     = tokens.size() > 5 ? tokens.get(5).getValue() : null;
            String val     = tokens.size() > 7 ? tokens.get(7).getValue() : null;
            for (String var : symbolTable.keySet()) {
                String role = "identifier";
                if (var.equals(leftOp))  role = "condition left";
                else if (var.equals(rightOp)) role = "condition right";
                else if (var.equals(tgt))     role = "assign target";
                else if (var.equals(val))     role = "assign value";
                sb.append(String.format("  %-14s  %-8s  %s\n", var, "int", role));
            }
        }
        sb.append("\n");

        // ── Semantic Checks (run all checks regardless of token count) ──
        List<String> warnings = new ArrayList<>();
        List<String> errors   = new ArrayList<>();
        List<String> infos    = new ArrayList<>();

        // Check 1: 'if' keyword present?
        boolean hasIf = tokens.stream().anyMatch(
            t -> t.getType() == Token.Type.KEYWORD && t.getValue().equals("if"));
        if (!hasIf)
            errors.add("Keyword 'if' not found in input.");

        // Check 2: 'then' keyword present?
        boolean hasThen = tokens.stream().anyMatch(
            t -> t.getType() == Token.Type.KEYWORD && t.getValue().equals("then"));
        if (!hasThen)
            errors.add("Keyword 'then' not found in input.");

        // Check 3: Relational operator present?
        boolean hasRelOp = tokens.stream().anyMatch(t -> {
            Set<String> ops = new HashSet<>(Arrays.asList(">","<",">=","<=","==","!="));
            return t.getType() == Token.Type.OPERATOR && ops.contains(t.getValue());
        });
        if (!hasRelOp)
            errors.add("No relational operator found (need: >, <, >=, <=, ==, !=).");

        // Check 4: Assignment operator '=' present?
        boolean hasAssign = tokens.stream().anyMatch(
            t -> t.getType() == Token.Type.OPERATOR && t.getValue().equals("="));
        if (!hasAssign)
            errors.add("No assignment operator '=' found in THEN branch.");

        // Check 5: Self-comparison (a > a always false/true)
        if (tokens.size() > 3 &&
            tokens.get(1).getType() == Token.Type.IDENTIFIER &&
            tokens.get(3).getType() == Token.Type.IDENTIFIER &&
            tokens.get(1).getValue().equals(tokens.get(3).getValue())) {
            String op = tokens.size() > 2 ? tokens.get(2).getValue() : "";
            if (op.equals(">") || op.equals("<") || op.equals("!="))
                warnings.add("ALWAYS FALSE: '" + tokens.get(1).getValue() + " " + op + " " +
                    tokens.get(3).getValue() + "' — a variable cannot be " + op + " itself.");
            else if (op.equals(">=") || op.equals("<=") || op.equals("=="))
                warnings.add("ALWAYS TRUE: '" + tokens.get(1).getValue() + " " + op + " " +
                    tokens.get(3).getValue() + "' — a variable is always " + op + " itself.");
        }

        // Check 6: Constant condition evaluation (5 > 3 etc.)
        if (tokens.size() > 3 &&
            tokens.get(1).getType() == Token.Type.NUMBER &&
            tokens.get(3).getType() == Token.Type.NUMBER) {
            try {
                double l = Double.parseDouble(tokens.get(1).getValue());
                double r = Double.parseDouble(tokens.get(3).getValue());
                String op = tokens.get(2).getValue();
                boolean res = evalCond(l, op, r);
                infos.add("Constant condition detected: " + l + " " + op + " " + r +
                    " → always " + (res ? "TRUE" : "FALSE"));
            } catch (Exception ignored) {}
        }

        // Check 7: Redundant self-assignment (x = x)
        if (tokens.size() > 7 &&
            tokens.get(5).getType() == Token.Type.IDENTIFIER &&
            tokens.get(7).getType() == Token.Type.IDENTIFIER &&
            tokens.get(5).getValue().equals(tokens.get(7).getValue())) {
            warnings.add("REDUNDANT ASSIGNMENT: '" + tokens.get(5).getValue() +
                " = " + tokens.get(7).getValue() + "' has no effect.");
        }

        // Check 8: Unknown tokens
        for (Token t : tokens) {
            if (t.getType() == Token.Type.UNKNOWN)
                errors.add("Unknown symbol '" + t.getValue() + "' — not a valid token.");
        }

        // ── Print Results ────────────────────────────────────
        if (!errors.isEmpty()) {
            sb.append("  ERRORS (").append(errors.size()).append("):\n");
            for (String e : errors) sb.append("    ✘ ").append(e).append("\n");
            sb.append("\n");
        }
        if (!warnings.isEmpty()) {
            sb.append("  WARNINGS (").append(warnings.size()).append("):\n");
            for (String w : warnings) sb.append("    ⚠ ").append(w).append("\n");
            sb.append("\n");
        }
        if (!infos.isEmpty()) {
            sb.append("  INFO:\n");
            for (String info : infos) sb.append("    ℹ ").append(info).append("\n");
            sb.append("\n");
        }

        // Condition + assignment type summary
        if (tokens.size() >= 8) {
            sb.append("  Type Compatibility Check:\n");
            sb.append("  ").append("─".repeat(36)).append("\n");
            String tgt = tokens.get(5).getValue();
            String val = tokens.get(7).getValue();
            String tgtType = "int";
            String valType = tokens.get(7).getType() == Token.Type.NUMBER
                ? (val.contains(".") ? "float" : "int") : "int";
            sb.append("  Target variable : ").append(tgt).append(" (").append(tgtType).append(")\n");
            sb.append("  Assigned value  : ").append(val).append(" (").append(valType).append(")\n");
            sb.append("  Compatible      : ").append(tgtType.equals(valType) ? "✔ Yes" : "⚠ Type mismatch").append("\n");
            sb.append("\n");
        }

        if (errors.isEmpty() && warnings.isEmpty()) {
            sb.append("  ✔ Semantic analysis passed. No issues found.\n");
        } else if (errors.isEmpty()) {
            sb.append("  ✔ Semantic analysis complete with warnings above.\n");
        } else {
            sb.append("  ✘ Semantic analysis found errors. Fix input and retry.\n");
        }
        return sb.toString();
    }

    private static boolean evalCond(double l, String op, double r) {
        switch (op) {
            case ">":  return l > r;
            case "<":  return l < r;
            case ">=": return l >= r;
            case "<=": return l <= r;
            case "==": return l == r;
            case "!=": return l != r;
        }
        return false;
    }
}
