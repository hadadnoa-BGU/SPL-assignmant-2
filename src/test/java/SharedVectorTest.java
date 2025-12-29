import memory.SharedVector;
import memory.VectorOrientation;
import org.junit.jupiter.api.Test;

public class SharedVectorTest {
    @Test
    void testAdd() {
        SharedVector v1 = new SharedVector(new double[]{1, 2, 3}, VectorOrientation.ROW_MAJOR);
        SharedVector v2 = new SharedVector(new double[]{4, 5, 6}, VectorOrientation.ROW_MAJOR);
        v1.add(v2);
        assert(v1.get(0) == 5);
        assert(v1.get(1) == 7);
        assert(v1.get(2) == 9);
    }

    @Test
    void testDot() {
        SharedVector v1 = new SharedVector(new double[]{1, 2, 3}, VectorOrientation.ROW_MAJOR);
        SharedVector v2 = new SharedVector(new double[]{4, 5, 6}, VectorOrientation.ROW_MAJOR);
        double result = v1.dot(v2);
        assert(result == 32); // 1*4 + 2*5 + 3*6 = 32
    }

    @Test
    void testNegate() {
        SharedVector v = new SharedVector(new double[]{1, -2, 3}, VectorOrientation.ROW_MAJOR);
        v.negate();
        assert(v.get(0) == -1);
        assert(v.get(1) == 2);
        assert(v.get(2) == -3);
        SharedVector v2 = new SharedVector(new double[]{1, 23, 232, 56}, VectorOrientation.ROW_MAJOR);
        v2.negate();
        assert(v2.get(0) == -1);
        assert(v2.get(1) == -23);
        assert(v2.get(2) == -232);
        assert(v2.get(3) == -56);
    }

    @Test
    void testTranspose() {
        SharedVector v = new SharedVector(new double[]{1, 2, 3}, VectorOrientation.ROW_MAJOR);
        v.transpose();
        assert(v.getOrientation() == VectorOrientation.COLUMN_MAJOR);
        v.transpose();
        assert(v.getOrientation() == VectorOrientation.ROW_MAJOR);
        SharedVector v2 = new SharedVector(new double[]{4, 5, 6, 45}, VectorOrientation.COLUMN_MAJOR);
        v2.transpose();
        assert(v2.getOrientation() == VectorOrientation.ROW_MAJOR);
    }

    @Test
    void testVecMatMul() {
        SharedVector v = new SharedVector(new double[]{1, 2}, VectorOrientation.ROW_MAJOR);
        SharedMatrix m = new SharedMatrix(new double[][]{
            {3, 4},
            {5, 6}
        });
        v.vecMatMul(m);
        //assume that matrix is in column major
        assert(v.get(0) == 11); // (1*3 + 2*4)
        assert(v.get(1) == 17); // (1*5 + 2*6)

        SharedVector v2 = new SharedVector(new double[]{7, 8, 9}, VectorOrientation.ROW_MAJOR);
        SharedMatrix m2 = new SharedMatrix(new double[][]{
            {1, 2, 3},
            {4, 5, 6},
            {7, 8, 9}
        });
        v2.vecMatMul(m2);
        assert(v2.get(0) == 50); // (7*1 + 8*2 + 9*3)
        assert(v2.get(1) == 122); // (7*4 + 8*5 + 9*6)
        assert(v2.get(2) == 194); // (7*7 + 8*8 + 9*9)
    }
}
