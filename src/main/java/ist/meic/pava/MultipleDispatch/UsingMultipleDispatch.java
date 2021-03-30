package ist.meic.pava.MultipleDispatch;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
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
                .filter(method -> method.getName().equals(name))
                .filter(method -> method.getParameterCount() == args.length)
                .map(method -> CandidateMethod.create(method, argTypes))
                .filter(Objects::nonNull)
                .min(new CandidateMethodComparator())
                .map(xmethod -> xmethod.method)
                .orElseThrow(() -> new NoSuchMethodException(buildNoSuchMethodExceptionMessage(receiver.getClass(), argTypes)));

            return bestMethod.invoke(receiver, args);
        } catch (IllegalAccessException
                | InvocationTargetException
                | NoSuchMethodException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
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

    private static class CandidateMethodComparator implements Comparator<CandidateMethod> {
        public int compare(CandidateMethod a, CandidateMethod b) {
            int comp = Integer.compare(a.upcastCount(), b.upcastCount());
            if (comp != 0) {
                return comp;
            }

            for (int i = 0; i < a.upcasts.length && i < b.upcasts.length; i++) {
                comp = Integer.compare(a.upcasts[i], b.upcasts[i]);
                if (comp != 0) {
                    return comp;
                }
            }

            // we really can't do any better, just enforce any total order from here
            return a.method.toString().compareTo(b.method.toString());
        }
    }

    private static class CandidateMethod {
        public Method method;
        public int[] upcasts;

        private CandidateMethod(Method method, int[] upcasts) {
            this.method = method;
            this.upcasts = upcasts;
        }

        static CandidateMethod create(Method method, Class<?>[] argTypes) {
            int[] upcasts = new int[argTypes.length];
            Class<?>[] paramTypes = method.getParameterTypes();

            for (int i = 0; i < paramTypes.length; i++) {
                Class<?> argType = argTypes[i];
                Class<?> paramType = paramTypes[i];
                while (argType != paramType && argType != Object.class) {
                    argType = argType.getSuperclass();
                    upcasts[i]++;
                }

                if (argType != paramType) {
                    // argType is Object and they are incompatible
                    return null;
                }
            }

            return new CandidateMethod(method, upcasts);
        }

        public int upcastCount() {
            return Arrays.stream(this.upcasts)
                .reduce(0, Integer::sum);
        }

        @Override
        public String toString() {
            return this.method.toString();
        }
    }
}
