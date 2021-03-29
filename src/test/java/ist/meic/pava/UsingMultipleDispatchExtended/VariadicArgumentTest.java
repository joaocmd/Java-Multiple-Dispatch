package ist.meic.pava.UsingMultipleDispatchExtended;

import org.junit.jupiter.api.DisplayName;
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
            assertEquals(res, "device with 0 shapes");
            res = (String) UsingMultipleDispatch.invoke(d, "draw", dev, shape);
            assertEquals(res, "device with 1 shapes");
            res = (String) UsingMultipleDispatch.invoke(d, "draw", dev, shape, shape);
            assertEquals(res, "device with 2 shapes");
            res = (String) UsingMultipleDispatch.invoke(d, "draw", dev, line, circle);
            assertEquals(res, "device with 2 shapes");
            res = (String) UsingMultipleDispatch.invoke(d, "draw", dev, shape, circle, line);
            assertEquals(res, "device with 3 shapes");
        }
    }

    @Test
    public void drawScreenShapes() {
        String res;

        res = (String) UsingMultipleDispatch.invoke(d, "draw", screen, shape);
        assertEquals(res, "screen with 1 shapes");
        res = (String) UsingMultipleDispatch.invoke(d, "draw", screen, circle);
        assertEquals(res, "screen with 1 shapes");
        res = (String) UsingMultipleDispatch.invoke(d, "draw", screen, shape, circle);
        assertEquals(res, "screen with 2 shapes");
        res = (String) UsingMultipleDispatch.invoke(d, "draw", screen, line, shape);
        assertEquals(res, "screen with 2 shapes");
    }

    @Test
    public void drawScreenLines() {
        String res;

        res = (String) UsingMultipleDispatch.invoke(d, "draw", screen);
        assertEquals(res, "screen with 0 lines");
        res = (String) UsingMultipleDispatch.invoke(d, "draw", screen, line);
        assertEquals(res, "screen with 1 lines");
        res = (String) UsingMultipleDispatch.invoke(d, "draw", screen, line, line);
        assertEquals(res, "screen with 2 lines");
        res = (String) UsingMultipleDispatch.invoke(d, "draw", screen, line, line, line);
        assertEquals(res, "screen with 3 lines");
    }

    @Test
    public void drawLineShapes() {
        String res;

        res = (String) UsingMultipleDispatch.invoke(d, "draw", screen, line, shape, shape);
        assertEquals(res, "screen with line and 2 shapes");
        res = (String) UsingMultipleDispatch.invoke(d, "draw", screen, line, circle, line);
        assertEquals(res, "screen with line and 2 shapes");
        res = (String) UsingMultipleDispatch.invoke(d, "draw", screen, line, circle);
        assertEquals(res, "screen with line and 1 shapes");
        res = (String) UsingMultipleDispatch.invoke(d, "draw", screen, line, line, circle, shape);
        assertEquals(res, "screen with line and 3 shapes");
    }

    @Test
    public void drawScreenCircle() {
        String res;

        res = (String) UsingMultipleDispatch.invoke(d, "draw", screen, circle);
        assertEquals(res, "screen with circle");
    }

    @Test
    public void drawScreenLineLine() {
        String res;

        res = (String) UsingMultipleDispatch.invoke(d, "draw", screen, line, line);
        assertEquals(res, "screen with 2 lines");
    }

    @Test
    public void drawPrinterLineCircleLine() {
        String res;

        res = (String) UsingMultipleDispatch.invoke(d, "draw", printer, line, circle, line);
        assertEquals(res, "printer with line, circle and line");
    }

    @Test
    public void drawShapes() {
        String res;

        res = (String) UsingMultipleDispatch.invoke(d, "draw");
        assertEquals(res, "0 shapes");
        res = (String) UsingMultipleDispatch.invoke(d, "draw", shape);
        assertEquals(res, "1 shapes");
        res = (String) UsingMultipleDispatch.invoke(d, "draw", line, circle);
        assertEquals(res, "2 shapes");
        res = (String) UsingMultipleDispatch.invoke(d, "draw", circle, shape);
        assertEquals(res, "2 shapes");
        res = (String) UsingMultipleDispatch.invoke(d, "draw", circle, shape, line);
        assertEquals(res, "3 shapes");
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
}
