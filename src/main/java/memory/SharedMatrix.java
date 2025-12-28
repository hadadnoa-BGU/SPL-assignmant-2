package memory;

public class SharedMatrix {

    private volatile SharedVector[] vectors = {}; // underlying vectors
    private volatile VectorOrientation orientation = VectorOrientation.ROW_MAJOR;


    public SharedMatrix() {
        // TODO: initialize empty matrix
        this.vectors = new SharedVector[0];
    }

    public SharedMatrix(double[][] matrix) {
        // TODO: construct matrix as row-major SharedVectors
        loadRowMajor(matrix);

    }

    public void loadRowMajor(double[][] matrix) {
        // TODO: replace internal data with new row-major matrix

        if( matrix == null){
            throw new IllegalArgumentException("matrix is null");
        }
        SharedVector[] newVectors = new SharedVector[matrix.length];

        for(int i = 0; i < matrix.length; i++) {
            if (matrix[i] == null) {
                throw new IllegalArgumentException("row " + i + " is null");
            }
            double[] row = new double[matrix[i].length];
            for (int j = 0; j < matrix[i].length; j++) {
                row[j] = matrix[i][j];
            }


            newVectors[i] = new SharedVector(row, VectorOrientation.ROW_MAJOR);
        }

        this.vectors = newVectors;
        this.orientation = VectorOrientation.ROW_MAJOR;
    }

    public void loadColumnMajor(double[][] matrix) {
        // TODO: replace internal data with new column-major matrix
    }

    public double[][] readRowMajor() {
        // TODO: return matrix contents as a row-major double[][]
        return null;
    }

    public SharedVector get(int index) {
        // TODO: return vector at index
        return vectors[index];
    }

    public int length() {
        // TODO: return number of stored vectors
        return vectors.length;
    }

    public VectorOrientation getOrientation() {
        // TODO: return orientation
        return orientation;
    }

    private void acquireAllVectorReadLocks(SharedVector[] vecs) {
        // TODO: acquire read lock for each vector
        for (SharedVector v : vecs) {
            v.readLock();
        }
    }

    private void releaseAllVectorReadLocks(SharedVector[] vecs) {
        // TODO: release read locks

        for (SharedVector v : vecs) {
            v.readUnlock();
        }

    }

    private void acquireAllVectorWriteLocks(SharedVector[] vecs) {
        // TODO: acquire write lock for each vector
        for (SharedVector v : vecs) {
            v.writeLock();
        }
    }

    private void releaseAllVectorWriteLocks(SharedVector[] vecs) {
        // TODO: release write locks

        for (SharedVector v : vecs) {
            v.writeUnlock();
        }
    }
}
