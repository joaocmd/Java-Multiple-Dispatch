package ist.meic.pava.MultipleDispatch;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.*;

public class MethodSelector {
    private Comparator<Method> comparator;
    private MethodFilterFactory filterFactory;

    public MethodSelector(PartialComparator<Method> partialComparator, MethodFilterFactory filterFactory) {
        this.comparator = new Comparator<Method>(){
            public int compare(Method lhs, Method rhs) {
                PartialOrdering partialOrd = partialComparator.compare(lhs, rhs);

                if (partialOrd == PartialOrdering.UNCOMPARABLE) {
                    // enforce some total order
                    return lhs.toString().compareTo(rhs.toString());
                } else {
                    return partialOrd.asTotalOrdering();
                }
            }
        };

        this.filterFactory = filterFactory;
    }

    public MethodSelector(PartialComparator<Method> partialComparator) {
        this(partialComparator, new SimpleMethodFilterFactory());
    }

    public Method selectMethod(Object receiver, String name, Object... args) throws NoSuchMethodException {
        return Arrays.stream(receiver.getClass().getMethods())
            .filter(filterFactory.build(receiver, name, args))
            .max(comparator)
            .orElseThrow(() -> buildNoSuchMethodException(receiver.getClass(), args));
    }

    public static Class<?>[] getArgTypes(Object[] args) {
        List<Class<?>> argTypesList = Arrays.stream(args).map(Object::getClass).collect(Collectors.toList());
        Class<?>[] argTypes = new Class[argTypesList.size()];
        argTypes = argTypesList.toArray(argTypes);
        return argTypes;
    }

    private static NoSuchMethodException buildNoSuchMethodException(Class<?> receiverType, Object[] args) {
        return new NoSuchMethodException(buildNoSuchMethodExceptionMessage(receiverType, args));
    }

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

    public static interface MethodFilterFactory {
        public Predicate<Method> build(Object receiver, String name, Object[] args);
    }

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
