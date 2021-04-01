package ist.meic.pava.MultipleDispatch;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.*;

/**
 * This class is designed to choose a Method based on the parameters given. The simple use case is:
 *  - Create a MethodSelector with a method filter and a method comparator;
 *  - Call selectMethod with the desire receiver type, name and arguments.
 *  For a more in depth description, read the constructor and selectMethod javadoc.
 */
public class MethodSelector {
    private Comparator<Method> comparator;
    private MethodFilterFactory filterFactory;

    /**
     * Creates a new MethodSelector instance with the given PartialComparator and the given filterFactory.
     * The filter factory is the first step, which dictates which methods are potential candidates. For example, Methods
     * are, for the simplest case, potential candidates if their name and types match the desired Method.
     * The partialComparator is used to decide between the best methods to pick, and imposes a partial order. The
     * partial order dictates which, in case of multiple possible methods, should be picked. If the a total order
     * can't be decided by the partialComparator, this constructor desambiguates by comparing each method by name.
     * The filter factory is
     * @param partialComparator the partialComparator to use
     * @param filterFactory the filterFactory to use
     */
    public MethodSelector(PartialComparator<Method> partialComparator, MethodFilterFactory filterFactory) {
        this.comparator = new Comparator<Method>(){
            public int compare(Method lhs, Method rhs) {
                PartialOrdering partialOrd = partialComparator.compare(lhs, rhs);

                if (partialOrd == PartialOrdering.INCOMPARABLE) {
                    // enforce some total order
                    return lhs.toString().compareTo(rhs.toString());
                } else {
                    return partialOrd.asTotalOrdering();
                }
            }
        };

        this.filterFactory = filterFactory;
    }

    /**
     * Creates a new MethodSelector with the default SimpleMethodFilterFactory and the given partialComparator.
     * @param partialComparator the PartialComparator to use
     */
    public MethodSelector(PartialComparator<Method> partialComparator) {
        this(partialComparator, new SimpleMethodFilterFactory());
    }

    /**
     * Selects the best method for the receiver with the given name and arguments. The result depends on the
     * filterFactory and comparator on the current MethodSelector, as those dictate how the methods are selected
     * and which ones are valid.
     * @param receiver the receiver, that is, the object that contains the method.
     * @param name the name of the method to call.
     * @param args the arguments to be passed to the desired method.
     * @return the selected Method, given by the above criteria.
     * @throws NoSuchMethodException if no matching method could be found.
     */
    public Method selectMethod(Object receiver, String name, Object... args) throws NoSuchMethodException {
        return Arrays.stream(receiver.getClass().getMethods())
            .filter(filterFactory.build(receiver, name, args))
            .max(comparator)
            .orElseThrow(() -> buildNoSuchMethodException(receiver.getClass(), args));
    }

    /**
     * Given an array of objects, returns the corresponding class of each one, returned in the same order as given.
     * @param args the arguments that are to be passed to a method call.
     * @return an Array of Class, the classes of each passed Object.
     */
    public static Class<?>[] getArgTypes(Object[] args) {
        List<Class<?>> argTypesList = Arrays.stream(args).map(Object::getClass).collect(Collectors.toList());
        Class<?>[] argTypes = new Class[argTypesList.size()];
        argTypes = argTypesList.toArray(argTypes);
        return argTypes;
    }


    /**
     * Builds a NoSuchMethodException for the given receiver type and arguments.
     * @param receiverType the type of the receiver.
     * @param args the arguments supplied to the method.
     * @return NoSuchMethodException with the corresponding message.
     */
    private static NoSuchMethodException buildNoSuchMethodException(Class<?> receiverType, Object[] args) {
        return new NoSuchMethodException(buildNoSuchMethodExceptionMessage(receiverType, args));
    }

    /**
     * Prettifies the method signature in format receiverType(arg1Type, arg2Type, ...).
     * @param receiverType the type of the receiver.
     * @param args the arguments supplied to the method.
     * @return the prettified string.
     */
    private static String buildNoSuchMethodExceptionMessage(Class<?> receiverType, Object[] args) {
        // Get class type of each Object in args
        final Class<?>[] argTypes = getArgTypes(args);

        return receiverType.getName() +
                '(' +
                Arrays.stream(argTypes)
                        .map(Class::getName)
                        .collect(Collectors.joining(", ")) +
                ')';
    }

    /**
     * A MethodFilterFactory is an Object that can return a predicate for filtering Methods into applicable methods.
     */
    public static interface MethodFilterFactory {
        /**
         * Returns a Predicate with the purpose to decide which methods are to be picked and which methods are not.
         * Methods, for the purpose of this package, are defined by their receiver type, name and objects.
         * @param receiver the receiver object, which would invoke the method.
         * @param name the name of the method to call.
         * @param args the arguments that specify the type of the Method.
         * @return a predicate that can be used to decide between keeping or filtering out the method.
         */
        public Predicate<Method> build(Object receiver, String name, Object[] args);
    }

    /**
     * This is the simplest implementation of the MethodFilterFactory. Methods are considered applicable if:
     *  - The method's name matches the given name;
     *  - The receiverType is assignable to the Method type;
     *  - It has the same number of arguments as args and each arg is assignable to the corresponding argument.
     */
    public static class SimpleMethodFilterFactory implements MethodFilterFactory {
        public Predicate<Method> build(Object receiver, String name, Object[] args) {
            return m -> {
                if (!m.getName().equals(name)) {
                    return false;
                }

                // Confirm that all arguments are compatible with their respective parameters
                if (args.length != m.getParameterCount()) {
                    return false;
                }

                Class<?>[] paramTypes = m.getParameterTypes();
                for (int i = 0; i < paramTypes.length; i++) {
                    if (!paramTypes[i].isAssignableFrom(args[i].getClass())) {
                        return false;
                    }
                }

                return true;
            };
        }
    }
}
