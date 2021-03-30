package ist.meic.pava.MultipleDispatch;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class UsingMultipleDispatch {
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
            // Get class type of each Object in args
            final Class<?>[] argTypes = getArgTypes(args);

            Method bestMethod = Arrays.stream(receiver.getClass().getMethods())
                .filter(candidateMethodFilter(name, argTypes))
                .max(new MethodSpecificityComparator())
                .orElseThrow(() -> new NoSuchMethodException(buildNoSuchMethodExceptionMessage(receiver.getClass(), argTypes)));

            return bestMethod.invoke(receiver, args);
        } catch (IllegalAccessException
                | InvocationTargetException
                | NoSuchMethodException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static Predicate<Method> candidateMethodFilter(String name, Class<?>[] argTypes) {
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

    private static class MethodSpecificityComparator implements Comparator<Method> {
        public int compare(Method lhs, Method rhs) {
            Class<?>[] lhsParamTypes = lhs.getParameterTypes();
            Class<?>[] rhsParamTypes = rhs.getParameterTypes();
            TypeSpecifictyComparator typeComp = new TypeSpecifictyComparator();

            for (int i = 0; i < lhsParamTypes.length; i++) {
                Optional<Integer> partialOrd = typeComp.compare(lhsParamTypes[i], rhsParamTypes[i]);

                if (partialOrd.isPresent() && partialOrd.get() != 0) {
                    return partialOrd.get();
                }
            }

            // we really can't do any better, just enforce any total order from here
            return lhs.toString().compareTo(rhs.toString());
        }
    }

    private static class TypeSpecifictyComparator {
        public Optional<Integer> compare(Class<?> lhs, Class<?> rhs) {
            if (lhs == rhs) {
                return Optional.of(0);
            }

            if (lhs.isAssignableFrom(rhs)) {
                return Optional.of(-1); // rhs is more specific
            } else if (rhs.isAssignableFrom(lhs)) {
                return Optional.of(1); // lhs is more specific
            } else {
                // equal specificity but not the same: incomparable
                return Optional.empty();
            }
        }
    }

    private static Class<?>[] getArgTypes(Object... args) {
        List<Class<?>> argTypesList = Arrays.stream(args).map(Object::getClass).collect(Collectors.toList());
        Class<?>[] argTypes = new Class[argTypesList.size()];
        argTypes = argTypesList.toArray(argTypes);
        return argTypes;
    }

    private static String buildNoSuchMethodExceptionMessage(Class<?> receiverType, Class<?>[] argTypes) {
        return receiverType.getName() +
                '(' +
                Arrays.stream(argTypes)
                        .map(Class::getName)
                        .collect(Collectors.joining(", ")) +
                ')';
    }
}
