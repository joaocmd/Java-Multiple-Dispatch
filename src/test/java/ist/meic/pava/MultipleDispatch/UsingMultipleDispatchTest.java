package ist.meic.pava.MultipleDispatch;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class Shape { }
class Line extends Shape { }
class Circle extends Shape { }

class Device {
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

class Screen extends Device {
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

class Printer extends Device {
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


public class UsingMultipleDispatchTest {
    @Test
    @DisplayName("Project Statement First Example")
    public void testProjectStatementFirst() {
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
}
