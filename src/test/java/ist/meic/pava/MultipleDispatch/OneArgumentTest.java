package ist.meic.pava.MultipleDispatch;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class OneArgumentTest {
    @ParameterizedTest
    @MethodSource("firstExampleTestCaseProvider")
    public void projectStatementFirstExample(Device dev, Shape shape, String expected) {
        String res = (String) UsingMultipleDispatch.invoke(dev, "draw", shape);
        assertEquals(expected, res);
    }

    @ParameterizedTest
    @MethodSource("firstExampleTestCaseProvider")
    public void projectStatementFirstExampleExtended(Device dev, Shape shape, String expected) {
        String res = (String) ist.meic.pava.MultipleDispatchExtended.UsingMultipleDispatch.invoke(dev, "draw", shape);
        assertEquals(expected, res);
    }

    private static Stream<Arguments> firstExampleTestCaseProvider() {
        return Stream.of(
            Arguments.of(new Screen(), new Line(), "drawing a line on screen!"),
            Arguments.of(new Screen(), new Circle(), "drawing a circle on screen!"),
            Arguments.of(new Printer(), new Line(), "drawing a line on printer!"),
            Arguments.of(new Printer(), new Circle(), "drawing a circle on printer!")
        );
    }

    public static class Shape { }
    public static class Line extends Shape { }
    public static class Circle extends Shape { }

    public static class Device {
        public String draw(Shape s) {
            return "draw what where?";
        }

        public String draw(Line l) {
            return "draw a line where?";
        }

        public String draw(Circle c) {
            return "draw a circle where?";
        }
    }

    public static class Screen extends Device {
        public String draw(Shape s) {
            return "draw what on screen?";
        }

        public String draw(Line l) {
            return "drawing a line on screen!";
        }

        public String draw(Circle c) {
            return "drawing a circle on screen!";
        }
    }

    public static class Printer extends Device {
        public String draw(Shape s) {
            return "draw what on screen?";
        }

        public String draw(Line l) {
            return "drawing a line on printer!";
        }

        public String draw(Circle c) {
            return "drawing a circle on printer!";
        }
    }
}
