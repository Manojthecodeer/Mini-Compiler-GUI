// ============================================================
// MiniCompilerGUI.java - Main GUI Class
// Features:
//   • Fully responsive — fonts & layout scale on resize/maximize
//   • Ctrl + Mouse Wheel = zoom output text
//   • Zoom toolbar: [−] [%] [+] [Reset]
//   • ComponentListener reflows all fonts on window resize
// ============================================================
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;

public class MiniCompilerGUI extends JFrame {

    // ── Colors ───────────────────────────────────────────────
    private static final Color BG_DARK      = new Color(18,  20,  30);
    private static final Color BG_PANEL     = new Color(26,  29,  44);
    private static final Color BG_INPUT     = new Color(35,  39,  58);
    private static final Color BG_OUTPUT    = new Color(12,  14,  22);
    private static final Color ACCENT_BLUE  = new Color(64, 156, 255);
    private static final Color ACCENT_CYAN  = new Color(0,  220, 200);
    private static final Color ACCENT_GREEN = new Color(80, 220, 120);
    private static final Color ACCENT_AMBER = new Color(255, 190,  50);
    private static final Color TEXT_MAIN    = new Color(220, 225, 240);
    private static final Color TEXT_DIM     = new Color(140, 148, 170);
    private static final Color TEXT_OUTPUT  = new Color(180, 240, 200);

    // ── Font scaling ─────────────────────────────────────────
    // BASE_WIDTH is the "design width" — at this width fonts are BASE_* size
    private static final int BASE_WIDTH       = 980;
    private static final float BASE_TITLE     = 22f;
    private static final float BASE_LABEL     = 13f;
    private static final float BASE_INPUT     = 15f;
    private static final float BASE_BTN       = 12f;
    private static final float BASE_OUTPUT    = 13f;
    private static final float BASE_STATUS    = 11f;
    private static final float BASE_SUB       = 12f;
    private static final float MIN_SCALE      = 0.65f;
    private static final float MAX_SCALE      = 2.2f;

    // ── Zoom (output area only) ───────────────────────────────
    private float zoomScale   = 1.0f;
    private static final float ZOOM_STEP = 0.1f;
    private static final float ZOOM_MIN  = 0.5f;
    private static final float ZOOM_MAX  = 3.0f;
    private JLabel zoomLabel;

    // ── All font-bearing components (for responsive resize) ───
    private JLabel   titleLabel;
    private JLabel   subLabel;
    private JLabel   srcLabel;
    private JLabel   outputLabel;
    private JLabel   statusLabel;
    private JLabel   infoLabel;
    private JTextField inputField;
    private JTextArea  outputArea;
    private JComboBox<String> sampleBox;
    private java.util.List<JButton> allButtons = new ArrayList<>();

    // Phase button config
    private static final Object[][] PHASES = {
        { "Lexical",     new Color( 80, 160, 255) },
        { "Syntax",      new Color(100, 210, 140) },
        { "Semantic",    new Color(255, 190,  60) },
        { "TAC",         new Color(220, 100, 255) },
        { "Optimize",    new Color(255, 120,  90) },
        { "Target Code", new Color( 60, 210, 200) },
        { "CFG",         new Color(255, 160,  80) },
        { "AST",         new Color(180, 100, 255) },
    };

    private static final String[] SAMPLES = {
        "if a > b then x = a",
        "if x >= 10 then y = x",
        "if a > a then x = 5",
        "if 5 > 3 then z = 1",
        "if count != 0 then result = count",
        "if 2 < 1 then flag = 0",
    };

    // ─────────────────────────────────────────────────────────
    public MiniCompilerGUI() {
        super("Mini Compiler — 6-Phase Compiler with GUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(980, 720);
        setMinimumSize(new Dimension(640, 460));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout(0, 0));

        add(buildHeader(),    BorderLayout.NORTH);
        add(buildCenter(),    BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        // ── Responsive: reflow fonts whenever window is resized ──
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                reflowFonts();
            }
        });

        setVisible(true);
        reflowFonts(); // initial pass
    }

    // ── Calculate scale factor from current window width ─────
    private float windowScale() {
        int w = Math.max(getWidth(), 1);
        float s = (float) w / BASE_WIDTH;
        return Math.max(MIN_SCALE, Math.min(MAX_SCALE, s));
    }

    // ── Apply scaled fonts to every component ────────────────
    private void reflowFonts() {
        float s = windowScale();

        setLabelFont(titleLabel,  "Consolas", Font.BOLD,  scale(BASE_TITLE,  s));
        setLabelFont(subLabel,    "Consolas", Font.PLAIN, scale(BASE_SUB,    s));
        setLabelFont(srcLabel,    "Consolas", Font.BOLD,  scale(BASE_LABEL,  s));
        setLabelFont(outputLabel, "Consolas", Font.BOLD,  scale(BASE_LABEL,  s));
        setLabelFont(statusLabel, "Consolas", Font.PLAIN, scale(BASE_STATUS, s));
        setLabelFont(infoLabel,   "Consolas", Font.PLAIN, scale(BASE_STATUS, s));
        setLabelFont(zoomLabel,   "Consolas", Font.BOLD,  scale(BASE_STATUS, s));

        if (inputField != null)
            inputField.setFont(new Font("Consolas", Font.PLAIN, scale(BASE_INPUT, s)));
        if (sampleBox != null)
            sampleBox.setFont(new Font("Consolas", Font.PLAIN, scale(BASE_BTN, s)));

        // Buttons
        for (JButton btn : allButtons)
            btn.setFont(new Font("Consolas", Font.BOLD, scale(BASE_BTN, s)));

        // Output area uses zoom * window scale
        applyOutputFont();

        revalidate();
        repaint();
    }

    // Apply combined window-scale + zoom to output area
    private void applyOutputFont() {
        if (outputArea == null) return;
        float s = windowScale();
        int size = Math.round(BASE_OUTPUT * s * zoomScale);
        size = Math.max(8, Math.min(72, size));
        outputArea.setFont(new Font("Consolas", Font.PLAIN, size));
        if (zoomLabel != null)
            zoomLabel.setText(Math.round(zoomScale * 100) + "%");
    }

    private int scale(float base, float s) {
        return Math.max(8, Math.round(base * s));
    }

    private void setLabelFont(JLabel lbl, String name, int style, int size) {
        if (lbl != null) lbl.setFont(new Font(name, style, size));
    }

    // ── HEADER ───────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_PANEL);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT_BLUE));

        titleLabel = new JLabel("  ⚙ Mini Compiler", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Consolas", Font.BOLD, 22));
        titleLabel.setForeground(ACCENT_BLUE);
        titleLabel.setBorder(new EmptyBorder(14, 16, 14, 0));

        subLabel = new JLabel("Lexical · Syntax · Semantic · TAC · Optimize · Target · CFG · AST  ");
        subLabel.setFont(new Font("Consolas", Font.PLAIN, 12));
        subLabel.setForeground(TEXT_DIM);

        header.add(titleLabel, BorderLayout.WEST);
        header.add(subLabel,   BorderLayout.EAST);
        return header;
    }

    // ── CENTER ───────────────────────────────────────────────
    private JPanel buildCenter() {
        JPanel center = new JPanel(new BorderLayout(0, 0));
        center.setBackground(BG_DARK);
        center.setBorder(new EmptyBorder(14, 14, 8, 14));
        center.add(buildInputPanel(),  BorderLayout.NORTH);
        center.add(buildOutputPanel(), BorderLayout.CENTER);
        return center;
    }

    // ── INPUT PANEL ──────────────────────────────────────────
    private JPanel buildInputPanel() {
        JPanel wrap = new JPanel(new BorderLayout(0, 8));
        wrap.setBackground(BG_DARK);
        wrap.setBorder(new EmptyBorder(0, 0, 10, 0));

        // Row 1: label + field + sample dropdown
        JPanel inputRow = new JPanel(new BorderLayout(8, 0));
        inputRow.setBackground(BG_DARK);

        srcLabel = new JLabel("Source Code: ");
        srcLabel.setFont(new Font("Consolas", Font.BOLD, 13));
        srcLabel.setForeground(ACCENT_CYAN);

        inputField = new JTextField("if a > b then x = a");
        inputField.setFont(new Font("Consolas", Font.PLAIN, 15));
        inputField.setBackground(BG_INPUT);
        inputField.setForeground(TEXT_MAIN);
        inputField.setCaretColor(ACCENT_BLUE);
        inputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT_BLUE.darker(), 1),
            new EmptyBorder(6, 10, 6, 10)
        ));

        sampleBox = new JComboBox<>(SAMPLES);
        sampleBox.setFont(new Font("Consolas", Font.PLAIN, 12));
        sampleBox.setBackground(BG_INPUT);
        sampleBox.setForeground(TEXT_DIM);
        sampleBox.setPreferredSize(new Dimension(220, 34));
        sampleBox.setToolTipText("Load a sample input");
        sampleBox.addActionListener(e -> {
            inputField.setText((String) sampleBox.getSelectedItem());
            setStatus("Sample loaded — click a phase button", ACCENT_CYAN);
        });

        inputRow.add(srcLabel,   BorderLayout.WEST);
        inputRow.add(inputField, BorderLayout.CENTER);
        inputRow.add(sampleBox,  BorderLayout.EAST);

        // Row 2: phase buttons
        JPanel buttonRow = buildButtonRow();

        wrap.add(inputRow,  BorderLayout.NORTH);
        wrap.add(buttonRow, BorderLayout.CENTER);
        return wrap;
    }

    // ── BUTTONS ──────────────────────────────────────────────
    private JPanel buildButtonRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        row.setBackground(BG_DARK);

        for (Object[] phase : PHASES) {
            String label = (String) phase[0];
            Color  color = (Color)  phase[1];
            JButton btn  = makePhaseButton(label, color);
            row.add(btn);
            allButtons.add(btn);
        }

        // Separator
        row.add(Box.createHorizontalStrut(8));

        // Run All
        JButton allBtn = makeUtilButton("▶ Run All", ACCENT_BLUE, new Color(20, 40, 70));
        allBtn.addActionListener(e -> runAllPhases());
        row.add(allBtn);
        allButtons.add(allBtn);

        // Clear
        JButton clearBtn = makeUtilButton("✖ Clear", new Color(255, 120, 100), new Color(60, 30, 30));
        clearBtn.addActionListener(e -> {
            outputArea.setText("");
            setStatus("Output cleared.", TEXT_DIM);
        });
        row.add(clearBtn);
        allButtons.add(clearBtn);

        return row;
    }

    private JButton makePhaseButton(String label, Color accent) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("Consolas", Font.BOLD, 12));
        btn.setBackground(BG_PANEL);
        btn.setForeground(accent);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(accent.darker(), 1),
            new EmptyBorder(5, 12, 5, 12)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(
                    clamp(accent.getRed()   / 2),
                    clamp(accent.getGreen() / 2),
                    clamp(accent.getBlue()  / 2)));
            }
            public void mouseExited(MouseEvent e) { btn.setBackground(BG_PANEL); }
        });
        btn.addActionListener(e -> runPhase(label));
        return btn;
    }

    private JButton makeUtilButton(String label, Color fg, Color bg) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("Consolas", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(fg.darker(), 1),
            new EmptyBorder(5, 14, 5, 14)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ── OUTPUT PANEL ─────────────────────────────────────────
    private JPanel buildOutputPanel() {
        JPanel wrap = new JPanel(new BorderLayout(0, 4));
        wrap.setBackground(BG_DARK);

        outputLabel = new JLabel(" ▼ Compiler Output");
        outputLabel.setFont(new Font("Consolas", Font.BOLD, 12));
        outputLabel.setForeground(TEXT_DIM);

        outputArea = new JTextArea();
        outputArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        outputArea.setBackground(BG_OUTPUT);
        outputArea.setForeground(TEXT_OUTPUT);
        outputArea.setCaretColor(ACCENT_GREEN);
        outputArea.setEditable(false);
        outputArea.setLineWrap(false);
        outputArea.setBorder(new EmptyBorder(10, 12, 10, 12));
        outputArea.setText(welcomeText());

        // ── Ctrl + Scroll Wheel = Zoom ────────────────────
        outputArea.addMouseWheelListener(e -> {
            if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0) {
                if (e.getWheelRotation() < 0) {
                    // Scroll up = zoom in
                    zoomScale = Math.min(ZOOM_MAX, zoomScale + ZOOM_STEP);
                } else {
                    // Scroll down = zoom out
                    zoomScale = Math.max(ZOOM_MIN, zoomScale - ZOOM_STEP);
                }
                applyOutputFont();
                e.consume();
            }
            // If Ctrl not held, let scroll happen normally
        });

        JScrollPane scroll = new JScrollPane(outputArea);
        scroll.setBackground(BG_OUTPUT);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(50, 60, 90), 1));
        scroll.getViewport().setBackground(BG_OUTPUT);
        // Smooth scrolling
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        // ── Zoom toolbar (top right of output panel) ──────
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(BG_DARK);
        topBar.add(outputLabel, BorderLayout.WEST);
        topBar.add(buildZoomToolbar(), BorderLayout.EAST);

        wrap.add(topBar, BorderLayout.NORTH);
        wrap.add(scroll, BorderLayout.CENTER);
        return wrap;
    }

    // ── ZOOM TOOLBAR ─────────────────────────────────────────
    private JPanel buildZoomToolbar() {
        JPanel zbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        zbar.setBackground(BG_DARK);

        JLabel hint = new JLabel("Ctrl+Scroll to zoom  |  ");
        hint.setFont(new Font("Consolas", Font.PLAIN, 10));
        hint.setForeground(TEXT_DIM);

        JButton zOut = makeZoomBtn("−");
        zOut.setToolTipText("Zoom out (Ctrl + Scroll Down)");
        zOut.addActionListener(e -> {
            zoomScale = Math.max(ZOOM_MIN, zoomScale - ZOOM_STEP);
            applyOutputFont();
        });

        zoomLabel = new JLabel("100%");
        zoomLabel.setFont(new Font("Consolas", Font.BOLD, 11));
        zoomLabel.setForeground(ACCENT_CYAN);
        zoomLabel.setBorder(new EmptyBorder(0, 6, 0, 6));
        zoomLabel.setPreferredSize(new Dimension(46, 20));
        zoomLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JButton zIn = makeZoomBtn("+");
        zIn.setToolTipText("Zoom in (Ctrl + Scroll Up)");
        zIn.addActionListener(e -> {
            zoomScale = Math.min(ZOOM_MAX, zoomScale + ZOOM_STEP);
            applyOutputFont();
        });

        JButton zReset = makeZoomBtn("↺");
        zReset.setToolTipText("Reset zoom to 100%");
        zReset.addActionListener(e -> {
            zoomScale = 1.0f;
            applyOutputFont();
        });

        zbar.add(hint);
        zbar.add(zOut);
        zbar.add(zoomLabel);
        zbar.add(zIn);
        zbar.add(zReset);
        return zbar;
    }

    private JButton makeZoomBtn(String txt) {
        JButton btn = new JButton(txt);
        btn.setFont(new Font("Consolas", Font.BOLD, 13));
        btn.setBackground(BG_PANEL);
        btn.setForeground(ACCENT_CYAN);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(40, 60, 90), 1),
            new EmptyBorder(1, 7, 1, 7)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(30, 50, 80)); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(BG_PANEL); }
        });
        return btn;
    }

    // ── STATUS BAR ───────────────────────────────────────────
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(14, 16, 26));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(40, 50, 80)));

        statusLabel = new JLabel("  Ready. Resize window to scale UI. Ctrl+Scroll on output to zoom.");
        statusLabel.setFont(new Font("Consolas", Font.PLAIN, 11));
        statusLabel.setForeground(TEXT_DIM);
        statusLabel.setBorder(new EmptyBorder(4, 8, 4, 8));

        infoLabel = new JLabel("Mini Compiler — 6 Phases  ");
        infoLabel.setFont(new Font("Consolas", Font.PLAIN, 11));
        infoLabel.setForeground(new Color(70, 80, 110));

        bar.add(statusLabel, BorderLayout.WEST);
        bar.add(infoLabel,   BorderLayout.EAST);
        return bar;
    }

    // ── PHASE DISPATCHER ─────────────────────────────────────
    private void runPhase(String phase) {
        String input = inputField.getText().trim();
        if (input.isEmpty()) {
            setOutput("  ⚠ Please enter a statement first.\n");
            setStatus("No input provided.", ACCENT_AMBER);
            return;
        }
        List<Token> tokens = LexicalAnalyzer.tokenize(input);
        String result;
        switch (phase) {
            case "Lexical":
                result = header("Lexical Analysis") + LexicalAnalyzer.buildOutput(tokens); break;
            case "Syntax":
                result = header("Syntax Analysis") + SyntaxAnalyzer.analyze(tokens); break;
            case "Semantic":
                result = header("Semantic Analysis") + SemanticAnalyzer.analyze(tokens); break;
            case "TAC":
                result = header("Intermediate Code Generation (TAC)") + IntermediateCodeGenerator.generate(tokens); break;
            case "Optimize":
                result = header("Code Optimization") + CodeOptimizer.optimize(tokens); break;
            case "Target Code":
                result = header("Target Code Generation") + TargetCodeGenerator.generate(tokens); break;
            case "CFG":
                result = header("Control Flow Graph") + CFGGenerator.generate(tokens); break;
            case "AST":
                result = header("Abstract Syntax Tree") + ASTGenerator.generate(tokens); break;
            default:
                result = "  Unknown phase: " + phase + "\n";
        }
        setOutput(result);
        setStatus("Phase [" + phase + "] executed  |  Input: " + input, ACCENT_GREEN);
    }

    private void runAllPhases() {
        String input = inputField.getText().trim();
        if (input.isEmpty()) {
            setOutput("  ⚠ Please enter a statement first.\n");
            return;
        }
        List<Token> tokens = LexicalAnalyzer.tokenize(input);
        StringBuilder sb = new StringBuilder();
        sb.append("  ╔══════════════════════════════════════════╗\n");
        sb.append("  ║         FULL COMPILER PIPELINE           ║\n");
        sb.append("  ║  Input: ").append(padRight(input, 34)).append("║\n");
        sb.append("  ╚══════════════════════════════════════════╝\n\n");
        sb.append(header("Phase 1: Lexical Analysis"));
        sb.append(LexicalAnalyzer.buildOutput(tokens)).append("\n");
        sb.append(header("Phase 2: Syntax Analysis"));
        sb.append(SyntaxAnalyzer.analyze(tokens)).append("\n");
        sb.append(header("Phase 3: Semantic Analysis"));
        sb.append(SemanticAnalyzer.analyze(tokens)).append("\n");
        sb.append(header("Phase 4: Intermediate Code Generation (TAC)"));
        sb.append(IntermediateCodeGenerator.generate(tokens)).append("\n");
        sb.append(header("Phase 5: Code Optimization"));
        sb.append(CodeOptimizer.optimize(tokens)).append("\n");
        sb.append(header("Phase 6: Target Code Generation"));
        sb.append(TargetCodeGenerator.generate(tokens)).append("\n");
        sb.append(header("Control Flow Graph (CFG)"));
        sb.append(CFGGenerator.generate(tokens)).append("\n");
        sb.append(header("Abstract Syntax Tree (AST)"));
        sb.append(ASTGenerator.generate(tokens)).append("\n");
        sb.append("  ════════════════════════════════════════════\n");
        sb.append("              ✔ ALL PHASES COMPLETE\n");
        sb.append("  ════════════════════════════════════════════\n");
        setOutput(sb.toString());
        setStatus("All phases complete  |  Input: " + input, ACCENT_CYAN);
    }

    // ── Helpers ──────────────────────────────────────────────
    private String header(String title) {
        String bar = "─".repeat(44);
        return "\n  ┌" + bar + "┐\n" +
               "  │  " + padRight(title, 43) + "│\n" +
               "  └" + bar + "┘\n\n";
    }

    private String padRight(String s, int n) {
        if (s.length() >= n) return s.substring(0, n);
        return s + " ".repeat(n - s.length());
    }

    private void setOutput(String text) {
        outputArea.setText(text);
        outputArea.setCaretPosition(0);
    }

    private void setStatus(String msg, Color color) {
        statusLabel.setText("  " + msg);
        statusLabel.setForeground(color);
    }

    private int clamp(int v) { return Math.max(0, Math.min(255, v)); }

    private String welcomeText() {
        return
            "  ═══════════════════════════════════════════\n" +
            "           ⚙  MINI COMPILER READY  ⚙\n" +
            "  ═══════════════════════════════════════════\n\n" +
            "  Enter a statement and click a phase button.\n\n" +
            "  Supported syntax:\n" +
            "    if <var> <op> <var|num> then <var> = <var|num>\n\n" +
            "  Example inputs:\n" +
            "    if a > b then x = a\n" +
            "    if x >= 10 then y = x\n" +
            "    if 5 > 3 then z = 1\n\n" +
            "  Operators: >  <  >=  <=  ==  !=\n\n" +
            "  ─────────────────────────────────────────────\n" +
            "  RESPONSIVE UI:\n" +
            "    • Resize or maximize the window —\n" +
            "      all fonts and layout scale automatically.\n\n" +
            "  ZOOM OUTPUT TEXT:\n" +
            "    • Hold Ctrl + scroll mouse wheel up/down\n" +
            "    • Or use the [−] [+] [↺] buttons (top right)\n" +
            "  ─────────────────────────────────────────────\n";
    }

    // ── MAIN ─────────────────────────────────────────────────
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(MiniCompilerGUI::new);
    }
}