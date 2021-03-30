package ist.meic.pava.MultipleDispatch;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Predicate;

public class UsingMultipleDispatch {
    /**
     * Invokes the method with name and args of the receiver. Implements dynamic dispatch for the arguments.
     * Throws RunTimeException on illegal access, missing method, or exception of the invoked method.
     * @param receiver  the receiver object, that is, the object that contains the method
     * @param name      the name of the method
     * @param args      the arguments to pass to the method
     * @return          the object returned by the method called
     */
    public final static Object invoke(Object receiver, String name, Object... args) {
        try {
            // Get class type of each Object in args
            final Class<?>[] argTypes = Util.getArgTypes(args);

            Method bestMethod = Arrays.stream(receiver.getClass().getMethods())
                .filter(candidateMethodFilter(name, argTypes))
                .max(methodComparator())
                .orElseThrow(() -> Util.buildNoSuchMethodException(receiver.getClass(), argTypes));

            return bestMethod.invoke(receiver, args);
        } catch (IllegalAccessException
                | InvocationTargetException
                | NoSuchMethodException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static final Comparator<Method> methodComparator() {
        return new Comparator<Method>(){
            PartialComparator<Method> partialComp = methodPartialComparator();

            public int compare(Method lhs, Method rhs) {
                PartialOrdering partialOrd = partialComp.compare(lhs, rhs);

                if (partialOrd == PartialOrdering.UNCOMPARABLE) {
                    // enforce some total order
                    return lhs.toString().compareTo(rhs.toString());
                } else {
                    return partialOrd.asTotalOrdering();
                }
            }
        };
    }

    protected static Predicate<Method> candidateMethodFilter(String name, Class<?>[] argTypes) {
        return new Predicate<Method>(){
            public boolean test(Method m) {
                if (m.getName() != name || argTypes.length != m.getParameterCount()) {
                    return false;
                }

                Class<?>[] paramTypes = m.getParameterTypes();
                for (int i = 0; i < paramTypes.length; i++) {
                    if (!paramTypes[i].isAssignableFrom(argTypes[i])) {
                        return false;
                    }
                }

                return true;
            }
        };
    }

    protected static PartialComparator<Method> methodPartialComparator() {
        return new MethodSpecificityComparator();
    }
}
