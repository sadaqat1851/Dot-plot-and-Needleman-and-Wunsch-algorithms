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

        // also show DP matrix visually in the top area (matrix with highlighted traceback)
        displayVisualMatrix(res);
    }

    private void displayVisualMatrix(NeedlemanWunsch.AlignmentResult res) {
        if (res == null || res.matrix == null) return;

        Container root = getContentPane();
        JTabbedPane tp = null;
        for (Component c : root.getComponents()) if (c instanceof JTabbedPane) tp = (JTabbedPane) c;
        if (tp == null) return;

        JPanel visualPanel = (JPanel) tp.getComponentAt(0);
        JSplitPane split = null;
        for (Component c : visualPanel.getComponents()) if (c instanceof JSplitPane) split = (JSplitPane) c;
        if (split == null) return;

        JPanel dotPlotContainer = (JPanel) split.getTopComponent();
        // remove existing (dot plot or previous matrix)
        if (currentDotPanel != null) dotPlotContainer.remove(currentDotPanel);

        int[][] matrix = res.matrix;
        int rows = matrix.length;
        int cols = matrix[0].length;

        // build a grid with row header + matrix
        JPanel grid = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;

        // top-left empty
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0; gbc.weighty = 0;
        grid.add(new JLabel(""), gbc);

        // top header (seq2, include '-' for j=0)
        for (int j = 0; j < cols; j++) {
            gbc.gridx = j + 1; gbc.gridy = 0; gbc.weightx = 1; gbc.weighty = 0;
            JLabel h;
            if (j == 0) h = new JLabel("-", SwingConstants.CENTER);
            else h = new JLabel(String.valueOf(lastSeq2.charAt(j - 1)), SwingConstants.CENTER);
            h.setBorder(BorderFactory.createLineBorder(Color.lightGray));
            h.setOpaque(true);
            h.setBackground(new Color(240,240,240));
            grid.add(h, gbc);
        }

        // matrix cells with left header
        java.util.Set<String> pathSet = new java.util.HashSet<>();
        if (res.tracebackPath != null) {
            for (int[] coord : res.tracebackPath) pathSet.add(coord[0] + "," + coord[1]);
        }

        for (int i = 0; i < rows; i++) {
            // left header
            gbc.gridx = 0; gbc.gridy = i + 1; gbc.weightx = 0;
            JLabel leftH;
            if (i == 0) leftH = new JLabel("-", SwingConstants.CENTER);
            else leftH = new JLabel(String.valueOf(lastSeq1.charAt(i - 1)), SwingConstants.CENTER);
            leftH.setBorder(BorderFactory.createLineBorder(Color.lightGray));
            leftH.setOpaque(true);
            leftH.setBackground(new Color(240,240,240));
            grid.add(leftH, gbc);

            for (int j = 0; j < cols; j++) {
                gbc.gridx = j + 1; gbc.gridy = i + 1; gbc.weightx = 1; gbc.weighty = 1;
                int value = matrix[i][j];
                JLabel cell = new JLabel(String.valueOf(value), SwingConstants.CENTER);
                cell.setOpaque(true);

                // color mapping (simple): positive -> greenish; zero -> white; negative -> light red
                if (pathSet.contains(i + "," + j)) {
                    // highlight path specially
                    cell.setBackground(new Color(255, 229, 153)); // pale yellow
                    cell.setBorder(BorderFactory.createLineBorder(new Color(255,140,0), 2));
                } else if (value > 0) {
                    cell.setBackground(new Color(200, 255, 200));
                } else if (value == 0) {
                    cell.setBackground(new Color(245, 245, 245));
                } else {
                    cell.setBackground(new Color(255,220,220));
                }
                cell.setBorder(BorderFactory.createLineBorder(new Color(220,220,220)));
                cell.setFont(cell.getFont().deriveFont(Font.PLAIN, 12f));

                // tooltip with coordinates
                cell.setToolTipText(String.format("(%d,%d) = %d", i, j, value));
                grid.add(cell, gbc);
            }
        }

        currentDotPanel = new JPanel(new BorderLayout());
        currentDotPanel.add(new JScrollPane(grid), BorderLayout.CENTER);

        // legend
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT));
        legend.add(createLegendDot("Traceback path", new Color(255,229,153)));
        legend.add(createLegendDot("Positive score", new Color(200,255,200)));
        legend.add(createLegendDot("Zero", new Color(245,245,245)));
        legend.add(createLegendDot("Negative score", new Color(255,220,220)));
        currentDotPanel.add(legend, BorderLayout.SOUTH);

        dotPlotContainer.add(currentDotPanel, BorderLayout.CENTER);
        dotPlotContainer.revalidate();
        dotPlotContainer.repaint();
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

        // Build a clean 3-row grid (top sequence, symbol row, bottom sequence) so each aligned column
        // is a consistent cell. This looks like the dot-plot visual but in alignment form.
        int cols = a1.length();
        JPanel grid = new JPanel(new GridLayout(3, cols, 2, 2));
        grid.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        // Build the three rows as arrays then add them row-by-row so columns align
        JLabel[] topLabels = new JLabel[cols];
        JLabel[] midLabels = new JLabel[cols];
        JLabel[] botLabels = new JLabel[cols];

        for (int k = 0; k < cols; k++) {
            char c1 = a1.charAt(k);
            char c2 = a2.charAt(k);

            String midSymbol;
            if (c1 == '-' || c2 == '-') {
                midSymbol = "-";
            } else if (c1 == c2) {
                midSymbol = "|";
            } else {
                midSymbol = ":";
            }

            JLabel topLabel = new JLabel(String.valueOf(c1), SwingConstants.CENTER);
            topLabel.setToolTipText("Top: " + c1 + " at col " + (k + 1));

            JLabel midLabel = new JLabel(midSymbol, SwingConstants.CENTER);
            midLabel.setToolTipText("Relation: " + (midSymbol.equals("|")?"match": midSymbol.equals(":")?"mismatch":"gap"));

            JLabel botLabel = new JLabel(String.valueOf(c2), SwingConstants.CENTER);
            botLabel.setToolTipText("Bottom: " + c2 + " at col " + (k + 1));

            topLabels[k] = topLabel;
            midLabels[k] = midLabel;
            botLabels[k] = botLabel;
        }

        // add top row cells
        for (int k = 0; k < cols; k++) {
            Color bg;
            char c1 = a1.charAt(k);
            char c2 = a2.charAt(k);
            if (c1 == '-' || c2 == '-') bg = new Color(200,200,200);
            else if (c1 == c2) bg = new Color(72,179,92);
            else bg = new Color(244,156,66);
            styleAlignmentCell(topLabels[k], bg);
            grid.add(topLabels[k]);
        }

        // add middle row
        for (int k = 0; k < cols; k++) {
            JLabel mid = midLabels[k];
            mid.setOpaque(true);
            mid.setBackground(new Color(245,245,245));
            mid.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            mid.setPreferredSize(new Dimension(20, 18));
            grid.add(mid);
        }

        // add bottom row
        for (int k = 0; k < cols; k++) {
            Color bg;
            char c1 = a1.charAt(k);
            char c2 = a2.charAt(k);
            if (c1 == '-' || c2 == '-') bg = new Color(200,200,200);
            else if (c1 == c2) bg = new Color(72,179,92);
            else bg = new Color(244,156,66);
            styleAlignmentCell(botLabels[k], bg);
            grid.add(botLabels[k]);
        }

        JPanel p = new JPanel(new BorderLayout());
        p.add(grid, BorderLayout.CENTER);

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

    private void styleAlignmentCell(JLabel lbl, Color bg) {
        lbl.setOpaque(true);
        lbl.setBackground(bg);
        lbl.setForeground(Color.white);
        lbl.setBorder(BorderFactory.createLineBorder(Color.gray));
        lbl.setPreferredSize(new Dimension(24, 24));
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 12f));
    }

    public static void createAndShow() {
        BioinfoGUI gui = new BioinfoGUI();
        gui.setVisible(true);
    }
}
