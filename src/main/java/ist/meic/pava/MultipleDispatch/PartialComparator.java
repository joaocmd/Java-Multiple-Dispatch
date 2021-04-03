package ist.meic.pava.MultipleDispatch;

/**
 * A comparison function which imposes a partial ordering on some collection of objects.
 *
 * This comparison must satisfy, for all a, b and c:
 * - asymmetry: a < b implies !(b < a) (must also hold for >)
 * - transitivity: a < b and b < c imply a < c (must also hold for > and ==)
 *
 * @see PartialOrdering
 */
@FunctionalInterface
public interface PartialComparator<T> {
	/**
	 * Compares its two arguments for order.
	 * @param lhs the first object to be compared.
	 * @param rhs the second object to be compared.
	 * @return partial ordering of the two arguments.
	 */
	public PartialOrdering compare(T lhs, T rhs);
}
