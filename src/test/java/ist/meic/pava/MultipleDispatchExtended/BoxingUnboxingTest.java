package ist.meic.pava.MultipleDispatchExtended;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class BoxingUnboxingTest {

    @Test
    @MethodSource("firstExampleTestCaseProvider")
    public void projectStatementFirstExampleExtended() {
        Functions functions = new Functions();
        int a = 1;
        Integer b = 1;
        assertDoesNotThrow(() -> UsingMultipleDispatch.invoke(functions, "f", a));
        assertDoesNotThrow(() -> UsingMultipleDispatch.invoke(functions, "f", b));
    }

    public static class Functions {
        public void f(int i) { }
    }
}
