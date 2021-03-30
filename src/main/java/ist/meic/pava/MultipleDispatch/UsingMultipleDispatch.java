package ist.meic.pava.MultipleDispatch;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class UsingMultipleDispatch {
    private static final MethodSelector methodSelector = new MethodSelector(new MethodSpecificityComparator());

    /**
     * Invokes the method with name and args of the receiver. Implements dynamic dispatch for the arguments.
     * Throws RunTimeException on illegal access, missing method, or exception of the invoked method.
     * @param receiver  the receiver object, that is, the object that contains the method
     * @param name      the name of the method
     * @param args      the arguments to pass to the method
     * @return          the object returned by the method called
     */
    public static Object invoke(Object receiver, String name, Object... args) {
        try {
            Method method = methodSelector.selectMethod(receiver, name, args);
            return method.invoke(receiver, args);
        } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static class MethodSpecificityComparator implements PartialComparator<Method> {
        private static TypeSpecificityComparator typeSpecificityComparator = new TypeSpecificityComparator();

        public PartialOrdering compare(Method lhs, Method rhs) {
            if (lhs == rhs) {
                return PartialOrdering.EQUAL;
            }

            // if the declaring class of lhs is a subtype of the declaring class of rhs, then lhs is less specific than rhs
            PartialOrdering receiverPartialOrd = typeSpecificityComparator.compare(lhs.getClass(), rhs.getClass());
            if (receiverPartialOrd != PartialOrdering.UNCOMPARABLE && receiverPartialOrd != PartialOrdering.EQUAL) {
                return receiverPartialOrd;
            }

            Class<?>[] lhsParamTypes = lhs.getParameterTypes();
            Class<?>[] rhsParamTypes = rhs.getParameterTypes();
            for (int i = 0; i < lhsParamTypes.length; i++) {
                PartialOrdering partialOrd = typeSpecificityComparator.compare(lhsParamTypes[i], rhsParamTypes[i]);

                if (partialOrd != PartialOrdering.UNCOMPARABLE && partialOrd != PartialOrdering.EQUAL) {
                    return partialOrd;
                }
            }

            return PartialOrdering.UNCOMPARABLE;
        }
    }
}
