package ist.meic.pava.MultipleDispatch;

import java.lang.reflect.Method;

/**
 * This MethodSpecificityComparator is the base case for comparing two methods. A method m1 is considered more specific
 * than method 2m when the declaring class of m1 is a subclass of m2. When the declaring classes are identical, then
 * m1 is more specific than m2 if the type of the first parameter of m1 is a subtype of the type of the first parameter
 * of m2. If these types are identical, then we repeat the process with the following parameters, from left to right.
 */
public class SimpleMethodSpecificityComparator implements PartialComparator<Method> {
    protected static TypeSpecificityComparator typeSpecificityComparator = new TypeSpecificityComparator();

    public PartialOrdering compare(Method lhs, Method rhs) {
        if (lhs == rhs) {
            return PartialOrdering.EQUAL;
        }

        // if the declaring class of lhs is a subtype of the declaring class of rhs, then lhs is less specific than rhs
        PartialOrdering receiverPartialOrd = typeSpecificityComparator.compare(lhs.getDeclaringClass(), rhs.getDeclaringClass());
        if (receiverPartialOrd != PartialOrdering.INCOMPARABLE && receiverPartialOrd != PartialOrdering.EQUAL) {
            return receiverPartialOrd;
        }

        Class<?>[] lhsParamTypes = lhs.getParameterTypes();
        Class<?>[] rhsParamTypes = rhs.getParameterTypes();
        for (int i = 0; i < lhsParamTypes.length && i < rhsParamTypes.length; i++) {
            PartialOrdering partialOrd = typeSpecificityComparator.compare(lhsParamTypes[i], rhsParamTypes[i]);

            if (partialOrd != PartialOrdering.INCOMPARABLE && partialOrd != PartialOrdering.EQUAL) {
                return partialOrd;
            }
        }

        return PartialOrdering.INCOMPARABLE;
    }
}
