# Mini Compiler Project — Java Swing GUI
### 6-Phase Compiler for IF Statements

---

## 📁 Project Structure

```
MiniCompiler/
├── MiniCompilerGUI.java          ← Main GUI (run this)
├── Token.java                    ← Token data class
├── LexicalAnalyzer.java          ← Phase 1: Tokenization
├── SyntaxAnalyzer.java           ← Phase 2: Grammar check
├── SemanticAnalyzer.java         ← Phase 3: Meaning check
├── IntermediateCodeGenerator.java← Phase 4: TAC
├── CodeOptimizer.java            ← Phase 5: Optimization
├── TargetCodeGenerator.java      ← Phase 6: Assembly
├── CFGGenerator.java             ← Bonus: Control Flow Graph
├── ASTGenerator.java             ← Bonus: AST view
└── README.md                     ← This file
```

---

## 🚀 How to Run (VS Code)

### Prerequisites
- Java JDK 11+ installed
- VS Code with "Extension Pack for Java" installed

### Option A — VS Code
1. Open VS Code → File → Open Folder → select `MiniCompiler/`
2. Open `MiniCompilerGUI.java`
3. Click the **▶ Run** button (top right), or press `F5`

### Option B — Terminal / Command Prompt
```bash
# Step 1: Navigate into the project folder
cd MiniCompiler

# Step 2: Compile all files
javac *.java

# Step 3: Run the GUI
java MiniCompilerGUI
```

### Option C — Single command
```bash
cd MiniCompiler && javac *.java && java MiniCompilerGUI
```

---

## 🎮 How to Use the GUI

1. **Type** your IF statement in the text field at the top
   (or pick one from the **dropdown** on the right)
2. **Click any phase button** to run ONLY that phase
3. **Click ▶ Run All Phases** to run all 6 phases at once
4. **Click ✖ Clear** to reset the output area

---

## ✅ Supported Syntax

```
if <variable> <operator> <variable|number> then <variable> = <variable|number>
```

| Component  | Examples               |
|------------|------------------------|
| variable   | a, b, x, count, result |
| operator   | >  <  >=  <=  ==  !=   |
| number     | 5, 10, 3.14            |

---

## 📋 Sample Inputs & Outputs

---

### Sample 1: Normal variable comparison
**Input:** `if a > b then x = a`

**Lexical Output:**
```
No.    Type          Value
----------------------------------
1      KEYWORD       "if"
2      IDENTIFIER    "a"
3      OPERATOR      ">"
4      IDENTIFIER    "b"
5      KEYWORD       "then"
6      IDENTIFIER    "x"
7      OPERATOR      "="
8      IDENTIFIER    "a"
✔ Tokenization successful.
```

**Syntax Output:**
```
✔ SYNTAX VALID
IF-STMT
├─ CONDITION
│   ├─ LEFT  : a
│   ├─ OP    : >
│   └─ RIGHT : b
└─ ASSIGNMENT
    ├─ TARGET : x
    └─ VALUE  : a
```

**TAC Output:**
```
1.   t1 = a > b
2.   if t1 goto L1
3.   goto L2
4.   L1: x = a
5.   L2: (end)
```

**Target Code:**
```
MOV   R1, [a]
MOV   R2, [b]
CMP   R1, R2
JG    L_TRUE
JMP   L_END
L_TRUE:
MOV   R3, [a]
MOV   [x], R3
L_END:
HALT
```

---

### Sample 2: Always-true detection
**Input:** `if a >= a then x = 5`

**Semantic Output:**
```
⚠ ALWAYS TRUE: 'a >= a' is always true.
```
**Optimizer Output:**
```
⚠ ALWAYS TRUE: 'a >= a' → simplified to:
   x = 5
```

---

### Sample 3: Always-false detection
**Input:** `if a > a then x = 5`

**Optimizer Output:**
```
⚠ ALWAYS FALSE: Dead code — assignment never executes.
```

---

### Sample 4: Constant folding
**Input:** `if 5 > 3 then z = 1`

**Semantic Output:**
```
ℹ Constant condition: 5 > 3 → ALWAYS TRUE
```
**Optimizer Output:**
```
✔ CONSTANT FOLDING: 5 > 3 = true (always)
→ False branch is DEAD CODE — can be removed.
```

---

## 📚 Phase-by-Phase Explanation (for Viva)

### Phase 1 — Lexical Analysis
**What it does:** Reads the input character by character and groups them into tokens (keywords, identifiers, operators, numbers).
**Output:** A list of tokens with their types.

### Phase 2 — Syntax Analysis
**What it does:** Checks if the token sequence follows the correct grammar rule.  
Grammar: `IF id relop id/num THEN id = id/num`  
**Output:** Valid or error with position-specific messages + parse tree.

### Phase 3 — Semantic Analysis
**What it does:** Checks for meaning:
- Builds a symbol table of variables
- Detects always-true/false self-comparisons (a > a)
- Checks type compatibility in assignment
**Output:** Symbol table + warnings/info.

### Phase 4 — TAC (Intermediate Code)
**What it does:** Converts the IF statement into Three Address Code — a simplified, machine-independent form.  
**Format:** `t1 = left op right`, `if t1 goto L1`, `goto L2`, etc.

### Phase 5 — Code Optimization
**What it does:** Looks for improvements:
- Constant folding (5 > 3 → always true)
- Dead code elimination (unreachable branches)
- Redundant assignment removal
**Output:** Optimized TAC with warnings.

### Phase 6 — Target Code
**What it does:** Translates TAC into pseudo x86 assembly instructions (MOV, CMP, JG/JL/JE, etc.)  
**Output:** Register-level assembly code.

### CFG — Control Flow Graph
Shows how execution flows between basic blocks (entry → condition → true/false branch → exit).

### AST — Abstract Syntax Tree
Shows the hierarchical tree structure of the parsed program (nodes for if-stmt, condition, binary-expr, assign-stmt, etc.)

---

## 🏆 Innovation Features
- ✅ Always-true/false condition detection
- ✅ Constant folding with dead code warning
- ✅ Self-comparison contradiction detection
- ✅ Type mismatch warning
- ✅ Control Flow Graph (text-based)
- ✅ AST with node-type legend
- ✅ Dark-themed professional GUI
- ✅ Sample input dropdown for quick demos
- ✅ Run All Phases button
