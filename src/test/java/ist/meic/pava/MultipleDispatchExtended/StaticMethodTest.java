package ist.meic.pava.MultipleDispatchExtended;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StaticMethodTest {
    @Test
    public void test() {
        Integer res = (Integer) UsingMultipleDispatch.invokeStatic(Functions.class, "f", (Object)5);
        assertEquals(1, res);

        res = (Integer) UsingMultipleDispatch.invokeStatic(Functions.class, "f", (Object)"2");
        assertEquals(2, res);
    }

    public static class Functions {
        public static int f(Integer i) {
            return 1;
        }
        public static int f(String s) {
            return 2;
        }
    }
}
