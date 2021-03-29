package ist.meic.pava.UsingMultipleDispatchExtended;

import java.lang.reflect.Array;
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
    static Object invoke(Object receiver, String name, Object... args) {
        try {
            // Get class type of each Object in args
            final Class<?>[] argTypes = getArgTypes(args);

            Method bestMethod = Arrays.stream(receiver.getClass().getMethods())
                .filter(method -> method.getName().equals(name))
                .filter(method -> method.getParameterCount() == args.length
                        || (method.isVarArgs() && args.length >= method.getParameterCount() - 1))
                .map(method -> CandidateMethod.create(method, argTypes))
                .filter(Objects::nonNull)
                .min(new CandidateMethodComparator())
                .map(xmethod -> xmethod.method)
                .orElseThrow(() -> new NoSuchMethodException(buildNoSuchMethodExceptionMessage(receiver.getClass(), argTypes)));

            return bestMethod.invoke(receiver, evaluateArguments(bestMethod, argTypes, args));
        } catch (IllegalAccessException
                | InvocationTargetException
                | NoSuchMethodException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static boolean shouldBuildVarargsArray(Method method, Class<?>[] argTypes) {
        // see https://docs.oracle.com/javase/specs/jls/se7/html/jls-15.html#jls-15.12.4.2
        int k = argTypes.length;
        int n = method.getParameterCount();

        if (k != n) {
            return true;
        }

        // k == n
        Class<?>[] paramTypes = method.getParameterTypes();
        return !paramTypes[paramTypes.length - 1].isAssignableFrom(argTypes[argTypes.length - 1]);
    }

    private static Object[] evaluateArguments(Method method, Class<?>[] argTypes, Object... args) throws NegativeArraySizeException {
        if (method.isVarArgs() && shouldBuildVarargsArray(method, argTypes)) {
            int nonVarargsCount = method.getParameterCount() - 1;
            int varargsCount = args.length - nonVarargsCount;

            Class<?> varargsType = method.getParameterTypes()[nonVarargsCount];
            assert varargsType.isArray();
            Object[] varargs = (Object[]) Array.newInstance(varargsType.getComponentType(), varargsCount);
            if (varargsCount != 0) {
                System.arraycopy(args, nonVarargsCount, varargs, 0, varargsCount);
            }

            Object[] newargs = new Object[method.getParameterCount()];
            System.arraycopy(args, 0, newargs, 0, nonVarargsCount);
            newargs[nonVarargsCount] = varargs;

            return newargs;
        }

        return args;
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

            // indirect way of preferring methods without variadic arguments
            comp = Integer.compare(a.method.getParameterCount(), b.method.getParameterCount());
            if (comp != 0) {
                return comp;
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

            // match non-varargs arguments
            int limit = method.isVarArgs() ? paramTypes.length - 1 : paramTypes.length;
            for (int i = 0; i < limit; i++) {
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

            if (method.isVarArgs()) {
                int iStart = paramTypes.length - 1; // index of first vararg argument
                Class<?> paramType = paramTypes[paramTypes.length - 1];
                assert paramType.isArray();
                paramType = paramType.getComponentType();

                for (int i = iStart; i < argTypes.length; i++) {
                    Class<?> argType = argTypes[i];
                    while (argType != paramType && argType != Object.class) {
                        argType = argType.getSuperclass();
                        upcasts[i]++;
                    }

                    if (argType != paramType) {
                        // argType is Object and they are incompatible
                        return null;
                    }
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
