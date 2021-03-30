package ist.meic.pava.MultipleDispatch;

import java.lang.reflect.Method;

public class MethodSpecificityComparator implements PartialComparator<Method> {
    public PartialOrdering compare(Method lhs, Method rhs) {
        if (lhs == rhs) {
            return PartialOrdering.EQUAL;
        }

        Class<?>[] lhsParamTypes = lhs.getParameterTypes();
        Class<?>[] rhsParamTypes = rhs.getParameterTypes();

        for (int i = 0; i < lhsParamTypes.length; i++) {
            PartialOrdering partialOrd = TypeSpecificityComparator.compare(lhsParamTypes[i], rhsParamTypes[i]);

            if (partialOrd != PartialOrdering.UNCOMPARABLE && partialOrd != PartialOrdering.EQUAL) {
                return partialOrd;
            }
        }

        return PartialOrdering.UNCOMPARABLE;
    }
}
