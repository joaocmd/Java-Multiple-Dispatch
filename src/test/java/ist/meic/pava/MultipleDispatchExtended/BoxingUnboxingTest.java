package ist.meic.pava.MultipleDispatchExtended;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BoxingUnboxingTest {

    @Test
    public void projectStatementFirstExampleExtended() {
        Functions functions = new Functions();
        int a = 1;
        Integer b = 1;
        assertEquals("int", UsingMultipleDispatch.invoke(functions, "f", a));
        assertEquals("int", UsingMultipleDispatch.invoke(functions, "f", b));
        assertEquals("varargs boxed", UsingMultipleDispatch.invoke(functions, "f", b, a, b));
        assertEquals("varargs unboxed", UsingMultipleDispatch.invoke(functions, "g", b, a, b));
    }

    public static class Functions {
        public String f(int i) { return "int"; }
        public String f(Integer i) { return "Integer"; }
        public String f(int i, Integer... varargs) { return "varargs boxed"; }
        public String g(int i, int... varargs) { return "varargs unboxed"; }
        public String g(int i, Integer... varargs) { return "varargs boxed"; }
    }
}