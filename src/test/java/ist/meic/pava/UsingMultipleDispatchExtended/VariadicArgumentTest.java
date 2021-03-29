package ist.meic.pava.UsingMultipleDispatchExtended;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VariadicArgumentTest {
    @Test
    @DisplayName("Project Statement First Example")
    public void testVarArgs() {
        Device device = new Device();
        Device screen = new Screen();
        Device printer = new Printer();

        Shape shape = new Shape();
        Shape line = new Line();
        Shape circle = new Circle();

        Drawer d = new Drawer();
        String res;

        res = (String) UsingMultipleDispatch.invoke(d, "draw", device);
        assertEquals(res, "device with 0 shapes");
        res = (String) UsingMultipleDispatch.invoke(d, "draw", device, shape);
        assertEquals(res, "device with 1 shapes");
        res = (String) UsingMultipleDispatch.invoke(d, "draw", device, shape, shape);
        assertEquals(res, "device with 2 shapes");
        res = (String) UsingMultipleDispatch.invoke(d, "draw", device, line, circle);
        assertEquals(res, "device with 2 shapes");
        res = (String) UsingMultipleDispatch.invoke(d, "draw", device, circle, shape);
        assertEquals(res, "device with 2 shapes");

        res = (String) UsingMultipleDispatch.invoke(d, "draw", screen);
        assertEquals(res, "screen with 0 lines");
        res = (String) UsingMultipleDispatch.invoke(d, "draw", screen, shape);
        assertEquals(res, "screen with 1 shapes");
        res = (String) UsingMultipleDispatch.invoke(d, "draw", screen, shape, shape);
        assertEquals(res, "screen with 2 shapes");
        res = (String) UsingMultipleDispatch.invoke(d, "draw", screen, circle, shape);
        assertEquals(res, "screen with 2 shapes");

        res = (String) UsingMultipleDispatch.invoke(d, "draw", screen, line);
        assertEquals(res, "screen with 1 lines");
        res = (String) UsingMultipleDispatch.invoke(d, "draw", screen, line, line, line);
        assertEquals(res, "screen with 3 lines");

        res = (String) UsingMultipleDispatch.invoke(d, "draw", screen, line, circle);
        assertEquals(res, "screen with line and 1 shapes");

        res = (String) UsingMultipleDispatch.invoke(d, "draw", screen, circle);
        assertEquals(res, "screen with circle");

        res = (String) UsingMultipleDispatch.invoke(d, "draw", screen, line, line);
        assertEquals(res, "screen with TWO lines");

        res = (String) UsingMultipleDispatch.invoke(d, "draw", line, line);
        assertEquals(res, "2 shapes");
    }

    static class Drawer {
        public String draw(VariadicArgumentTest.Device d, VariadicArgumentTest.Shape... s) {
            return String.format("device with %d shapes", s.length);
        }
        public String draw(VariadicArgumentTest.Screen d, VariadicArgumentTest.Shape... s) {
            return String.format("screen with %d shapes", s.length);
        }
        public String draw(VariadicArgumentTest.Screen d, VariadicArgumentTest.Line... l) {
            return String.format("screen with %d lines", l.length);
        }
        public String draw(VariadicArgumentTest.Screen d, VariadicArgumentTest.Line l, VariadicArgumentTest.Shape... s) {
            return String.format("screen with line and %d shapes", s.length);
        }
        public String draw(VariadicArgumentTest.Screen d, VariadicArgumentTest.Circle c) {
            return "screen with circle";
        }
        public String draw(VariadicArgumentTest.Screen d, VariadicArgumentTest.Line l1, VariadicArgumentTest.Line l2) {
            return "screen with TWO lines";
        }
        public String draw(VariadicArgumentTest.Shape... s) {
            return String.format("%d shapes", s.length);
        }
    }

    static class Shape { }
    static class Line extends Shape { }
    static class Circle extends Shape { }

    static class Device {}
    static class Screen extends Device {}
    static class Printer extends Device {}
}
