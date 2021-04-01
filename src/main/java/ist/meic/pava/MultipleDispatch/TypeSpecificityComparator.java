package ist.meic.pava.MultipleDispatch;

/**
 * Compares the specificity of two Classes.
 */
public class TypeSpecificityComparator implements PartialComparator<Class<?>>{

     /**
     * Compares the specificity of two types. A type is equal to itself. If a type is assignable to another type, it
     * is so a subclass or an interface implementation, which means that it is more specific. If none of these
     * conditions are met, the types not comparable (e.g.: siblings or totally completely distinct classes).
     * @param lhs the left hand side argument.
     * @param rhs the right hand side argument.
     * @return the partial ordering for the specificity of the two given classes.
     */
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
