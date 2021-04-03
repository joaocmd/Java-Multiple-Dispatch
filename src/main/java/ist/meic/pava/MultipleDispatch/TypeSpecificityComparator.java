package ist.meic.pava.MultipleDispatch;

/**
 * The TypeSpecificityComparator performs a type comparison according to their
 * subtype relationship.
 *
 * A type is considered lesser than other if and only if it is a superclass
 * (or superinterface) of the other. When two types have no relationship with each
 * other in the interface or class hierarchy, they are considered incomparable.
 *
 * Keep in mind that primitive types are not the same as their boxed versions.
 */
public class TypeSpecificityComparator implements PartialComparator<Class<?>>{
    public PartialOrdering compare(Class<?> lhs, Class<?> rhs) {
        if (lhs == rhs) {
            return PartialOrdering.EQUAL;
        }

        if (lhs.isAssignableFrom(rhs)) {
            return PartialOrdering.LESS; // rhs is more specific
        } else if (rhs.isAssignableFrom(lhs)) {
            return PartialOrdering.GREATER; // lhs is more specific
        } else {
            // equal specificity but not the same: incomparable
            return PartialOrdering.INCOMPARABLE;
        }
    }
}
