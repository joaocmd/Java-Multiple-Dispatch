
package ist.meic.pava.MultipleDispatchExtended;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.stream.Stream;

import ist.meic.pava.MultipleDispatch.MethodSelector;
import ist.meic.pava.MultipleDispatch.PartialComparator;
import ist.meic.pava.MultipleDispatch.PartialOrdering;
import ist.meic.pava.MultipleDispatch.SimpleCandidateMethodFinder;
import ist.meic.pava.MultipleDispatch.SimpleMethodSpecificityComparator;

/**
 * Implements dynamic dispatch on the arguments of a method call.
 *
 * Supports calling methods with variadic arguments with the same syntax as regular
 * calls except for one case: when passing exactly one array (T[]) after the non
 * variadic arguments. In a regular call, this array could be casted to the vararg
 * type and would be considered one of the varargs. When using this class, as long
 * as T is assignable to the vararg type, the array T[] will be treated as the
 * container of all variadic arguments.
 *
 * Will automatically box/unbox arguments, except for variadic arguments, where
 * the boxed versions must be used always.
 *
 * See ist.meic.pava.MultipleDispatchExtended.VariadicArgumentTest for an
 *      example of this edge case (varargsPassArrayTest).
 */
public class UsingMultipleDispatch {
    private static MethodSelector staticMethodSelector = new MethodSelector(new ExtendedMethodComparator(),
            new StaticExtendedCandidateMethodFinder());
    private static MethodSelector nonStaticMethodSelector = new MethodSelector(new ExtendedMethodComparator(),
            new NonStaticExtendedCandidateMethodFinder());

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
     * See https://docs.oracle.com/javase/specs/jls/se7/html/jls-15.html#jls-15.12.4.2
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
     * See https://docs.oracle.com/javase/specs/jls/se7/html/jls-15.html#jls-15.12.4.2
     * @param method method to call
     * @param args   arguments list
     * @return transformed arguments list as per JLS 15.12.4.2
     */
    private static Object[] evaluateArguments(Method method, Object[] args) {
        if (method.isVarArgs() && shouldBuildVarargsArray(method, args)) {
            int nonVarargsCount = method.getParameterCount() - 1;
            int varargsCount = args.length - nonVarargsCount;

            Class<?> varargsType = method.getParameterTypes()[nonVarargsCount];
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
     * The ExtendedMethodComparator extends the ordering defined by
     * SimpleMethodSpecificityComparator with variadic arguments-specific details.
     *
     * The comparison criteria are as follows:
     * - if method b has a more specific receiver type than method a, b is more specific than a;
     * - if so far both methods are equally specific, but method b has more non-varargs
     *   formal parameters, then b is more specific;
     * - if so far both methods are equally specific, but method b is varargs and
     *   a is not, then b is more specific;
     * - if so far both methods are equally specific, but method b's normalized argument
     *   types are more specific than a's (when compared one by one from left to
     *   right until two are non equal), then b is more specific.
     */
    public static class ExtendedMethodComparator implements PartialComparator<Method> {
        private static PartialComparator<Class<?>> typeComparator = new ExtendedTypeSpecificityComparator();

        public PartialOrdering compare(Method lhs, Method rhs) {
            if (lhs == rhs) {
                return PartialOrdering.EQUAL;
            }

            // if the declaring class of lhs is a subtype of the declaring class of rhs,
            // then lhs is less specific than rhs
            PartialOrdering p = typeComparator.compare(lhs.getDeclaringClass(), rhs.getDeclaringClass())
                // if lhs accepts less (non-varargs) parameters than rhs, then lhs is less
                // specific
                .mapEqual(() -> PartialOrdering.fromTotalOrdering(Integer.compare(getNormalParameterCount(lhs), getNormalParameterCount(rhs))))
                .mapEqual(() -> {
                    // if lhs is varargs and rhs is not, lhs is less specific
                    if (lhs.isVarArgs() && !rhs.isVarArgs()) {
                        return PartialOrdering.LESS;
                    } else if (!lhs.isVarArgs() && rhs.isVarArgs()) {
                        return PartialOrdering.GREATER;
                    }

                    return PartialOrdering.EQUAL;
                })
                .mapEqual(() -> {
                    Class<?>[] lhsNormalParams = getNormalizedParameterTypes(lhs, rhs);
                    Class<?>[] rhsNormalParams = getNormalizedParameterTypes(rhs, lhs);
                    return SimpleMethodSpecificityComparator.compareParameters(lhsNormalParams, rhsNormalParams, typeComparator);
                });

            return p;
        }

        /**
         * Normalizes a method parameter list to erase varargs presence.
         *
         * The resulting parameter list will have at least as many parameters as
         * the other method, and all parameters after the normal ones will the
         * varargs type.
         *
         * @param method a method
         * @param other the other method
         * @return normalized parameter list of method
         */
        private static Class<?>[] getNormalizedParameterTypes(Method method, Method other) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            int lastIdx = parameterTypes.length - 1;

            if (method.isVarArgs()) {
                // treat varargs as any other args
                parameterTypes[lastIdx] = parameterTypes[lastIdx].getComponentType();

                // expand method arguments list to match size of the other (varargs may have
                // that many)
                if (parameterTypes.length < other.getParameterCount()) {
                    Class<?>[] newTypes = new Class<?>[other.getParameterCount()];
                    System.arraycopy(parameterTypes, 0, newTypes, 0, lastIdx);
                    Arrays.fill(newTypes, lastIdx, newTypes.length, parameterTypes[lastIdx]);
                    parameterTypes = newTypes;
                }
            }

            return parameterTypes;
        }

        private static int getNormalParameterCount(Method method) {
            int paramCount = method.getParameterCount();

            if (method.isVarArgs()) {
                paramCount--;
            }

            return paramCount;
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
     *
     * Primitive method parameter and argument types are normalized to their boxed
     * versions before comparison (so they are indistinguishable).
     */
    public abstract static class ExtendedCandidateMethodFinderBase implements MethodSelector.CandidateMethodFinder {
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
                        if (!isAssignableFrom(paramTypes, argTypes, 0, regularArgCount)) {
                            return false;
                        }

                        // Check varargs compatibility
                        if (method.isVarArgs()) {
                            int varargFirstIndex = paramTypes.length - 1;

                            if (varargFirstIndex == argTypes.length) {
                                // varargs method with no varargs supplied
                                return true;
                            }

                            if (paramTypes.length == argTypes.length
                                    && isAssignableFrom(paramTypes[varargFirstIndex], argTypes[varargFirstIndex])) {
                                // varargs method with args array supplied
                                return true;
                            }

                            Class<?> varargType = paramTypes[varargFirstIndex].getComponentType();
                            if (!isAssignableFrom(varargType, argTypes, varargFirstIndex)) {
                                return false;
                            }
                        }

                        return true;
                    });
        }

        private static boolean isAssignableFrom(Class<?> lhs, Class<?> rhs) {
            lhs = TypeNormalizer.boxed(lhs);
            rhs = TypeNormalizer.boxed(rhs);

            return lhs.isAssignableFrom(rhs);
        }

        private static boolean isAssignableFrom(Class<?> lhs, Class<?>[] rhs, int fromIndex) {
            for (int i = fromIndex; i < rhs.length; i++) {
                if (!isAssignableFrom(lhs, rhs[i])) {
                    return false;
                }
            }

            return true;
        }

        private static boolean isAssignableFrom(Class<?>[] lhs, Class<?>[] rhs, int fromIndex, int toIndex) {
            for (int i = fromIndex; i < toIndex; i++) {
                if (!isAssignableFrom(lhs[i], rhs[i])) {
                    return false;
                }
            }

            return true;
        }
    }

    /**
     * ExtendedCandidateMethodFinder that only accepts static methods.
     */
    public static class StaticExtendedCandidateMethodFinder extends ExtendedCandidateMethodFinderBase {
        @Override
        public Stream<Method> findCandidates(Class<?> receiverClass, String name, Object[] args) {
            return super.findCandidates(receiverClass, name, args)
                .filter(m -> Modifier.isStatic(m.getModifiers()));
        }
    }

    /**
     * ExtendedCandidateMethodFinder that only accepts non-static methods.
     */
    public static class NonStaticExtendedCandidateMethodFinder extends ExtendedCandidateMethodFinderBase {
        @Override
        public Stream<Method> findCandidates(Class<?> receiverClass, String name, Object[] args) {
            return super.findCandidates(receiverClass, name, args)
                .filter(m -> !Modifier.isStatic(m.getModifiers()));
        }
    }
}
