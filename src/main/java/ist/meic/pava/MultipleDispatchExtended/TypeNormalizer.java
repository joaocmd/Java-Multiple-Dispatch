package ist.meic.pava.MultipleDispatchExtended;

public class TypeNormalizer {

    /**
     * Gets the boxed version of a type.
     *
     * @param type a type.
     * @return boxed version of the type.
     */
    public static Class<?> boxed(Class<?> type) {
        if (type == boolean.class) {
            return Boolean.class;
        } else if (type == byte.class) {
            return Byte.class;
        } else if (type == char.class) {
            return Character.class;
        } else if (type == double.class) {
            return Double.class;
        } else if (type == float.class) {
            return Float.class;
        } else if (type == int.class) {
            return Integer.class;
        } else if (type == long.class) {
            return Long.class;
        } else if (type == short.class) {
            return Short.class;
        } else if (type == void.class) {
            return Void.class;
        } else {
            return type;
        }
    }
}
