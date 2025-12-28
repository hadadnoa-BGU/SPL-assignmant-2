package memory;

import java.util.concurrent.locks.ReadWriteLock;

public class SharedVector {

    private double[] vector;
    private VectorOrientation orientation;
    private ReadWriteLock lock = new java.util.concurrent.locks.ReentrantReadWriteLock();

    public SharedVector(double[] vector, VectorOrientation orientation) {
        this.vector = vector; //maybe we should implement deep copy
        this.orientation = orientation;
    }

    public double get(int index) {
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
        this.lock.writeLock().lock();
    }

    public void writeUnlock() {
        this.lock.writeLock().unlock();
    }

    public void readLock() {
        this.lock.readLock().lock();
    }

    public void readUnlock() {
        this.lock.readLock().unlock();
    }

    public void transpose() {
        this.writeLock();
        this.orientation = (this.orientation == VectorOrientation.ROW_MAJOR) ? VectorOrientation.COLUMN_MAJOR : VectorOrientation.ROW_MAJOR;
        this.writeUnlock();
    }

    public void add(SharedVector other) {
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

    public void vecMatMul(SharedMatrix matrix) 
    {
        if (matrix == null) 
            throw new IllegalArgumentException("matrix is null");

        writeLock();
        // other.acquireAllVectorReadLocks();
        try 
        {
            if(this.orientation == VectorOrientation.ROW_MAJOR)
            {
                if(other.getOrientation() == VectorOrientation.ROW_MAJOR)
                {
                    
                }
                else
                {

                }
            }
            else
            {
                if(other.getOrientation() == VectorOrientation.ROW_MAJOR)
                {

                }
                else
                {
                    
                }
            }
        } 
        finally 
        {
            writeUnlock();
        }
}
