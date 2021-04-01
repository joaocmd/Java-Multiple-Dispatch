package ist.meic.pava.MultipleDispatch;

import java.lang.reflect.Method;

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
