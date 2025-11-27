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

        // main layout
        setLayout(new BorderLayout(10, 10));

        // top panel: inputs
        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));

        JPanel inputs = new JPanel(new GridLayout(2, 1, 6, 6));

        seq1Field = createInputArea("TTTTGGGCGATAGCTAAAGCTC");
        seq2Field = createInputArea("ATTGGGCGGTAGCTTAAGGTC");

        inputs.add(wrapWithLabel("Sequence 1 (A/C/G/T)", seq1Field));
        inputs.add(wrapWithLabel("Sequence 2 (A/C/G/T)", seq2Field));

        top.add(inputs, BorderLayout.CENTER);

        // buttons panel
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton dotBtn = new JButton("Generate Dot-plot");
        JButton alignBtn = new JButton("Compute Alignment");
        JButton clearBtn = new JButton("Clear Output");
        controls.add(dotBtn);
        controls.add(alignBtn);
        controls.add(clearBtn);

        top.add(controls, BorderLayout.SOUTH);

        add(top, BorderLayout.NORTH);

        // center: output area
        outputArea = new JTextArea();
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        outputArea.setEditable(false);
        outputArea.setMargin(new Insets(8, 8, 8, 8));

        JScrollPane scroll = new JScrollPane(outputArea);
        scroll.setBorder(BorderFactory.createTitledBorder("Output"));
        add(scroll, BorderLayout.CENTER);

        // footer: small help
        JLabel footer = new JLabel("Tip: copy/paste sequences and click an action. Matches are shown as \"*\" in dot-plot.");
        footer.setBorder(BorderFactory.createEmptyBorder(6, 8, 8, 8));
        add(footer, BorderLayout.SOUTH);

        // actions
        dotBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                generateDotPlot();
            }
        });

        alignBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                computeAlignment();
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
    }

    public static void createAndShow() {
        BioinfoGUI gui = new BioinfoGUI();
        gui.setVisible(true);
    }
}
