package ist.meic.pava.MultipleDispatchExtended;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class InterfaceTest {
    @ParameterizedTest
    @MethodSource("secondExampleTestCaseProvider")
    public void testProjectStatementSecondExampleExtended(Device device, Shape shape, Color brush, String expected) {
        String res = (String) UsingMultipleDispatch.invoke(device, "draw", shape, brush);
        assertEquals(expected, res);
    }

    private static Stream<Arguments> secondExampleTestCaseProvider() {
        return Stream.of(
            Arguments.of(new Screen(), new Circle(), new Blue(), "drawing a circle on screen in blue"),
            Arguments.of(new Screen(), new Circle(), new Red(), "drawing a circle on screen in red"),
            Arguments.of(new Screen(), new Line(), new Blue(), "drawing a line on screen in blue"),
            Arguments.of(new Screen(), new Line(), new Red(), "drawing a line on screen in some generic color"),
            Arguments.of(new Printer(), new Circle(), new Blue(), "drawing a circle on printer in some generic color"),
            Arguments.of(new Printer(), new Circle(), new Red(), "drawing a circle on printer in some generic color"),
            Arguments.of(new Printer(), new Line(), new Blue(), "drawing a line on printer in blue"),
            Arguments.of(new Printer(), new Line(), new Red(), "drawing a line on printer in red")
        );
    }

    public interface Shape {}
    public static class Line implements Shape {}
    public static class Circle implements Shape {}

    public interface Color {}
    public static class Blue implements Color {}
    public static class Red implements Color {}

    public static class Device {
        public String draw(Shape s, Color b) {
            return "draw what where and in some generic color";
        }
        public String draw(Line c, Color b) {
            return "draw a line where and in some generic color";
        }
        public String draw(Circle l, Color b) {
            return "draw a circle where and in some generic color";
        }
    }
    public static class Screen extends Device {
        public String draw(Line c, Color b) {
            return "drawing a line on screen in some generic color";
        }
        public String draw(Line c, Blue p) {
            return "drawing a line on screen in blue";
        }
        public String draw(Circle l, Color b) {
            return "draw a circle where and in some generic color";
        }
        public String draw(Circle l, Blue p) {
            return "drawing a circle on screen in blue";
        }
        public String draw(Circle l, Red c) {
            return "drawing a circle on screen in red";
        }
    }
    public static class Printer extends Device {
        public String draw(Line c, Blue p) {
            return "drawing a line on printer in blue";
        }
        public String draw(Line c, Red r) {
            return "drawing a line on printer in red";
        }
        public String draw(Circle l, Color b) {
            return "drawing a circle on printer in some generic color";
        }
    }
}
