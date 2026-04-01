// ============================================================
// IntermediateCodeGenerator.java — Phase 4: Three Address Code
// Works for correct AND wrong input — always shows output
// ============================================================
import java.util.*;

public class IntermediateCodeGenerator {

    public static String generate(List<Token> tokens) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Intermediate Code Generation (TAC) ===\n\n");

        if (tokens.isEmpty()) {
            sb.append("  ✘ ERROR: No tokens found. Cannot generate TAC.\n");
            return sb.toString();
        }

        // ── Extract parts with safe fallbacks ───────────────
        String left  = getVal(tokens, 1, "<left?>");
        String op    = getVal(tokens, 2, "<op?>");
        String right = getVal(tokens, 3, "<right?>");
        String tgt   = getVal(tokens, 5, "<target?>");
        String val   = tokens.size() > 7 ? tokens.get(7).getValue() : "<value?>";

        boolean hasErrors = left.contains("?") || op.contains("?") ||
                            right.contains("?") || tgt.contains("?") || val.contains("?");

        if (hasErrors) {
            sb.append("  ⚠ Input is incomplete. Generating partial TAC with placeholders.\n");
            sb.append("  → Fix the input for complete TAC output.\n\n");
        }

        // ── Three Address Code ───────────────────────────────
        sb.append("  Three Address Code (TAC):\n");
        sb.append("  ").append("─".repeat(38)).append("\n");
        sb.append(String.format("  %-4s t1 = %s %s %s\n",    "1.", left, op, right));
        sb.append(String.format("  %-4s if t1 goto L_TRUE\n", "2."));
        sb.append(String.format("  %-4s goto L_FALSE\n",      "3."));
        sb.append(String.format("  %-4s L_TRUE:  %s = %s\n", "4.", tgt, val));
        sb.append(String.format("  %-4s L_FALSE: (skip)\n",   "5."));
        sb.append(String.format("  %-4s L_END:\n",            "6."));

        // ── Quadruple Table ─────────────────────────────────
        sb.append("\n  Quadruple Table  (Op, Arg1, Arg2, Result):\n");
        sb.append("  ").append("─".repeat(50)).append("\n");
        sb.append(String.format("  %-5s  %-8s  %-10s  %-10s  %-10s\n",
            "No.", "Op", "Arg1", "Arg2", "Result"));
        sb.append("  ").append("─".repeat(50)).append("\n");
        sb.append(String.format("  %-5s  %-8s  %-10s  %-10s  %-10s\n",
            "1", op,      left,    right,   "t1"));
        sb.append(String.format("  %-5s  %-8s  %-10s  %-10s  %-10s\n",
            "2", "if",    "t1",    "L_TRUE", "—"));
        sb.append(String.format("  %-5s  %-8s  %-10s  %-10s  %-10s\n",
            "3", "goto",  "L_FALSE","—",    "—"));
        sb.append(String.format("  %-5s  %-8s  %-10s  %-10s  %-10s\n",
            "4", "=",     val,     "—",     tgt));

        // ── Explanation ──────────────────────────────────────
        sb.append("\n  What each line does:\n");
        sb.append("  ").append("─".repeat(38)).append("\n");
        sb.append("  1. Evaluate condition → store result in temp t1\n");
        sb.append("  2. If t1 is true  → jump to L_TRUE (run assignment)\n");
        sb.append("  3. If t1 is false → jump to L_FALSE (skip assignment)\n");
        sb.append("  4. L_TRUE: Assign " + val + " to " + tgt + "\n");
        sb.append("  5. L_FALSE: skip the assignment block\n");
        sb.append("  6. L_END: program continues here\n");

        sb.append("\n  Temporary variables used: t1\n");
        sb.append("  Labels used: L_TRUE, L_FALSE, L_END\n");

        if (!hasErrors) {
            sb.append("\n  ✔ TAC generation complete.\n");
        } else {
            sb.append("\n  ⚠ TAC generated with placeholders. Fix input for complete output.\n");
        }
        return sb.toString();
    }

    private static String getVal(List<Token> tokens, int pos, String fallback) {
        return pos < tokens.size() ? tokens.get(pos).getValue() : fallback;
    }
}
