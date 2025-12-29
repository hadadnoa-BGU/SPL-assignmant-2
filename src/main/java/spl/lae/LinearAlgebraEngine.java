package spl.lae;

import parser.*;
import memory.*;
import scheduling.*;

import java.util.ArrayList;
import java.util.List;

public class LinearAlgebraEngine {

    private SharedMatrix leftMatrix = new SharedMatrix();
    private SharedMatrix rightMatrix = new SharedMatrix();
    private TiredExecutor executor;

    public LinearAlgebraEngine(int numThreads) {
        // TODO: create executor with given thread count

        executor = new TiredExecutor(numThreads);
    }

    public ComputationNode run(ComputationNode computationRoot) {
        // TODO: resolve computation tree step by step until final matrix is produced

        computationRoot.associativeNesting();

        while (computationRoot.getNodeType() != ComputationNodeType.MATRIX){

            ComputationNode node = computationRoot.findResolvable();
            loadAndCompute(node);
            node.resolve(leftMatrix.readRowMajor());
        }


        return computationRoot;
    }

    public void loadAndCompute(ComputationNode node) {
        // TODO: load operand matrices
        // TODO: create compute tasks & submit tasks to executor
        //we assume the node is already associatively nested

        List<Runnable> tasks;
        List<ComputationNode> children = node.getChildren();

        leftMatrix.loadRowMajor(children.get(0).getMatrix());

        if(children.size() == 2){
            rightMatrix.loadRowMajor(children.get(1).getMatrix());
        }


        //switch case taken from computation node map op
        switch (node.getNodeType()) {
            case ADD:
                tasks = createAddTasks();
                break;
            case MULTIPLY:
                tasks = createMultiplyTasks();
                break;
            case NEGATE:
                tasks = createNegateTasks();
                break;
            case TRANSPOSE:
                tasks = createTransposeTasks();
                break;
            default:
                throw new IllegalArgumentException("Unknown operator: " + node.getNodeType());
        }

        executor.submitAll(tasks);






    }

    public List<Runnable> createAddTasks() {
        // TODO: return tasks that perform row-wise addition


        int n = leftMatrix.length();
        if (n != rightMatrix.length()) {
            throw new IllegalArgumentException("Addition: different number of vectors");
        }

        List<Runnable> tasks = new ArrayList<>();

        for(int i = 0; i < n; i++){
            //for readability change i to row
            int row = i;


            tasks.add(() -> {
                SharedVector left = leftMatrix.get(row);
                SharedVector right = rightMatrix.get(row);

                left.add(right);
            });

        }
        return null;
    }

    public List<Runnable> createMultiplyTasks() {
        List<Runnable> tasks = new ArrayList<>();
        int numRows = leftMatrix.length();

        for (int i = 0; i < numRows; i++) {
            final int currentRow = i;
            tasks.add(() -> {
                SharedVector leftVec = leftMatrix.get(currentRow);


                leftVec.vecMatMul(rightMatrix);
            });
        }

        return tasks;
    }

    public List<Runnable> createNegateTasks() {
        // TODO: return tasks that negate rows
        List<Runnable> tasks = new ArrayList<>();

        // iterate through all the vectors
        for (int i = 0; i < leftMatrix.length(); i++) {
            SharedVector v = leftMatrix.get(i);

            tasks.add(() -> {
                // multiply by -1 in the vector negate function
                v.negate();
            });
        }

        return tasks;
    }

    public List<Runnable> createTransposeTasks() {
        // TODO: return tasks that transpose rows
        List<Runnable> tasks = new ArrayList<>();

        // goes through all the vectors in the matrix
        for (int i = 0; i < leftMatrix.length(); i++) {

            SharedVector v = leftMatrix.get(i);

            tasks.add(() -> {
                // change the orientation from row to column or vice versa
                v.transpose();
            });
        }

        return tasks;
    }

    public String getWorkerReport() {
        // TODO: return summary of worker activity
        return executor.getWorkerReport();

    }
}
