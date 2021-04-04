package ist.meic.pava.MultipleDispatchExtended;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.stream.Stream;

import ist.meic.pava.MultipleDispatch.MethodSelector;
import ist.meic.pava.MultipleDispatch.PartialOrdering;
import ist.meic.pava.MultipleDispatch.SimpleCandidateMethodFinder;
import ist.meic.pava.MultipleDispatch.SimpleMethodSpecificityComparator;

/**
 * Implements dynamic dispatch on the arguments of a method call.
 *
 * Not guaranteed to autobox/unbox arguments. Supports calling methods with
 * variadic arguments with the same syntax as regular calls except for one case:
 * when passing exactly one array (T[]) after the non variadic arguments. In a
 * regular call, this array could be casted to the vararg type and would be
 * considered one of the varargs. When using this class, as long as T is
 * assignable to the vararg type, the array T[] will be treated as the container
 * of all variadic arguments.
 *
 * @see ist.meic.pava.MultipleDispatchExtended.VariadicArgumentTest for an
 *      example of this edge case (varargsPassArrayTest)
 */
public class UsingMultipleDispatch {
    private static MethodSelector staticMethodSelector = new MethodSelector(new VarargsAwareMethodComparator(),
            new StaticVarargsAwareCandidateMethodFinder());
    private static MethodSelector nonStaticMethodSelector = new MethodSelector(new VarargsAwareMethodComparator(),
            new NonStaticVarargsAwareCandidateMethodFinder());

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
            Method method = nonStaticMethodSelector.selectMethod(receiver.getClass(), name, args);
            return method.invoke(receiver, evaluateArguments(method, args));
        } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Similar to the first version, but is intended to be used to call static methods.
     * @param receiverClass the class of the method.
     * @param name the name of the method.
     * @param args the arguments to pass to the method.
     * @return the object returned by the method called.
     */
    public static Object invokeStatic(Class<?> receiverClass, String name, Object... args) {
        try {
            Method method = staticMethodSelector.selectMethod(receiverClass, name, args);
            return method.invoke(null, evaluateArguments(method, args));
        } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Determines if the arguments list when calling a given method must be
     * rewritten as per JLS 15.12.4.2 in variadic method calls.
     *
     * @see https://docs.oracle.com/javase/specs/jls/se7/html/jls-15.html#jls-15.12.4.2
     * @param method method to call
     * @param args   arguments list
     * @return true if the arguments list has to be rewritten
     */
    private static boolean shouldBuildVarargsArray(Method method, Object[] args) {
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

    /**
     * Transforms the argument list for calling a given method as per JLS 15.12.4.2
     * to support variadic method calls.
     *
     * @see https://docs.oracle.com/javase/specs/jls/se7/html/jls-15.html#jls-15.12.4.2
     * @param method method to call
     * @param args   arguments list
     * @return transformed arguments list as per JLS 15.12.4.2
     */
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

    /**
     * The VarargsAwareMethodComparator extends the ordering defined by
     * SimpleMethodSpecificityComparator with variadic arguments-specific details.
     *
     * During the SimpleMethodSpecificityComparator comparison, the parameter lists
     * for both methods are normalized when they support a variable number of
     * parameters of type T: the new parameters list will contain all non-varargs
     * parameters, and at least as many Ts as necessary in the end to be the same
     * size as the other method's parameter list.
     *
     * In particular, out of two equally specific methods m1 and m2 as per the
     * simple comparator,if m1 has less (non-vargs) formal parameters than m2,
     * it is considered less specific than m2.
     */
    public static class VarargsAwareMethodComparator extends SimpleMethodSpecificityComparator {
        @Override
        public PartialOrdering compare(Method lhs, Method rhs) {
            PartialOrdering superOrd = super.compare(lhs, rhs);
            if (superOrd != PartialOrdering.EQUAL) {
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

            return PartialOrdering.INCOMPARABLE;
        }

        @Override
        protected Class<?>[][] getParameterTypes(Method m1, Method m2) {
            Class<?>[] parameterTypes1 = m1.getParameterTypes();
            Class<?>[] parameterTypes2 = m2.getParameterTypes();

            // treat varargs as any other args
            if (m1.isVarArgs()) {
                parameterTypes1[parameterTypes1.length - 1] = parameterTypes1[parameterTypes1.length - 1]
                        .getComponentType();
            }
            if (m2.isVarArgs()) {
                parameterTypes2[parameterTypes2.length - 1] = parameterTypes2[parameterTypes2.length - 1]
                        .getComponentType();
            }

            // expand method arguments list to match size of the other (varargs may have
            // that many)
            if (m1.isVarArgs() && parameterTypes1.length < parameterTypes2.length) {
                Class<?>[] newTypes = new Class<?>[parameterTypes2.length];
                int varargIdx = parameterTypes1.length - 1;
                System.arraycopy(parameterTypes1, 0, newTypes, 0, varargIdx);
                Arrays.fill(newTypes, varargIdx, newTypes.length, parameterTypes1[varargIdx]);
                parameterTypes1 = newTypes;
            }
            if (m2.isVarArgs() && parameterTypes2.length < parameterTypes1.length) {
                Class<?>[] newTypes = new Class<?>[parameterTypes1.length];
                int varargIdx = parameterTypes2.length - 1;
                System.arraycopy(parameterTypes2, 0, newTypes, 0, varargIdx);
                Arrays.fill(newTypes, varargIdx, newTypes.length, parameterTypes2[varargIdx]);
                parameterTypes2 = newTypes;
            }

            return new Class<?>[][] { parameterTypes1, parameterTypes2 };
        }
    }

    /**
     * This is an implementation of the CandidateMethodSource that supports varargs.
     *
     * All methods satisfying these conditions are made available to the caller:
     *  - They are defined for the receiver;
     *  - They are public;
     *  - The method's name matches the given name;
     *  - The number of arguments matches the number of formal parameters, or exceeds
     *    the number of formal parameters and the method is varargs;
     *  - Each formal parameter's Class or Interface is either the same (class) as,
     *    or is a superclass or superinterface of, the class of the corresponding argument.
     *    If the method is varargs, all varargs arguments may also be a subclass/subinterface
     *    of the component type of the last formal parameter (which will always be an array of some type).
     */
    public abstract static class VarargsAwareCandidateMethodFinderBase implements MethodSelector.CandidateMethodFinder {
        public Stream<Method> findCandidates(Class<?> receiverClass, String name, Object[] args) {
            Class<?>[] argTypes = MethodSelector.getObjectTypes(args);

            return Arrays.stream(receiverClass.getMethods())
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

    public static class StaticVarargsAwareCandidateMethodFinder extends VarargsAwareCandidateMethodFinderBase {
        @Override
        public Stream<Method> findCandidates(Class<?> receiverClass, String name, Object[] args) {
            return super.findCandidates(receiverClass, name, args)
                .filter(m -> Modifier.isStatic(m.getModifiers()));
        }
    }

    public static class NonStaticVarargsAwareCandidateMethodFinder extends VarargsAwareCandidateMethodFinderBase {
        @Override
        public Stream<Method> findCandidates(Class<?> receiverClass, String name, Object[] args) {
            return super.findCandidates(receiverClass, name, args)
                .filter(m -> !Modifier.isStatic(m.getModifiers()));
        }
    }
}
