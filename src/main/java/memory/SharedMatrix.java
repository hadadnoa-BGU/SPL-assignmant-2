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
        if (matrix == null) {
            throw new IllegalArgumentException("matrix is null");
        }

        //check if matrix is empty
        if (matrix.length == 0) {
            this.vectors = new SharedVector[0];
            this.orientation = VectorOrientation.COLUMN_MAJOR;
            return;
        }

        int rows = matrix.length;
        int cols = matrix[0].length;

        //check none of the elements are null
        for (int r = 0; r < rows; r++) {
            if (matrix[r] == null || matrix[r].length != cols) {
                throw new IllegalArgumentException("matrix is not rectangular");
            }
        }

        SharedVector[] newVectors = new SharedVector[cols];

        for (int c = 0; c < cols; c++) {
            double[] column = new double[rows];
            for (int r = 0; r < rows; r++) {
                column[r] = matrix[r][c];
            }
            newVectors[c] = new SharedVector(column, VectorOrientation.COLUMN_MAJOR);
        }

        this.vectors = newVectors;
        this.orientation = VectorOrientation.COLUMN_MAJOR;

    }

    public double[][] readRowMajor() {
        // TODO: return matrix contents as a row-major double[][]
        SharedVector[] output = this.vectors;
        acquireAllVectorReadLocks(output);
        try{
            if(output.length == 0){
                return new double[0][0];
            }
            //orientation = rows
            if(this.orientation == VectorOrientation.ROW_MAJOR){
                double[][] out = new double[output.length][];

                for (int i = 0; i < output.length; i++) {
                    int cols = output[i].length();
                    out[i] = new double[cols];
                    for (int j = 0; j < cols; j++) {
                        out[i][j] = output[i].get(j);
                    }
                }
                return out;
            }else{
                //orientation = columns
                int cols = output.length;
                int rows = output[0].length();

                double[][] out = new double[rows][cols];
                for (int i = 0; i < cols; i++) {
                    if (output[i].length() != rows) {
                        throw new IllegalArgumentException("column vectors length changes");
                    }
                    for (int j = 0; j < rows; j++) {
                        out[j][i] = output[i].get(j);
                    }
                }
                return out;

            }
        }finally {
            releaseAllVectorReadLocks(output);
        }



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
