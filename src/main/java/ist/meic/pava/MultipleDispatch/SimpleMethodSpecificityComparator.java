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
    protected static TypeSpecificityComparator typeSpecificityComparator = new TypeSpecificityComparator();

    public PartialOrdering compare(Method lhs, Method rhs) {
        if (lhs == rhs) {
            return PartialOrdering.EQUAL;
        }

        // if the declaring class of lhs is a subtype of the declaring class of rhs,
        // then lhs is less specific than rhs
        PartialOrdering receiverPartialOrd = typeSpecificityComparator.compare(lhs.getDeclaringClass(),
                rhs.getDeclaringClass());
        if (receiverPartialOrd != PartialOrdering.EQUAL) {
            return receiverPartialOrd;
        }

        Class<?>[][] paramTypes = getParameterTypes(lhs, rhs);
        Class<?>[] lhsParamTypes = paramTypes[0];
        Class<?>[] rhsParamTypes = paramTypes[1];
        for (int i = 0; i < lhsParamTypes.length && i < rhsParamTypes.length; i++) {
            PartialOrdering partialOrd = typeSpecificityComparator.compare(lhsParamTypes[i], rhsParamTypes[i]);

            if (partialOrd != PartialOrdering.EQUAL) {
                return partialOrd;
            }
        }

        return PartialOrdering.INCOMPARABLE;
    }

    /**
     * Obtains parameter types of the given methods. Can be overriden to influence
     * comparison (say, when varargs or primitives are involved).
     *
     * @param m1 method
     * @param m2 another method
     * @return parameter types of given methods
     */
    protected Class<?>[][] getParameterTypes(Method m1, Method m2) {
        return new Class<?>[][] { m1.getParameterTypes(), m2.getParameterTypes() };
    }
}
