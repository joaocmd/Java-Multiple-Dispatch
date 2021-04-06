package ist.meic.pava.MultipleDispatchExtended;

import ist.meic.pava.MultipleDispatch.PartialOrdering;

/**
 * The ExtendedTypeSpecificityComparator performs a type comparison according to their
 * subtype relationship *and* disambiguates non-related types.
 *
 * Primitives are considered more specific than their boxed counterparts.
 *
 * When two types have no relationship between them, if only one of them is an interface,
 * it is considered less specific; if both are interfaces/classes, they are compared by name.
 */
public class ExtendedTypeSpecificityComparator extends ist.meic.pava.MultipleDispatch.TypeSpecificityComparator {
    public PartialOrdering compare(Class<?> lhsOrig, Class<?> rhsOrig) {
        Class<?> lhs = TypeNormalizer.boxed(lhsOrig);
        Class<?> rhs = TypeNormalizer.boxed(rhsOrig);

        return super.compare(lhs, rhs)
            .mapEqual(() -> {
                // prefer primitives
                if (lhsOrig.isPrimitive() && !rhsOrig.isPrimitive()) {
                    return PartialOrdering.GREATER;
                } else if (!lhsOrig.isPrimitive() && rhsOrig.isPrimitive()) {
                    return PartialOrdering.LESS;
                }

                return PartialOrdering.EQUAL;
            })
            .mapIncomparable(() -> {
                // Classes are considered more specific than interfaces
                if (lhs.isInterface() && !rhs.isInterface()) {
                    return PartialOrdering.LESS;
                } else if (!lhs.isInterface() && rhs.isInterface()) {
                    return PartialOrdering.GREATER;
                }

                return PartialOrdering.INCOMPARABLE;
            })
            .mapIncomparable(() -> {
                // disambiguate by name
                return PartialOrdering.fromTotalOrdering(lhs.getName().compareTo(rhs.getName()));
            });
    }

}