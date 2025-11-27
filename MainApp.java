/* Assignment11.java
   OOP Java program to:
   build a dot-plot for two sequences
   perform Needleman-Wunsch global alignment
   print DP score grid, direction grid, step-by-step computations
   print traceback and final alignments
   Uses simple scoring:
     match = +1
     mismatch = -1
     gap = -1
   Sequences come from assignment:
     Sequence 1: TTTTGGGCGATAGCTAAAGCTC
     Sequence 2: ATTGGGCGGTAGCTTAAGGTC
*/

public class MainApp {
    public static void main(String[] args) {
    // If 'cli' is supplied as argument run the console flow, otherwise start the GUI
    if (args != null && args.length > 0 && args[0].equalsIgnoreCase("cli")) {
      String seq1 = "TTTTGGGCGATAGCTAAAGCTC";
      String seq2 = "ATTGGGCGGTAGCTTAAGGTC";

      DotPlot dp = new DotPlot(seq1, seq2);
      char[][] dot = dp.createDotPlot();

      System.out.println("\n===== DOT PLOT =====");
      dp.printDotPlot(dot);

      NeedlemanWunsch nw = new NeedlemanWunsch(seq1, seq2);
      int[][] matrix = nw.createMatrix();

      System.out.println("\n===== NEEDLEMAN-WUNSCH MATRIX =====");
      nw.printMatrix(matrix);
    } else {
      // Start the Swing GUI
      javax.swing.SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          BioinfoGUI.createAndShow();
        }
      });
    }
    }
}
