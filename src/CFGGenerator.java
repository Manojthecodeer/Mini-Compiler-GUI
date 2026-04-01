// ============================================================
// CFGGenerator.java — Control Flow Graph (text-based)
// Works for correct AND wrong input — always shows output
// ============================================================
import java.util.*;

public class CFGGenerator {

    public static String generate(List<Token> tokens) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Control Flow Graph (CFG) ===\n\n");

        if (tokens.isEmpty()) {
            sb.append("  ✘ ERROR: No tokens found. Cannot generate CFG.\n");
            return sb.toString();
        }

        String left  = getVal(tokens, 1, "?");
        String op    = getVal(tokens, 2, "?");
        String right = getVal(tokens, 3, "?");
        String tgt   = getVal(tokens, 5, "?");
        String val   = tokens.size() > 7 ? tokens.get(7).getValue() : "?";

        boolean incomplete = left.equals("?") || op.equals("?") || right.equals("?")
                          || tgt.equals("?") || val.equals("?");
        if (incomplete) {
            sb.append("  ⚠ Input is incomplete. CFG shown with placeholders.\n\n");
        }

        // ── Draw CFG ─────────────────────────────────────────
        sb.append("  ┌──────────────────────────────┐\n");
        sb.append("  │           ENTRY              │\n");
        sb.append("  └──────────────┬───────────────┘\n");
        sb.append("                 │\n");
        sb.append("                 ▼\n");
        sb.append("  ┌──────────────────────────────┐\n");
        sb.append("  │    BLOCK B1  (Condition)     │\n");
        sb.append("  │                              │\n");
        sb.append(String.format("  │  t1 = %-4s %-3s %-4s          │\n",
            trunc(left, 4), trunc(op, 3), trunc(right, 4)));
        sb.append("  │  if t1  →  TRUE              │\n");
        sb.append("  │  else   →  FALSE             │\n");
        sb.append("  └──────┬───────────────┬───────┘\n");
        sb.append("         │               │\n");
        sb.append("       [TRUE]         [FALSE]\n");
        sb.append("         │               │\n");
        sb.append("         ▼               ▼\n");
        sb.append("  ┌───────────┐   ┌───────────┐\n");
        sb.append("  │ BLOCK B2  │   │ BLOCK B3  │\n");
        sb.append("  │(T-Branch) │   │(F-Branch) │\n");
        sb.append("  │           │   │           │\n");
        sb.append(String.format("  │%s=%-5s │   │  (skip)   │\n",
            padLeft(tgt, 3), padRight(val, 5)));
        sb.append("  └─────┬─────┘   └─────┬─────┘\n");
        sb.append("        │               │\n");
        sb.append("        └───────┬───────┘\n");
        sb.append("                │\n");
        sb.append("                ▼\n");
        sb.append("  ┌──────────────────────────────┐\n");
        sb.append("  │            EXIT              │\n");
        sb.append("  └──────────────────────────────┘\n\n");

        // ── Block Descriptions ───────────────────────────────
        sb.append("  Basic Block Summary:\n");
        sb.append("  ").append("─".repeat(44)).append("\n");
        sb.append(String.format("  %-10s : Entry point. Program starts here.\n", "ENTRY"));
        sb.append(String.format("  %-10s : Evaluates condition: %s %s %s\n", "B1 (Cond)", left, op, right));
        sb.append(String.format("  %-10s : TRUE  branch — executes: %s = %s\n", "B2 (True)", tgt, val));
        sb.append(String.format("  %-10s : FALSE branch — skips assignment.\n", "B3 (False)"));
        sb.append(String.format("  %-10s : Program ends here.\n", "EXIT"));

        sb.append("\n  CFG Edges (control flow paths):\n");
        sb.append("  ").append("─".repeat(44)).append("\n");
        sb.append("  ENTRY  ──►  B1\n");
        sb.append("  B1     ──►  B2   (when condition is TRUE)\n");
        sb.append("  B1     ──►  B3   (when condition is FALSE)\n");
        sb.append("  B2     ──►  EXIT\n");
        sb.append("  B3     ──►  EXIT\n");

        sb.append("\n  Graph Properties:\n");
        sb.append("  Nodes (Basic Blocks) : 4  (ENTRY, B1, B2, B3, EXIT)\n");
        sb.append("  Edges (Jumps)        : 5\n");
        sb.append("  Branches             : 1  (at B1)\n");
        sb.append("  Join Points          : 1  (at EXIT)\n");

        if (!incomplete) {
            sb.append("\n  ✔ CFG generation complete.\n");
        } else {
            sb.append("\n  ⚠ CFG generated with placeholders.\n");
        }
        return sb.toString();
    }

    private static String getVal(List<Token> tokens, int pos, String fb) {
        return pos < tokens.size() ? tokens.get(pos).getValue() : fb;
    }
    private static String trunc(String s, int n) {
        return s.length() > n ? s.substring(0, n) : s;
    }
    private static String padLeft(String s, int n) {
        return String.format("%" + n + "s", s.length() > n ? s.substring(0,n) : s);
    }
    private static String padRight(String s, int n) {
        return String.format("%-" + n + "s", s.length() > n ? s.substring(0,n) : s);
    }
}
