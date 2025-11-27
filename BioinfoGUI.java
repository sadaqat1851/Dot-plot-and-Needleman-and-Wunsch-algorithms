import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Simple, clean Swing GUI to run Dot-plot and Needleman–Wunsch on two sequences.
 * Designed to be easy to use and readable for students learning these algorithms.
 */
public class BioinfoGUI extends JFrame {

    private final JTextArea seq1Field;
    private final JTextArea seq2Field;
    private final JTextArea outputArea;

    public BioinfoGUI() {
        setTitle("Dot-plot & Needleman–Wunsch — Visual Learner");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);

        // main layout - split left controls / right results
        setLayout(new BorderLayout(8, 8));

        // left panel: inputs
        JPanel left = new JPanel(new BorderLayout(8, 8));
        left.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 4));

        JPanel inputs = new JPanel(new GridLayout(2, 1, 6, 6));

        seq1Field = createInputArea("TTTTGGGCGATAGCTAAAGCTC");
        seq2Field = createInputArea("ATTGGGCGGTAGCTTAAGGTC");

        inputs.add(wrapWithLabel("Sequence 1 (A/C/G/T)", seq1Field));
        inputs.add(wrapWithLabel("Sequence 2 (A/C/G/T)", seq2Field));

        left.add(inputs, BorderLayout.CENTER);

        // buttons panel
        JPanel controls = new JPanel(new GridLayout(3, 1, 6, 6));
        JButton dotBtn = new JButton("Generate Dot-plot");
        JButton alignBtn = new JButton("Compute Alignment");
        JButton clearBtn = new JButton("Clear Output");
        controls.add(dotBtn);
        controls.add(alignBtn);
        controls.add(clearBtn);

        left.add(controls, BorderLayout.SOUTH);

        add(left, BorderLayout.WEST);

        // RIGHT: results pane with tabs (visual and text)
        JTabbedPane rightTabs = new JTabbedPane();

        // Visual pane (for colored dot-plot and alignment)
        JPanel visualPanel = new JPanel(new BorderLayout(6, 6));
        visualPanel.setBorder(BorderFactory.createEmptyBorder(8, 4, 8, 8));

        // Dot-plot preview (scrollable)
        JPanel dotPlotContainer = new JPanel(new BorderLayout());
        dotPlotContainer.setBorder(BorderFactory.createTitledBorder("Dot-plot (visual)"));
        // will fill later

        // alignment preview
        JPanel alignContainer = new JPanel(new BorderLayout());
        alignContainer.setBorder(BorderFactory.createTitledBorder("Alignment (visual)"));

        // Put both visual parts in a split pane
        JSplitPane visualSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        visualSplit.setTopComponent(dotPlotContainer);

        // Alignment area center
        JPanel alignCenter = new JPanel(new BorderLayout());
        alignCenter.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        alignContainer.add(alignCenter, BorderLayout.CENTER);

        visualSplit.setBottomComponent(alignContainer);
        visualSplit.setResizeWeight(0.6);

        visualPanel.add(visualSplit, BorderLayout.CENTER);

        rightTabs.addTab("Visual", visualPanel);

        // Text output tab
        outputArea = new JTextArea();
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        outputArea.setEditable(false);
        outputArea.setMargin(new Insets(8, 8, 8, 8));
        JScrollPane textScroll = new JScrollPane(outputArea);
        textScroll.setBorder(BorderFactory.createTitledBorder("Text Output"));
        rightTabs.addTab("Text", textScroll);

        add(rightTabs, BorderLayout.CENTER);

        // footer: small help
        JLabel footer = new JLabel("Tip: click Generate Dot-plot or Compute Alignment. Matches are highlighted in green.");
        footer.setBorder(BorderFactory.createEmptyBorder(6, 8, 8, 8));
        add(footer, BorderLayout.SOUTH);

        // actions
        dotBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                generateDotPlot();
                // also populate visual dotPlot
                displayVisualDotPlot();
            }
        });

        alignBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                computeAlignment();
                displayVisualAlignment();
            }
        });

        clearBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                outputArea.setText("");
            }
        });

    }

    private JTextArea createInputArea(String sample) {
        JTextArea t = new JTextArea(sample);
        t.setLineWrap(true);
        t.setRows(2);
        t.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        return t;
    }

    private JPanel wrapWithLabel(String labelText, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(4, 4));
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        p.add(lbl, BorderLayout.NORTH);
        p.add(new JScrollPane(comp), BorderLayout.CENTER);
        return p;
    }

    private void generateDotPlot() {
        String s1 = seq1Field.getText().trim().toUpperCase();
        String s2 = seq2Field.getText().trim().toUpperCase();

        if (s1.isEmpty() || s2.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Both sequences must be provided.", "Input required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        DotPlot dp = new DotPlot(s1, s2);
        char[][] grid = dp.createDotPlot();
        String out = "===== DOT PLOT =====\n" + dp.dotPlotToString(grid);
        outputArea.append(out + "\n");
        // store last grid for visual use
        lastDotGrid = grid;
        lastSeq1 = s1;
        lastSeq2 = s2;
    }

    // stored values for visual rendering
    private char[][] lastDotGrid = null;
    private String lastSeq1 = null;
    private String lastSeq2 = null;

    private JPanel currentDotPanel = null;
    private JPanel currentAlignPanel = null;

    private void displayVisualDotPlot() {
        if (lastDotGrid == null) return;

        // get the visual split pane / dotPlotContainer nested earlier
        Container root = getContentPane();
        JTabbedPane tp = null;
        for (Component c : root.getComponents()) if (c instanceof JTabbedPane) tp = (JTabbedPane) c;
        if (tp == null) return;

        JPanel visualPanel = (JPanel) tp.getComponentAt(0);
        // visualPanel contains visualSplit in component 0
        JSplitPane split = null;
        for (Component c : visualPanel.getComponents()) if (c instanceof JSplitPane) split = (JSplitPane) c;
        if (split == null) return;

        JPanel dotPlotContainer = (JPanel) split.getTopComponent();

        if (currentDotPanel != null) dotPlotContainer.remove(currentDotPanel);

        JPanel grid = new JPanel();
        int r = lastDotGrid.length;
        int c = lastDotGrid[0].length;
        grid.setLayout(new GridLayout(r + 1, c + 1, 1, 1));
        grid.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        grid.add(new JLabel(" "));
        for (int j = 0; j < c; j++) {
            JLabel h = new JLabel(String.valueOf(lastSeq2.charAt(j)), SwingConstants.CENTER);
            h.setOpaque(true);
            h.setBackground(new Color(240, 240, 240));
            grid.add(h);
        }

        for (int i = 0; i < r; i++) {
            JLabel rowH = new JLabel(String.valueOf(lastSeq1.charAt(i)), SwingConstants.CENTER);
            rowH.setOpaque(true);
            rowH.setBackground(new Color(240, 240, 240));
            grid.add(rowH);

            for (int j = 0; j < c; j++) {
                JLabel cell = new JLabel("", SwingConstants.CENTER);
                cell.setOpaque(true);
                cell.setBorder(BorderFactory.createLineBorder(new Color(230,230,230)));
                if (lastDotGrid[i][j] == '*') {
                    cell.setBackground(new Color(72, 179, 92)); // green
                    cell.setText("*");
                    cell.setForeground(Color.white);
                } else {
                    cell.setBackground(new Color(250, 250, 250));
                }
                grid.add(cell);
            }
        }

        currentDotPanel = new JPanel(new BorderLayout());
        currentDotPanel.add(new JScrollPane(grid), BorderLayout.CENTER);

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT));
        legend.add(createLegendDot("Match", new Color(72,179,92)));
        legend.add(createLegendDot("No match", new Color(240,240,240)));
        currentDotPanel.add(legend, BorderLayout.SOUTH);

        dotPlotContainer.add(currentDotPanel, BorderLayout.CENTER);
        dotPlotContainer.revalidate();
        dotPlotContainer.repaint();
    }

    private JComponent createLegendDot(String text, Color c) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT,4,4));
        JLabel box = new JLabel("  ");
        box.setOpaque(true);
        box.setBackground(c);
        box.setBorder(BorderFactory.createLineBorder(Color.gray));
        p.add(box);
        p.add(new JLabel(text));
        return p;
    }

    private void computeAlignment() {
        String s1 = seq1Field.getText().trim().toUpperCase();
        String s2 = seq2Field.getText().trim().toUpperCase();

        if (s1.isEmpty() || s2.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Both sequences must be provided.", "Input required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        NeedlemanWunsch nw = new NeedlemanWunsch(s1, s2);
        int[][] matrix = nw.createMatrix();

        StringBuilder out = new StringBuilder();
        out.append("===== NEEDLEMAN–WUNSCH DP MATRIX =====\n");
        out.append(nw.matrixToString(matrix));

        NeedlemanWunsch.AlignmentResult res = nw.align();
        out.append("\n===== ALIGNMENT =====\n");
        out.append("Score: " + res.score + "\n\n");
        out.append(res.alignedSeq1 + "\n");
        out.append(res.alignedSeq2 + "\n\n");

        outputArea.append(out.toString());

        // store last alignment pieces for visual
        lastAlignment = res;
        lastSeq1 = s1;
        lastSeq2 = s2;
    }

    private NeedlemanWunsch.AlignmentResult lastAlignment = null;

    private void displayVisualAlignment() {
        if (lastAlignment == null) return;

        Container root = getContentPane();
        JTabbedPane tp = null;
        for (Component c : root.getComponents()) if (c instanceof JTabbedPane) tp = (JTabbedPane) c;
        if (tp == null) return;

        JPanel visualPanel = (JPanel) tp.getComponentAt(0);
        JSplitPane split = null;
        for (Component c : visualPanel.getComponents()) if (c instanceof JSplitPane) split = (JSplitPane) c;
        if (split == null) return;

        JPanel alignContainer = (JPanel) split.getBottomComponent();
        JPanel alignCenter = (JPanel) ((BorderLayout) alignContainer.getLayout()).getLayoutComponent(BorderLayout.CENTER);

        if (currentAlignPanel != null) alignCenter.remove(currentAlignPanel);

        String a1 = lastAlignment.alignedSeq1;
        String a2 = lastAlignment.alignedSeq2;

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JPanel topRow = new JPanel();
        JPanel midRow = new JPanel();
        JPanel botRow = new JPanel();

        topRow.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));
        midRow.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));
        botRow.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));

        for (int k = 0; k < a1.length(); k++) {
            char c1 = a1.charAt(k);
            char c2 = a2.charAt(k);

            Color bg;
            if (c1 == '-' || c2 == '-') bg = new Color(220, 220, 220); // gap
            else if (c1 == c2) bg = new Color(72, 179, 92); // match
            else bg = new Color(244, 156, 66); // mismatch orange

            JLabel topLabel = new JLabel(String.valueOf(c1));
            topLabel.setOpaque(true);
            topLabel.setBackground(bg);
            topLabel.setForeground(Color.white);
            topLabel.setBorder(BorderFactory.createLineBorder(Color.gray));
            topLabel.setPreferredSize(new Dimension(18, 18));

            JLabel midLabel = new JLabel(" ");
            midLabel.setPreferredSize(new Dimension(18, 6));

            JLabel botLabel = new JLabel(String.valueOf(c2));
            botLabel.setOpaque(true);
            botLabel.setBackground(bg);
            botLabel.setForeground(Color.white);
            botLabel.setBorder(BorderFactory.createLineBorder(Color.gray));
            botLabel.setPreferredSize(new Dimension(18, 18));

            topRow.add(topLabel);
            midRow.add(midLabel);
            botRow.add(botLabel);
        }

        p.add(topRow);
        p.add(midRow);
        p.add(botRow);

        // score display
        JLabel scoreLab = new JLabel("Score: " + lastAlignment.score);
        scoreLab.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));
        p.add(scoreLab);

        currentAlignPanel = new JPanel(new BorderLayout());
        currentAlignPanel.add(new JScrollPane(p), BorderLayout.CENTER);

        alignCenter.add(currentAlignPanel, BorderLayout.CENTER);
        alignCenter.revalidate();
        alignCenter.repaint();
    }

    public static void createAndShow() {
        BioinfoGUI gui = new BioinfoGUI();
        gui.setVisible(true);
    }
}
