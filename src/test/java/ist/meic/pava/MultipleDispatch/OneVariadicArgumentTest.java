package ist.meic.pava.MultipleDispatch;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OneVariadicArgumentTest {
    @Test
    @DisplayName("Project Statement First Example")
    public void testProjectStatementFirstExample() {
        String[] results = new String[] {
                "drawing a line on screen!",
                "drawing a circle on screen!",
                "drawing a line on printer!",
                "drawing a circle on printer!",
        };

        Device[] devices = new Device[] { new Screen(), new Printer() };
        Shape[] shapes = new Shape[] { new Line(), new Circle() };
        int i = 0;
        for (Device device : devices) {
            for (Shape shape : shapes) {
                String res = (String) UsingMultipleDispatch.invoke(device, "draw", shape);
                assertEquals(res, results[i]);
                i++;
            }
        }
    }

    class Shape { }
    class Line extends Shape { }
    class Circle extends Shape { }

    class Device {
        public String draw(OneVariadicArgumentTest.Shape... s) {
            return "draw what where?";
        }

        public String draw(OneVariadicArgumentTest.Line... l) {
            return "draw a line where?";
        }

        public String draw(OneVariadicArgumentTest.Circle... c) {
            return "draw a circle where?";
        }
    }

    class Screen extends Device {
        public String draw(OneVariadicArgumentTest.Shape... s) {
            return "draw what on screen?";
        }

        public String draw(OneVariadicArgumentTest.Line... l) {
            return "drawing a line on screen!";
        }

        public String draw(OneVariadicArgumentTest.Circle... c) {
            return "drawing a circle on screen!";
        }
    }

    class Printer extends Device {
        public String draw(OneVariadicArgumentTest.Shape... s) {
            return "draw what on screen?";
        }

        public String draw(OneVariadicArgumentTest.Line... l) {
            return "drawing a line on printer!";
        }

        public String draw(OneVariadicArgumentTest.Circle... c) {
            return "drawing a circle on printer!";
        }
    }
}
