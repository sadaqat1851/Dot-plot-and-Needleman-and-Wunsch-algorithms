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

    /**
     * Compute alignment (traceback) from the filled DP matrix.
     * Returns an AlignmentResult containing the aligned sequences and score.
     */
    public AlignmentResult align() {
        int[][] matrix = createMatrix();
        StringBuilder a1 = new StringBuilder();
        StringBuilder a2 = new StringBuilder();

        int i = seq1.length();
        int j = seq2.length();

        while (i > 0 || j > 0) {
            if (i > 0 && j > 0) {
                int scoreHere = matrix[i][j];
                int diag = matrix[i - 1][j - 1];
                int up = (i > 0) ? matrix[i - 1][j] : Integer.MIN_VALUE/2;
                int left = (j > 0) ? matrix[i][j - 1] : Integer.MIN_VALUE/2;

                int matchScore = (seq1.charAt(i - 1) == seq2.charAt(j - 1)) ? match : mismatch;

                // Prefer diagonal when it equals the current cell
                if (scoreHere == diag + matchScore) {
                    a1.append(seq1.charAt(i - 1));
                    a2.append(seq2.charAt(j - 1));
                    i--; j--;
                    continue;
                }
                if (scoreHere == up + gap) {
                    a1.append(seq1.charAt(i - 1));
                    a2.append('-');
                    i--;
                    continue;
                }
                if (scoreHere == left + gap) {
                    a1.append('-');
                    a2.append(seq2.charAt(j - 1));
                    j--;
                    continue;
                }
            }

            // If either sequence has remaining characters, consume them
            if (i > 0) {
                a1.append(seq1.charAt(i - 1));
                a2.append('-');
                i--;
            } else if (j > 0) {
                a1.append('-');
                a2.append(seq2.charAt(j - 1));
                j--;
            }
        }

        String aligned1 = a1.reverse().toString();
        String aligned2 = a2.reverse().toString();
        int score = matrix[seq1.length()][seq2.length()];

        return new AlignmentResult(aligned1, aligned2, score, matrix);
    }

    /**
     * Alignment result container
     */
    public static class AlignmentResult {
        public final String alignedSeq1;
        public final String alignedSeq2;
        public final int score;
        public final int[][] matrix;

        public AlignmentResult(String a1, String a2, int score, int[][] matrix) {
            this.alignedSeq1 = a1;
            this.alignedSeq2 = a2;
            this.score = score;
            this.matrix = matrix;
        }
    }

    /**
     * Build a simple printable string representation of the DP matrix
     * suitable for display in a GUI text area.
     */
    public String matrixToString(int[][] matrix) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("     "));
        for (int j = 0; j < seq2.length(); j++) sb.append(String.format(" %4c", seq2.charAt(j)));
        sb.append('\n');
        sb.append("-----");
        for (int j = 0; j < matrix[0].length; j++) sb.append("----");
        sb.append('\n');

        for (int i = 0; i < matrix.length; i++) {
            if (i == 0) sb.append("  | ");
            else sb.append(String.format(" %2c | ", seq1.charAt(i - 1)));
            for (int j = 0; j < matrix[0].length; j++) sb.append(String.format(" %3d", matrix[i][j]));
            sb.append('\n');
        }
        return sb.toString();
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
