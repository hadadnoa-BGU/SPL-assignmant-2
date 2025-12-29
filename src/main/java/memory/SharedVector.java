package memory;

import java.util.concurrent.locks.ReadWriteLock;

public class SharedVector {

    private double[] vector;
    private VectorOrientation orientation;
    private ReadWriteLock lock = new java.util.concurrent.locks.ReentrantReadWriteLock();

    public SharedVector(double[] vector, VectorOrientation orientation) {
        // TODO: store vector data and its orientation
        this.vector = vector; //maybe we should implement deep copy
        this.orientation = orientation;
    }

    public double get(int index) {
        // TODO: return element at index (read-locked)
        double result;
        this.readLock();
        try 
        {
            if (index < 0 || index >= vector.length) 
            {
                throw new IndexOutOfBoundsException("index should be between 0 and " + (vector.length - 1) + " but was: " + index);
            }
            result = this.vector[index];
        } 
        finally 
        {
            this.readUnlock();
        }
        return result;
    }

    public int length() {
        // TODO: return vector length
        this.readLock();
        try
        {
            return this.vector.length;
        }
        finally
        {
            this.readUnlock();
        }
    }

    public VectorOrientation getOrientation() {
        // TODO: return vector orientation
        this.readLock();
        try
        {
            return this.orientation;
        }
        finally
        {
            this.readUnlock();
        }
    }

    public void writeLock() {
        // TODO: acquire write lock
        this.lock.writeLock().lock();
    }

    public void writeUnlock() {
        // TODO: release write lock
        this.lock.writeLock().unlock();
    }

    public void readLock() {
        // TODO: acquire read lock
        this.lock.readLock().lock();
    }

    public void readUnlock() {
        // TODO: release read lock
        this.lock.readLock().unlock();
    }

    public void transpose() {
        // TODO: transpose vector
        this.writeLock();
        this.orientation = (this.orientation == VectorOrientation.ROW_MAJOR) ? VectorOrientation.COLUMN_MAJOR : VectorOrientation.ROW_MAJOR;
        this.writeUnlock();
    }

    public void add(SharedVector other) {
        // TODO: add two vectors
        if (other == null) 
            throw new IllegalArgumentException("other is null");

        int thisLength = this.length();
        this.writeLock();
        try
        {
            //first check if other==this
            if(this == other)
            {
                for(int i = 0; i < thisLength; i++)
                {
                    this.vector[i] *= 2;
                }
            }
            else
            {
                // check dimentions
                int otherLength = other.length();
                if(thisLength != otherLength)
                    throw new ArithmeticException("Undefined operation: cannot add A(" + thisLength + " x 1) with B(" + otherLength + " x 1)");
                other.readLock();
                try
                {
                    // perform computations
                    for(int i = 0; i < thisLength; i++)
                    {
                        this.vector[i] += other.vector[i];
                    }
                }
                finally
                {
                    other.readUnlock();
                }  
            }
        }
        finally
        {
            this.writeUnlock();
        }  
    }

    public void negate() 
    {
        // TODO: negate vector
        this.writeLock();
        try
        {
            //negation
            for (int i = 0; i < this.vector.length; i++)
            {
                this.vector[i] = -this.vector[i];
            }
        }
        finally
        {
            this.writeUnlock();
        }
    }

    public double dot(SharedVector other) {
        // TODO: compute dot product (row · column)
        if (other == null) 
            throw new IllegalArgumentException("other is null");

        double result = 0.0;
        int thisLength = this.length();

        this.readLock();
        other.readLock();
        try
        {
            // first check dimentions
            int otherLength = other.length();
            if(thisLength != otherLength)
                throw new ArithmeticException("Undefined operation: cannot multiply A(" + thisLength + " x 1) by B(" + otherLength + " x 1)");

            // perform computations
            for (int i = 0; i < thisLength; i++)
            {
                result += this.vector[i] * other.vector[i];
            }
        }
        finally
        {
            other.readUnlock();
            this.readUnlock();
        }  
        
        return result;
    }

    public void vecMatMul(SharedMatrix matrix) {
        // TODO: compute row-vector × matrix
        if (matrix == null) {
            throw new IllegalArgumentException("matrix is null");
        }

        this.writeLock();
        try {
            int myLength = this.vector.length;
            int matRows;
            int matCols;

            // check dimensions
            if (matrix.getOrientation() == VectorOrientation.ROW_MAJOR) {
                matRows = matrix.length();
                if (matRows > 0) {
                    matCols = matrix.get(0).length();
                } else {
                    matCols = 0;
                }
            } else {
                // column major case
                matCols = matrix.length();
                if (matCols > 0) {
                    matRows = matrix.get(0).length();
                } else {
                    matRows = 0;
                }
            }

            // check legality
            if (myLength != matRows) {
                throw new IllegalArgumentException(
                        "Dimension mismatch: Vector length (" + myLength +
                                ") must match Matrix rows (" + matRows + ")"
                );
            }

            double[] result = new double[matCols];


            if (matrix.getOrientation() == VectorOrientation.COLUMN_MAJOR) {
                // matrix is columns
                for (int i = 0; i < matCols; i++) {
                    SharedVector colVec = matrix.get(i);
                    result[i] = this.dot(colVec);
                }
            } else {
                // matrix is rows
                for (int j = 0; j < matCols; j++) {
                    double sum = 0;
                    // check rows for each column
                    for (int k = 0; k < matRows; k++) {
                        double matVal = matrix.get(k).get(j);
                        sum += this.vector[k] * matVal;
                    }
                    result[j] = sum;
                }
            }

            this.vector = result;

        } finally {
            this.writeUnlock();
        }
    }
}
