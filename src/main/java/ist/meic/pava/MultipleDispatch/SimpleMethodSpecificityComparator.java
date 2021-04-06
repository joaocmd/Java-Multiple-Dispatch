package ist.meic.pava.MultipleDispatch;

import java.lang.reflect.Method;

/**
 * The SimpleMethodSpecificityComparator class enforces the method specificity
 * ordering defined in the project statement (excluding extensions).
 *
 * A method m1 is considered more specific than method m2 when the declaring
 * class of m1 is a subclass of m2. When the declaring classes are identical,
 * then m1 is more specific than m2 if the type of the first parameter of m1 is
 * a subtype of the type of the first parameter of m2. If these types are
 * identical, then we repeat the process with the following parameters, from
 * left to right.
 *
 * If one of the methods has more formal parameters than the other, only the
 * first k parameters will be considered for comparison, where k is the number
 * of formal parameters of the other method.
 *
 * @see TypeSpecificityComparator for the subtype relationship implementation
 */
public class SimpleMethodSpecificityComparator implements PartialComparator<Method> {
    private static PartialComparator<Class<?>> typeComparator = new TypeSpecificityComparator();

    public PartialOrdering compare(Method lhs, Method rhs) {
        if (lhs == rhs) {
            return PartialOrdering.EQUAL;
        }

        // if the declaring class of lhs is a subtype of the declaring class of rhs,
        // then lhs is less specific than rhs
        return typeComparator.compare(lhs.getDeclaringClass(), rhs.getDeclaringClass())
            .mapEqual(() -> compareParameters(lhs.getParameterTypes(), rhs.getParameterTypes(), typeComparator));
    }

    /**
     * Compares two parameter lists with a given type comparator.
     *
     * @param lhs left parameter list
     * @param rhs right parameter list
     * @param typeComparator a type (partial) comparator
     * @return partial ordering of the two parameter lists
     */
    public static PartialOrdering compareParameters(Class<?>[] lhs, Class<?>[] rhs, PartialComparator<Class<?>> typeComparator) {
        for (int i = 0; i < lhs.length && i < rhs.length; i++) {
            PartialOrdering partialOrd = typeComparator.compare(lhs[i], rhs[i]);

            if (partialOrd != PartialOrdering.EQUAL) {
                return partialOrd;
            }
        }

        return PartialOrdering.EQUAL;
    }
}
