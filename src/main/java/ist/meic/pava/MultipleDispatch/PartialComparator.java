package ist.meic.pava.MultipleDispatch;

public interface PartialComparator<T> {
	public PartialOrdering compare(T lhs, T rhs);
}
