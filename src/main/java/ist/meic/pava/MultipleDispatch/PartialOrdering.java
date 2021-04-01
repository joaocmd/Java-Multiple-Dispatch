package ist.meic.pava.MultipleDispatch;

/**
 * This contains the PartialOrdering possible outcomes: LESS, GREATER or EQUAL. If the objects are not
 * comparable aren't any of the above, they are deemed INCOMPARABLE.
 */
public enum PartialOrdering {
    INCOMPARABLE,
    EQUAL,
    LESS,
    GREATER;

    /**
     * Converts the PartialOrdering to an int that would be used by a function that requires ordering, such as sort,
     * min or max.
     * @return the int corresponding to the PartialOrdering.
     * @throws UnsupportedOperationException if there is no total order.
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
