public class NeedlemanWunsch {

    private String seq1, seq2;
    private int match = 1, mismatch = -1, gap = -1;

    public NeedlemanWunsch(String s1, String s2) {
        this.seq1 = s1;
        this.seq2 = s2;
    }

    public int[][] createMatrix() {
        int rows = seq1.length() + 1;
        int cols = seq2.length() + 1;

        int[][] matrix = new int[rows][cols];

        for (int i = 0; i < rows; i++)
            matrix[i][0] = i * gap;

        for (int j = 0; j < cols; j++)
            matrix[0][j] = j * gap;

        for (int i = 1; i < rows; i++) {
            for (int j = 1; j < cols; j++) {

                int diag = matrix[i - 1][j - 1] +
                        (seq1.charAt(i - 1) == seq2.charAt(j - 1) ? match : mismatch);

                int up = matrix[i - 1][j] + gap;
                int left = matrix[i][j - 1] + gap;

                matrix[i][j] = Math.max(diag, Math.max(up, left));
            }
        }
        return matrix;
    }

    public void printMatrix(int[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;

        // Print top header
        System.out.print("      ");
        for (int j = 0; j < seq2.length(); j++)
            System.out.printf("%4c", seq2.charAt(j));
        System.out.println();

        printLine(cols);

        // Print rows
        for (int i = 0; i < rows; i++) {

            // Row label
            if (i == 0) System.out.print("   | ");
            else        System.out.printf("%2c | ", seq1.charAt(i - 1));

            // Values
            for (int j = 0; j < cols; j++) {
                System.out.printf("%4d", matrix[i][j]);
            }
            System.out.println();
        }

        printLine(cols);
    }

    private void printLine(int cols) {
        System.out.print("----+-");
        for (int i = 0; i < cols; i++) {
            System.out.print("----");
        }
        System.out.println();
    }
}
