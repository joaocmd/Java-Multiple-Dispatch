package ist.meic.pava.MultipleDispatch;

/**
 * The result of a comparison between two objects.
 */
public enum PartialOrdering {
    /**
     * An ordering where the two values cannot be compared.
     */
    INCOMPARABLE,
    /**
     * An ordering where a compared value is equal to another.
     */
    EQUAL,
    /**
     * An ordering where a compared value is less than another.
     */
    LESS,
    /**
     * An ordering where a compared value is greater than another.
     */
    GREATER;

    /**
     * Casts the ordering to a total order (represented as an int in Java)
     * @return the int corresponding to the ordering.
     * @throws UnsupportedOperationException if there is no total order/the ordering was INCOMPARABLE.
     * @see java.util.Comparator
     */
    public int asTotalOrdering() {
        switch (this) {
            case EQUAL:
                return 0;
            case LESS:
                return -1;
            case GREATER:
                return 1;
            case INCOMPARABLE:
                throw new UnsupportedOperationException("cannot interpret as total ordering: elements are incomparable");
            default:
                throw new UnsupportedOperationException();
        }
    }
}
