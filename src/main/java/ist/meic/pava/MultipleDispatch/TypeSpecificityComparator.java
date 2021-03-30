package ist.meic.pava.MultipleDispatch;

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
            return PartialOrdering.UNCOMPARABLE;
        }
    }
}
