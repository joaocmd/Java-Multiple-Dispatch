package ist.meic.pava.MultipleDispatch;

/**
 * A PartialComparator compares objects attributing them a partial order.
 */
public interface PartialComparator<T> {
	/**
	 * Compares two objects assignable to T, giving them a partial order.
	 * @param lhs the left hand side argument.
	 * @param rhs the right hand side argument.
	 * @return the corresponding PartialOrdering.
	 */
	public PartialOrdering compare(T lhs, T rhs);
}
