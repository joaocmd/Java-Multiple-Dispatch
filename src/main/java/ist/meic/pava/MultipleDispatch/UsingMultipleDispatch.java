package ist.meic.pava.MultipleDispatch;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Implements dynamic dispatch on the arguments of a method call.
 *
 * Not guaranteed to autobox/unbox arguments, nor to support variadic method calls.
 * @see SimpleMethodSpecificityComparator for determining which method will be called.
 * @see MethodSelector for determining which method will be called when there are incomparable methods present.
 */
public class UsingMultipleDispatch {
    private static final MethodSelector methodSelector = new MethodSelector(new SimpleMethodSpecificityComparator());

    /**
     * Invokes a method by receiver, name and arguments.
     * Implements dynamic dispatch on the arguments (Java only does it for the receiver).
     *
     * @param receiver receiver object, where method would be called.
     * @param name name of the method to call.
     * @param args call arguments.
     * @return object returned by the method call.
     * @throws RuntimeException when any exceptions occur when invoking the method, and when the method does not exist/is inaccessible
     */
    public static Object invoke(Object receiver, String name, Object... args) {
        try {
            Method method = methodSelector.selectMethod(receiver, name, args);
            return method.invoke(receiver, args);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

}
