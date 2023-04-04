package util.dataWriter;
import java.io.FileWriter;
import java.io.IOException;

public class MatrixToCsv {

    public void convert(int[][] matrix,String name) throws IOException {

        String fileName = "Output_Files//"+name+".csv";

        try (FileWriter writer = new FileWriter(fileName)) {
            for (int[] row : matrix) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < row.length; i++) {
                    sb.append(row[i]);
                    if (i != row.length - 1) {
                        sb.append(",");
                    }
                }
                sb.append("\n");
                writer.write(sb.toString());
            }
        }
    }
}
