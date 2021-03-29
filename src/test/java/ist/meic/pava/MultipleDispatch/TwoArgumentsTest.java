package ist.meic.pava.MultipleDispatch;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class TwoArgumentsTest {
    @ParameterizedTest
    @MethodSource("secondExampleTestCaseProvider")
    public void testProjectStatementSecondExample(Device device, Shape shape, Brush brush, String expected) {
        String res = (String) UsingMultipleDispatch.invoke(device, "draw", shape, brush);
        assertEquals(expected, res);
    }

    @ParameterizedTest
    @MethodSource("secondExampleTestCaseProvider")
    public void testProjectStatementSecondExampleExtended(Device device, Shape shape, Brush brush, String expected) {
        String res = (String) ist.meic.pava.UsingMultipleDispatchExtended.UsingMultipleDispatch.invoke(device, "draw", shape, brush);
        assertEquals(expected, res);
    }

    private static Stream<Arguments> secondExampleTestCaseProvider() {
        return Stream.of(
            Arguments.of(new Screen(), new Line(), new Pencil(), "drawing a line on screen with pencil!"),
            Arguments.of(new Screen(), new Line(), new Crayon(), "drawing a line on screen with crayon!"),
            Arguments.of(new Screen(), new Circle(), new Pencil(), "drawing a circle on screen with pencil!"),
            Arguments.of(new Screen(), new Circle(), new Crayon(), "drawing a circle on screen with what?"),
            Arguments.of(new Printer(), new Line(), new Pencil(), "drawing a line on printer with what?"),
            Arguments.of(new Printer(), new Line(), new Crayon(), "drawing a line on printer with what?"),
            Arguments.of(new Printer(), new Circle(), new Pencil(), "drawing a circle on printer with pencil!"),
            Arguments.of(new Printer(), new Circle(), new Crayon(), "drawing a circle on printer with crayon!")
        );
    }

    public static class Shape {}
    public static class Line extends Shape {}
    public static class Circle extends Shape {}
    public static class Brush {}
    public static class Pencil extends Brush {}
    public static class Crayon extends Brush {}
    public static class Device {
        public String draw(Shape s, Brush b) {
            return "draw what where and with what?";
        }
        public String draw(Line l, Brush b) {
            return "draw a line where and with what?";
        }
        public String draw(Circle c, Brush b) {
            return "draw a circle where and with what?";
        }
    }
    public static class Screen extends Device {
        public String draw(Line l, Brush b) {
            return "draw a line where and with what?";
        }
        public String draw(Line l, Pencil p) {
            return "drawing a line on screen with pencil!";
        }
        public String draw(Line l, Crayon c) {
            return "drawing a line on screen with crayon!";
        }
        public String draw(Circle c, Brush b) {
            return "drawing a circle on screen with what?";
        }
        public String draw(Circle c, Pencil p) {
            return "drawing a circle on screen with pencil!";
        }
    }
    public static class Printer extends Device {
        public String draw(Line l, Brush b) {
            return "drawing a line on printer with what?";
        }
        public String draw(Circle c, Pencil p) {
            return "drawing a circle on printer with pencil!";
        }
        public String draw(Circle c, Crayon r) {
            return "drawing a circle on printer with crayon!";
        }
    }
}
