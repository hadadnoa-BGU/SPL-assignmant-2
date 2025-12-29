import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Method;
import java.util.List;
import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

import memory.SharedMatrix;
import parser.ComputationNode;
import parser.ComputationNodeType;
import spl.lae.LinearAlgebraEngine;

public class LinearAlgebraEngineTest {
    LinearAlgebraEngine engine = new LinearAlgebraEngine(4);
    // --- Reflection Helpers to access private members ---
    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Object invokePrivateMethod(Object target, String methodName) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName);
        method.setAccessible(true);
        return method.invoke(target);
    }
    @Test
    void testCreateAddTasks() throws Exception {
        double[][] data = {{1.0, 2.0}, {3.0, 4.0}};
        SharedMatrix mat1 = new SharedMatrix(data);
        SharedMatrix mat2 = new SharedMatrix(data);
        setPrivateField(engine, "leftMatrix", mat1);
        setPrivateField(engine, "rightMatrix", mat2);
        // 3. Invoke private method
        @SuppressWarnings("unchecked")
        List<Runnable> tasks = (List<Runnable>) invokePrivateMethod(engine, "createNegateTasks");

        // 4. Verify
        assertNotNull(tasks);
        assertEquals(2, tasks.size(), "Should create 1 task per row for negation");
    }

 
@Test
    void testCreateMultiplyTasks() throws Exception {
        // 1. Setup Data (2x2 matrices)
        double[][] data = {{1.0, 2.0}, {3.0, 4.0}};
        SharedMatrix mat1 = new SharedMatrix(data);
        SharedMatrix mat2 = new SharedMatrix(data);

        // 2. Inject matrices
        setPrivateField(engine, "leftMatrix", mat1);
        setPrivateField(engine, "rightMatrix", mat2);

        // 3. Invoke private method
        @SuppressWarnings("unchecked")
        List<Runnable> tasks = (List<Runnable>) invokePrivateMethod(engine, "createMultiplyTasks");

        // 4. Verify
        assertNotNull(tasks);
        assertEquals(2, tasks.size(), "Should create 1 task per row for multiplication");
    }

    @Test
    void testCreateNegateTasks() throws Exception {
        // 1. Setup Data
        double[][] data = {{1.0, 2.0}, {3.0, 4.0}};
        SharedMatrix mat = new SharedMatrix(data);

        // 2. Inject matrix (only leftMatrix is needed for Unary ops)
        setPrivateField(engine, "leftMatrix", mat);

        // 3. Invoke private method
        @SuppressWarnings("unchecked")
        List<Runnable> tasks = (List<Runnable>) invokePrivateMethod(engine, "createNegateTasks");

        // 4. Verify
        assertNotNull(tasks);
        assertEquals(2, tasks.size(), "Should create 1 task per row for negation");
    }

    @Test
    void testCreateTransposeTasks() throws Exception {
        // 1. Setup Data
        double[][] data = {{1.0, 2.0}, {3.0, 4.0}};
        SharedMatrix mat = new SharedMatrix(data);

        // 2. Inject matrix
        setPrivateField(engine, "leftMatrix", mat);

        // 3. Invoke private method
        @SuppressWarnings("unchecked")
        List<Runnable> tasks = (List<Runnable>) invokePrivateMethod(engine, "createTransposeTasks");

        // 4. Verify
        assertNotNull(tasks);
        assertEquals(2, tasks.size(), "Should create 1 task per row for transpose");
    }

    @Test
    void testLoadAndCompute() {
        // This integration test verifies that matrices are loaded and tasks submitted
        // We will perform a simple ADD operation: [[1]] + [[2]] = [[3]]
        
        // 1. Create Leaf Nodes
        double[][] d1 = {{1.0}};
        double[][] d2 = {{2.0}};
        
        // Mocking/Creating nodes (assuming you have a constructor or setter for ComputationNode)
        ComputationNode leaf1 = new ComputationNode(ComputationNodeType.MATRIX, null);
        leaf1.resolve(d1);

        ComputationNode leaf2 = new ComputationNode(ComputationNodeType.MATRIX, null);
        leaf2.resolve(d2);

        // 2. Create Parent Node (ADD)
        ComputationNode addNode = new ComputationNode(ComputationNodeType.ADD, List.of(leaf1, leaf2));
    

        // 3. Run loadAndCompute
        engine.loadAndCompute(addNode);

        // 4. Verify Result
        // Since loadAndCompute writes to 'leftMatrix' inside the engine, we check that
        // We use Reflection to read the result back
        try {
            Field leftField = LinearAlgebraEngine.class.getDeclaredField("leftMatrix");
            leftField.setAccessible(true);
            SharedMatrix resultMatrix = (SharedMatrix) leftField.get(engine);
            
            assertEquals(3.0, resultMatrix.get(0).get(0), "1.0 + 2.0 should equal 3.0");
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

}
