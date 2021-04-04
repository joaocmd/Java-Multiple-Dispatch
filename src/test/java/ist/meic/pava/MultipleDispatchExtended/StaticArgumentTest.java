package ist.meic.pava.MultipleDispatchExtended;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StaticArgumentTest {

    @Test
    public void projectStatementFirstExampleExtended() {
        Integer res = (Integer) UsingMultipleDispatch.invoke(Functions.class, "f", 5);
        assertEquals(5, res);
    }

    public static class Functions {
        public static int f(Integer i) {
            return i;
        }
    }
}
