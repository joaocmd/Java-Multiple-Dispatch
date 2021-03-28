package ist.meic.pava.MultipleDispatch;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TwoArgumentsTest {
    @Test
    @DisplayName("Project Statement Second Example")
    public void secondExample() {
        String[] results = new String[] {
                "drawing a line on screen with pencil!",
                "drawing a line on screen with crayon!",
                "drawing a circle on screen with pencil!",
                "drawing a circle on screen with what?",
                "drawing a line on printer with what?",
                "drawing a line on printer with what?",
                "drawing a circle on printer with pencil!",
                "drawing a circle on printer with crayon!"
        };

        Device[] devices = new Device[] { new Screen(), new Printer() };
        Shape[] shapes = new Shape[] { new Line(), new Circle() };
        Brush[] brushes = new Brush[] { new Pencil(), new Crayon() };

        int i = 0;
        for (Device device : devices) {
            for (Shape shape : shapes) {
                for (Brush brush : brushes) {
                    String res = (String) UsingMultipleDispatch.invoke(device, "draw", shape, brush);
                    assertEquals(res, results[i]);
                    i++;
                }
            }
        }
    }

    class Shape {
    }
    class Line extends Shape {
    }
    class Circle extends Shape {
    }
    class Brush {
    }
    class Pencil extends Brush {
    }
    class Crayon extends Brush {
    }
    class Device {
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
    class Screen extends Device {
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
    class Printer extends Device {
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
