package ist.meic.pava.MultipleDispatch;

import java.util.function.Supplier;

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
     * Casts a total ordering (represented as an int in Java) to a partial ordering.
     *
     * @param ord int corresponding to a total ordering
     * @return partial ordering
     * @see java.util.Comparator
     */
    public static PartialOrdering fromTotalOrdering(int ord) {
        if (ord == 0) {
            return EQUAL;
        } else if (ord < 0) {
            return LESS;
        } else {
            return GREATER;
        }
    }

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

    /**
     * If this ordering is EQUAL, returns the value returned by next, otherwise
     * returns the current ordering.
     *
     * Used to chain progressively more specific comparisons.
     * Suitable for chaining.
     *
     * @param next supplier of the new value
     * @return the current ordering or the result of calling next
     */
    public PartialOrdering mapEqual(Supplier<PartialOrdering> next) {
        switch (this) {
            case EQUAL:
                return next.get();
            default:
                return this;
        }
    }


    /**
     * If this ordering is INCOMPARABLE, returns the value returned by next,
     * otherwise returns the current ordering.
     *
     * Used to recover from incomparable conditions with different comparison criteria.
     * Suitable for chaining.
     *
     * @param next Supplier of the new value
     * @return the current ordering or the result of calling next
     */
    public PartialOrdering mapIncomparable(Supplier<PartialOrdering> next) {
        switch (this) {
            case INCOMPARABLE:
                return next.get();
            default:
                return this;
        }
    }
}
