package ist.meic.pava.MultipleDispatchExtended;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Stream;

import ist.meic.pava.MultipleDispatch.MethodSelector;
import ist.meic.pava.MultipleDispatch.PartialOrdering;
import ist.meic.pava.MultipleDispatch.SimpleCandidateMethodFinder;
import ist.meic.pava.MultipleDispatch.SimpleMethodSpecificityComparator;

public class UsingMultipleDispatch {
    private static MethodSelector methodSelector = new MethodSelector(new VarargsAwareMethodComparator(),
            new VarargsAwareCandidateMethodFinder());

    /**
     * Invokes the method with name and args of the receiver. Implements dynamic
     * dispatch for the arguments. Throws RuntimeException on illegal access,
     * missing method, or exception of the invoked method.
     *
     * @param receiver the receiver object, that is, the object that contains the
     *                 method.
     * @param name the name of the method.
     * @param args the arguments to pass to the method.
     * @return the object returned by the method called.
     */
    public static Object invoke(Object receiver, String name, Object... args) {
        try {
            Method method = methodSelector.selectMethod(receiver, name, args);
            return method.invoke(receiver, evaluateArguments(method, args));
        } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean shouldBuildVarargsArray(Method method, Object[] args) {
        // see
        // https://docs.oracle.com/javase/specs/jls/se7/html/jls-15.html#jls-15.12.4.2
        int k = args.length;
        int n = method.getParameterCount();

        if (k != n) {
            return true;
        }

        // k == n
        Class<?> lastParamType = method.getParameterTypes()[n - 1];
        Class<?> lastArgType = args[k - 1].getClass();

        // we don't have access to the declared argument type before passing it to
        // invoke()
        // so we'll deviate from the spec here and only consider this argument as the
        // varargs array
        // if the array type is an exact match (that is if the type isn't equal, we
        // build a new varargs array with this argument in it)
        return !lastParamType.isAssignableFrom(lastArgType);
    }

    private static Object[] evaluateArguments(Method method, Object[] args) {
        if (method.isVarArgs() && shouldBuildVarargsArray(method, args)) {
            int nonVarargsCount = method.getParameterCount() - 1;
            int varargsCount = args.length - nonVarargsCount;

            Class<?> varargsType = method.getParameterTypes()[nonVarargsCount];
            assert varargsType.isArray();
            Object varargsArray = Array.newInstance(varargsType.getComponentType(), varargsCount);
            if (varargsCount != 0) {
                System.arraycopy(args, nonVarargsCount, varargsArray, 0, varargsCount);
            }

            Object[] newargs = new Object[method.getParameterCount()];
            System.arraycopy(args, 0, newargs, 0, nonVarargsCount);
            newargs[nonVarargsCount] = varargsArray;

            return newargs;
        }

        return args;
    }

    private static class VarargsAwareMethodComparator extends SimpleMethodSpecificityComparator {
        public PartialOrdering compare(Method lhs, Method rhs) {
            PartialOrdering superOrd = super.compare(lhs, rhs);
            if (superOrd != PartialOrdering.INCOMPARABLE && superOrd != PartialOrdering.EQUAL) {
                return superOrd;
            }

            // if lhs accepts less (non-varargs) parameters than rhs, then lhs is less
            // specific
            int lhsNormalParamCount = lhs.getParameterCount();
            int rhsNormalParamCount = rhs.getParameterCount();
            if (lhs.isVarArgs())
                lhsNormalParamCount--;
            if (rhs.isVarArgs())
                rhsNormalParamCount--;
            int comp = Integer.compare(lhsNormalParamCount, rhsNormalParamCount);
            if (comp < 0) {
                return PartialOrdering.LESS;
            } else if (comp > 0) {
                return PartialOrdering.GREATER;
            }

            // if lhs accepts all arguments that rhs accepts plus varargs, lhs is less
            // specific
            if (lhs.isVarArgs() && !rhs.isVarArgs()) {
                return PartialOrdering.LESS;
            } else if (!lhs.isVarArgs() && rhs.isVarArgs()) {
                return PartialOrdering.GREATER;
            }

            return PartialOrdering.INCOMPARABLE;
        }
    }

    private static class VarargsAwareCandidateMethodFinder implements MethodSelector.CandidateMethodFinder {
        public Stream<Method> findCandidates(Object receiver, String name, Object[] args) {
            Class<?>[] argTypes = MethodSelector.getObjectTypes(args);

            return Arrays.stream(receiver.getClass().getMethods())
                .filter(SimpleCandidateMethodFinder.NAME_FILTER.apply(name))
                .filter(method -> {
                    Class<?>[] paramTypes = method.getParameterTypes();

                    if ((method.isVarArgs() && paramTypes.length - 1 > argTypes.length)
                            || (!method.isVarArgs() && paramTypes.length != argTypes.length)) {
                        // normal case: argument and parameter count mismatch
                        // if varargs: not enough non-vararg arguments
                        return false;
                    }

                    // Check regular argument compatibility
                    int regularArgCount = method.isVarArgs() ? paramTypes.length - 1 : paramTypes.length;
                    for (int i = 0; i < regularArgCount; i++) {
                        if (!paramTypes[i].isAssignableFrom(argTypes[i])) {
                            return false;
                        }
                    }

                    // Check varargs compatibility
                    if (method.isVarArgs()) {
                        int varargFirstIndex = paramTypes.length - 1;

                        if (varargFirstIndex == argTypes.length) {
                            // varargs method with no varargs supplied
                            return true;
                        }

                        if (paramTypes.length == argTypes.length
                                && paramTypes[varargFirstIndex].isAssignableFrom(argTypes[varargFirstIndex])) {
                            // varargs method with args array supplied
                            return true;
                        }

                        Class<?> varargType = paramTypes[varargFirstIndex].getComponentType();
                        for (int i = varargFirstIndex; i < argTypes.length; i++) {
                            if (!varargType.isAssignableFrom(argTypes[i])) {
                                return false;
                            }
                        }
                    }

                    return true;
                });
        }
    }
}
