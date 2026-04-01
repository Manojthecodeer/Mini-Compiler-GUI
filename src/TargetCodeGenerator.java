// ============================================================
// TargetCodeGenerator.java — Phase 6: Target Code (Assembly)
// Works for correct AND wrong input — always shows output
// ============================================================
import java.util.*;

public class TargetCodeGenerator {

    public static String generate(List<Token> tokens) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Target Code Generation ===\n\n");

        if (tokens.isEmpty()) {
            sb.append("  ✘ ERROR: No tokens found. Cannot generate target code.\n");
            return sb.toString();
        }

        String left  = getVal(tokens, 1, "?LEFT");
        String op    = getVal(tokens, 2, "?OP");
        String right = getVal(tokens, 3, "?RIGHT");
        String tgt   = getVal(tokens, 5, "?TGT");
        String val   = tokens.size() > 7 ? tokens.get(7).getValue() : "?VAL";

        boolean hasPlaceholders = left.startsWith("?") || op.startsWith("?") ||
                                   right.startsWith("?") || tgt.startsWith("?") || val.startsWith("?");
        if (hasPlaceholders) {
            sb.append("  ⚠ Input is incomplete. Generating partial assembly with placeholders.\n");
            sb.append("  → Fix the input for complete target code.\n\n");
        }

        // Input summary
        sb.append("  ; Input: if ").append(left).append(" ").append(op).append(" ")
          .append(right).append(" then ").append(tgt).append(" = ").append(val).append("\n\n");

        // ── Assembly Code ────────────────────────────────────
        sb.append("  ; ─────── Load Section ────────────────────────\n");
        sb.append(String.format("  %-8s R1, [%s]%n", "MOV", left));
        sb.append(String.format("  %-8s R2, [%s]%n", "MOV", right));

        sb.append("\n  ; ─────── Compare Section ─────────────────────\n");
        sb.append(String.format("  %-8s R1, R2%n", "CMP"));

        sb.append("\n  ; ─────── Jump Section ────────────────────────\n");
        sb.append(String.format("  %-8s %s%n", jumpInstr(op), "L_TRUE"));
        sb.append(String.format("  %-8s %s%n", "JMP", "L_FALSE"));

        sb.append("\n  ; ─────── True Branch ─────────────────────────\n");
        sb.append(String.format("  %-8s%n", "L_TRUE:"));
        sb.append(String.format("  %-8s R3, [%s]%n", "MOV", val));
        sb.append(String.format("  %-8s [%s], R3%n", "MOV", tgt));
        sb.append(String.format("  %-8s %s%n", "JMP", "L_END"));

        sb.append("\n  ; ─────── False Branch ────────────────────────\n");
        sb.append(String.format("  %-8s%n", "L_FALSE:"));
        sb.append(String.format("  %-8s %s%n", "NOP", "; skip assignment"));

        sb.append("\n  ; ─────── End ─────────────────────────────────\n");
        sb.append(String.format("  %-8s%n", "L_END:"));
        sb.append(String.format("  %-8s%n", "HALT"));

        // ── Register + Label Info ────────────────────────────
        sb.append("\n  Register Usage:\n");
        sb.append("  ").append("─".repeat(36)).append("\n");
        sb.append(String.format("  %-6s : holds value of %s (left operand)\n", "R1", left));
        sb.append(String.format("  %-6s : holds value of %s (right operand)\n", "R2", right));
        sb.append(String.format("  %-6s : holds value of %s (to assign)\n", "R3", val));

        sb.append("\n  Instruction Legend:\n");
        sb.append("  ").append("─".repeat(36)).append("\n");
        sb.append("  MOV   : Move/load a value into register or memory\n");
        sb.append("  CMP   : Compare two registers (sets CPU flags)\n");
        sb.append("  ").append(jumpInstr(op)).append("     : Jump if condition '").append(op).append("' is true\n");
        sb.append("  JMP   : Unconditional jump\n");
        sb.append("  NOP   : No operation (placeholder for skipped code)\n");
        sb.append("  HALT  : Stop program execution\n");

        sb.append("\n  Label Map:\n");
        sb.append("  ").append("─".repeat(36)).append("\n");
        sb.append("  L_TRUE  : jump here when condition (").append(left).append(" ").append(op)
          .append(" ").append(right).append(") is TRUE\n");
        sb.append("  L_FALSE : jump here when condition is FALSE\n");
        sb.append("  L_END   : program terminates here\n");

        if (!hasPlaceholders) {
            sb.append("\n  ✔ Target code generation complete.\n");
        } else {
            sb.append("\n  ⚠ Partial target code — fix input for full output.\n");
        }
        return sb.toString();
    }

    private static String jumpInstr(String op) {
        switch (op) {
            case ">":  return "JG ";
            case "<":  return "JL ";
            case ">=": return "JGE";
            case "<=": return "JLE";
            case "==": return "JE ";
            case "!=": return "JNE";
            default:   return "JMP";
        }
    }

    private static String getVal(List<Token> tokens, int pos, String fallback) {
        return pos < tokens.size() ? tokens.get(pos).getValue() : fallback;
    }
}
