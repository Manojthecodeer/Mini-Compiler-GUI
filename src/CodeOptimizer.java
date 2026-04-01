// ============================================================
// CodeOptimizer.java — Phase 5: Code Optimization
// Works for correct AND wrong input — always shows output
// ============================================================
import java.util.*;

public class CodeOptimizer {

    public static String optimize(List<Token> tokens) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Code Optimization ===\n\n");

        if (tokens.isEmpty()) {
            sb.append("  ✘ ERROR: No tokens to optimize.\n");
            return sb.toString();
        }

        String left  = getVal(tokens, 1, null);
        String op    = getVal(tokens, 2, null);
        String right = getVal(tokens, 3, null);
        String tgt   = getVal(tokens, 5, null);
        String val   = tokens.size() > 7 ? tokens.get(7).getValue() : null;

        Token tokLeft  = tokens.size() > 1 ? tokens.get(1) : null;
        Token tokRight = tokens.size() > 3 ? tokens.get(3) : null;

        List<String> optimizations = new ArrayList<>();
        List<String> warnings      = new ArrayList<>();
        boolean alwaysTrue  = false;
        boolean alwaysFalse = false;

        // ── Check 1: Constant folding ─────────────────────
        if (tokLeft != null && tokRight != null && op != null &&
            tokLeft.getType() == Token.Type.NUMBER &&
            tokRight.getType() == Token.Type.NUMBER) {
            try {
                double l = Double.parseDouble(left);
                double r = Double.parseDouble(right);
                boolean res = evalCond(l, op, r);
                if (res) {
                    alwaysTrue = true;
                    optimizations.add("CONSTANT FOLDING: " + left + " " + op + " " + right + " = TRUE");
                    optimizations.add("  → Condition is always TRUE at compile time.");
                    optimizations.add("  → Conditional jump (if/goto) can be removed.");
                    optimizations.add("  → False branch (L_FALSE) is DEAD CODE — eliminated.");
                } else {
                    alwaysFalse = true;
                    optimizations.add("CONSTANT FOLDING: " + left + " " + op + " " + right + " = FALSE");
                    optimizations.add("  → Condition is always FALSE at compile time.");
                    optimizations.add("  → True branch (L_TRUE) is DEAD CODE — eliminated.");
                    if (tgt != null && val != null)
                        optimizations.add("  → Assignment '" + tgt + " = " + val + "' will NEVER execute.");
                }
            } catch (Exception ignored) {}
        }

        // ── Check 2: Self-comparison ──────────────────────
        if (tokLeft != null && tokRight != null && op != null &&
            tokLeft.getType() == Token.Type.IDENTIFIER &&
            tokRight.getType() == Token.Type.IDENTIFIER &&
            tokLeft.getValue().equals(tokRight.getValue())) {
            if (op.equals(">") || op.equals("<") || op.equals("!=")) {
                alwaysFalse = true;
                warnings.add("ALWAYS FALSE: '" + left + " " + op + " " + right +
                    "' — variable cannot be " + op + " itself.");
                warnings.add("  → True branch is dead code — assignment never runs.");
            } else if (op.equals(">=") || op.equals("<=") || op.equals("==")) {
                alwaysTrue = true;
                warnings.add("ALWAYS TRUE: '" + left + " " + op + " " + right +
                    "' — variable is always " + op + " itself.");
                warnings.add("  → False branch is dead code — always executes assignment.");
            }
        }

        // ── Check 3: Redundant self-assignment ────────────
        if (tgt != null && val != null && tgt.equals(val)) {
            warnings.add("REDUNDANT ASSIGNMENT: '" + tgt + " = " + val +
                "' — assigning variable to itself. No effect.");
        }

        // ── Check 4: Missing parts ────────────────────────
        if (tokens.size() < 8) {
            warnings.add("INCOMPLETE INPUT: Cannot fully optimize — some tokens are missing.");
            warnings.add("  → Optimization results are partial.");
        }

        // ── Check 5: Unknown tokens ───────────────────────
        for (Token t : tokens) {
            if (t.getType() == Token.Type.UNKNOWN)
                warnings.add("UNKNOWN TOKEN: '" + t.getValue() + "' — optimization may be inaccurate.");
        }

        // ── Print checks performed ────────────────────────
        sb.append("  Optimization Checks Performed:\n");
        sb.append("  ").append("─".repeat(42)).append("\n");
        sb.append("  [1] Constant Folding     (evaluate at compile time)\n");
        sb.append("  [2] Dead Code Elimination (remove unreachable branches)\n");
        sb.append("  [3] Self-comparison       (always true/false detection)\n");
        sb.append("  [4] Redundant Assignment  (x = x has no effect)\n");
        sb.append("\n");

        // ── Print findings ───────────────────────────────
        if (!optimizations.isEmpty()) {
            sb.append("  Optimizations Applied:\n");
            for (String o : optimizations) sb.append("    ✔ ").append(o).append("\n");
            sb.append("\n");
        }
        if (!warnings.isEmpty()) {
            sb.append("  Warnings:\n");
            for (String w : warnings) sb.append("    ⚠ ").append(w).append("\n");
            sb.append("\n");
        }
        if (optimizations.isEmpty() && warnings.isEmpty()) {
            sb.append("  No issues detected:\n");
            sb.append("    ✔ No constant conditions.\n");
            sb.append("    ✔ No dead code found.\n");
            sb.append("    ✔ No redundant assignments.\n");
            sb.append("    ✔ No self-comparisons.\n\n");
        }

        // ── Optimized TAC Output ─────────────────────────
        sb.append("  Optimized TAC:\n");
        sb.append("  ").append("─".repeat(34)).append("\n");
        String safeLeft  = left  != null ? left  : "<left?>";
        String safeOp    = op    != null ? op    : "<op?>";
        String safeRight = right != null ? right : "<right?>";
        String safeTgt   = tgt   != null ? tgt   : "<target?>";
        String safeVal   = val   != null ? val   : "<value?>";

        if (alwaysTrue) {
            sb.append("  (Condition always TRUE — simplified)\n");
            sb.append("  ").append(safeTgt).append(" = ").append(safeVal).append("\n");
        } else if (alwaysFalse) {
            sb.append("  (Condition always FALSE — block removed)\n");
            sb.append("  (no code — dead branch eliminated)\n");
        } else {
            sb.append("  t1 = ").append(safeLeft).append(" ").append(safeOp).append(" ").append(safeRight).append("\n");
            sb.append("  if t1 goto L_TRUE\n");
            sb.append("  goto L_FALSE\n");
            sb.append("  L_TRUE: ").append(safeTgt).append(" = ").append(safeVal).append("\n");
            sb.append("  L_FALSE:\n");
            sb.append("  L_END:\n");
        }

        sb.append("\n  ✔ Optimization phase complete.\n");
        return sb.toString();
    }

    private static String getVal(List<Token> tokens, int pos, String fallback) {
        return pos < tokens.size() ? tokens.get(pos).getValue() : fallback;
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
