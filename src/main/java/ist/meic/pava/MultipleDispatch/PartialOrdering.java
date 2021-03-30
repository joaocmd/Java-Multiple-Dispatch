package ist.meic.pava.MultipleDispatch;

public enum PartialOrdering {
    UNCOMPARABLE,
    EQUAL,
    LESS,
    GREATER;

    public int asTotalOrdering() {
        switch (this) {
            case EQUAL:
                return 0;
            case LESS:
                return -1;
            case GREATER:
                return 1;
            case UNCOMPARABLE:
                throw new UnsupportedOperationException("cannot interpret as total ordering: elements are incomparable");
            default:
                throw new UnsupportedOperationException();
        }
    }
}
