import memory.SharedMatrix;
import org.junit.jupiter.api.Test;

public class SharedMatrixTest {


    @Test
    void testLoadColumnMajor() {
        // Test loading a matrix in column-major order
        SharedMatrix matrix = new SharedMatrix();
        double[][] data = {
            {1, 2, 3},
            {4, 5, 6},
            {7, 8, 9}
        };
        matrix.loadColumnMajor(data);
        double[][] readData = matrix.readRowMajor();
        for (int i = 0; i < readData.length; i++) {
            for (int j = 0; j < readData[0].length; j++) {
                assert(readData[i][j] == data[i][j]);
            }
        }  
    }

    @Test
    void testLoadRowMajor() {
        // Test loading a matrix in row-major order
        SharedMatrix matrix = new SharedMatrix();
        double[][] data = {
            {1, 2, 3},
            {4, 5, 6},
            {7, 8, 9}
        };
        matrix.loadRowMajor(data);
        double[][] readData = matrix.readRowMajor();
        for (int i = 0; i < readData.length; i++) {
            for (int j = 0; j < readData[0].length; j++) {
                assert(readData[i][j] == data[i][j]);
            }
        }  
    }
     @Test
    void testReadRawMajor() {
        // Test reading a matrix in row-major order
        double[][] data = {
            {1, 2, 3},
            {4, 5, 6},
            {7, 8, 9}
        };
        SharedMatrix matrix = new SharedMatrix(data);
        double[][] readData = matrix.readRowMajor();
        for (int i = 0; i < readData.length; i++) {
            for (int j = 0; j < readData[0].length; j++) {
                assert(readData[i][j] == data[i][j]);
            }
        }  
    }
}