package ist.meic.pava.MultipleDispatch;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class UsingMultipleDispatch {
    private static final MethodSelector methodSelector = new MethodSelector(new SimpleMethodSpecificityComparator());

    /**
     * Invokes the method with name and args of the receiver. Implements dynamic dispatch for the arguments.
     * Throws RunTimeException on illegal access, missing method, or exception of the invoked method.
     * @param receiver the receiver object, that is, the object that contains the method.
     * @param name the name of the method.
     * @param args the arguments to pass to the method.
     * @return the object returned by the method called.
     */
    public static Object invoke(Object receiver, String name, Object... args) {
        try {
            Method method = methodSelector.selectMethod(receiver, name, args);
            return method.invoke(receiver, args);
        } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

}
