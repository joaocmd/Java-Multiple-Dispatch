package ist.meic.pava.MultipleDispatchExtended;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VariadicArgumentTest {
    Device device = new Device();
    Device screen = new Screen();
    Device printer = new Printer();

    Shape shape = new Shape();
    Shape line = new Line();
    Shape circle = new Circle();

    Drawer d = new Drawer();

    @Test
    public void drawDevShapes() {
        String res;

        for (Device dev : new Device[] {device, printer}) {
            res = (String) UsingMultipleDispatch.invoke(d, "draw", dev);
            assertEquals("device with 0 shapes", res);
            res = (String) UsingMultipleDispatch.invoke(d, "draw", dev, shape);
            assertEquals("device with 1 shapes", res);
            res = (String) UsingMultipleDispatch.invoke(d, "draw", dev, shape, shape);
            assertEquals("device with 2 shapes", res);
            res = (String) UsingMultipleDispatch.invoke(d, "draw", dev, line, circle);
            assertEquals("device with 2 shapes", res);
            res = (String) UsingMultipleDispatch.invoke(d, "draw", dev, shape, circle, line);
            assertEquals("device with 3 shapes", res);
        }
    }

    @Test
    public void drawScreenShapes() {
        String res;

        res = (String) UsingMultipleDispatch.invoke(d, "draw", screen, shape);
        assertEquals("screen with 1 shapes", res);
        res = (String) UsingMultipleDispatch.invoke(d, "draw", screen, shape, line);
        assertEquals("screen with 2 shapes", res);
        res = (String) UsingMultipleDispatch.invoke(d, "draw", screen, shape, shape);
        assertEquals("screen with 2 shapes", res);
        res = (String) UsingMultipleDispatch.invoke(d, "draw", screen, circle, shape);
        assertEquals("screen with 2 shapes", res);
    }

    @Test
    public void drawScreenLines() {
        String res;

        res = (String) UsingMultipleDispatch.invoke(d, "draw", screen);
        assertEquals("screen with 0 lines", res);
        res = (String) UsingMultipleDispatch.invoke(d, "draw", screen, line, line);
        assertEquals("screen with 2 lines", res);
        res = (String) UsingMultipleDispatch.invoke(d, "draw", screen, line, line, line);
        assertEquals("screen with 3 lines", res);
    }

    @Test
    public void drawLineShapes() {
        String res;

        res = (String) UsingMultipleDispatch.invoke(d, "draw", screen, line);
        assertEquals("screen with line and 0 shapes", res);
        res = (String) UsingMultipleDispatch.invoke(d, "draw", screen, line, circle);
        assertEquals("screen with line and 1 shapes", res);
        res = (String) UsingMultipleDispatch.invoke(d, "draw", screen, line, shape, shape);
        assertEquals("screen with line and 2 shapes", res);
        res = (String) UsingMultipleDispatch.invoke(d, "draw", screen, line, circle, line);
        assertEquals("screen with line and 2 shapes", res);
        res = (String) UsingMultipleDispatch.invoke(d, "draw", screen, line, line, circle, shape);
        assertEquals("screen with line and 3 shapes", res);
    }

    @Test
    public void drawScreenCircle() {
        String res;

        res = (String) UsingMultipleDispatch.invoke(d, "draw", screen, circle);
        assertEquals("screen with circle", res);
    }

    @Test
    public void drawScreenLineLine() {
        String res;

        res = (String) UsingMultipleDispatch.invoke(d, "draw", screen, line, line);
        assertEquals("screen with 2 lines", res);
    }

    @Test
    public void drawPrinterLineCircleLine() {
        String res;

        res = (String) UsingMultipleDispatch.invoke(d, "draw", printer, line, circle, line);
        assertEquals("printer with line, circle and line", res);
    }

    @Test
    public void drawShapes() {
        String res;

        res = (String) UsingMultipleDispatch.invoke(d, "draw");
        assertEquals("0 shapes", res);
        res = (String) UsingMultipleDispatch.invoke(d, "draw", shape);
        assertEquals("1 shapes", res);
        res = (String) UsingMultipleDispatch.invoke(d, "draw", line, circle);
        assertEquals("2 shapes", res);
        res = (String) UsingMultipleDispatch.invoke(d, "draw", circle, shape);
        assertEquals("2 shapes", res);
        res = (String) UsingMultipleDispatch.invoke(d, "draw", circle, shape, line);
        assertEquals("3 shapes", res);
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
            return "screen with 2 lines";
        }
        public String draw(VariadicArgumentTest.Printer d, VariadicArgumentTest.Line l1, VariadicArgumentTest.Circle c, VariadicArgumentTest.Line l2) {
            return "printer with line, circle and line";
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

    @Test
    public void varargsPassArrayTest() {
        VarargsPassArray instance = new VarargsPassArray();

        // what we really want to do is to compare the version that passes
        // through multiple dispatch with the one that does not, but that's hard
        // to read to we compare everything twice instead
        assertEquals(0, instance.countArgs());
        assertEquals(1, instance.countArgs("asd"));
        assertEquals(2, instance.countArgs("asd", "asd"));
        assertEquals(2, instance.countArgs("asd", Integer.valueOf(1)));
        assertEquals(2, instance.countArgs((Object[]) new String[]{"asd", "asd"}));

        assertEquals(0, (Integer) UsingMultipleDispatch.invoke(instance, "countArgs"));
        assertEquals(1, (Integer) UsingMultipleDispatch.invoke(instance, "countArgs", "asd"));
        assertEquals(2, (Integer) UsingMultipleDispatch.invoke(instance, "countArgs", "asd", "asd"));
        assertEquals(2, (Integer) UsingMultipleDispatch.invoke(instance, "countArgs", "asd", Integer.valueOf(1)));
        assertEquals(2, (Integer) UsingMultipleDispatch.invoke(instance, "countArgs", (Object[]) new String[]{"asd", "asd"}));

        // The only case where behavior does not match a regular invocation exactly
        // invoke() sees the String[] as a String[] (not Object), no matter how much we cast it
        assertEquals(1, instance.countArgs((Object) new String[]{"asd", "asd"}));
        assertEquals(2, (Integer) UsingMultipleDispatch.invoke(instance, "countArgs", (Object) new String[]{"asd", "asd"}));
    }

    static class VarargsPassArray {
        public Integer countArgs(Object... args) {
            return Integer.valueOf(args.length);
        }
    }
}
