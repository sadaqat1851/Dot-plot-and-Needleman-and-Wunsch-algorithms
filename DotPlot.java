public class DotPlot {

    private String seq1, seq2;

    public DotPlot(String s1, String s2) {
        this.seq1 = s1;
        this.seq2 = s2;
    }

    public char[][] createDotPlot() {
        int rows = seq1.length();
        int cols = seq2.length();
        char[][] grid = new char[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                grid[i][j] = (seq1.charAt(i) == seq2.charAt(j)) ? '*' : ' ';
            }
        }
        return grid;
    }

    public void printDotPlot(char[][] grid) {
        System.out.print("      ");
        for (int j = 0; j < seq2.length(); j++)
            System.out.printf("%2c", seq2.charAt(j));
        System.out.println();

        for (int i = 0; i < seq1.length(); i++) {
            System.out.printf("%2c | ", seq1.charAt(i));
            for (int j = 0; j < seq2.length(); j++)
                System.out.printf("%2c", grid[i][j]);
            System.out.println();
        }
    }

    /**
     * Build a string representation of the dot-plot for GUI display
     */
    public String dotPlotToString(char[][] grid) {
        StringBuilder sb = new StringBuilder();
        sb.append("      ");
        for (int j = 0; j < seq2.length(); j++)
            sb.append(String.format("%2c", seq2.charAt(j)));
        sb.append('\n');

        for (int i = 0; i < seq1.length(); i++) {
            sb.append(String.format("%2c | ", seq1.charAt(i)));
            for (int j = 0; j < seq2.length(); j++)
                sb.append(String.format("%2c", grid[i][j]));
            sb.append('\n');
        }
        return sb.toString();
    }
}
