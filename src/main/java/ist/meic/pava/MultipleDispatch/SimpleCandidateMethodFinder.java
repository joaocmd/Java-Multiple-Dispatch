package ist.meic.pava.MultipleDispatch;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * This is the simplest implementation of the CandidateMethodSource.
 *
 * All methods satisfying these conditions are made available to the caller:
 *  - They are defined for the receiver;
 *  - They are public;
 *  - The method's name matches the given name;
 *  - The number of arguments matches the number of formal parameters;
 *  - Each formal parameter's Class or Interface is either the same (class) as,
 *    or is a superclass or superinterface of, the class of the corresponding argument.
 */
public class SimpleCandidateMethodFinder implements MethodSelector.CandidateMethodFinder {
    public static final Function<String, Predicate<Method>> NAME_FILTER = name -> {
        return m -> m.getName().equals(name);
    };

    public Stream<Method> findCandidates(Class<?> receiverClass, String name, Object[] args) {
        return Arrays.stream(receiverClass.getMethods())
            .filter(NAME_FILTER.apply(name))
            .filter(m -> !Modifier.isStatic(m.getModifiers()))
            .filter(m -> {
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
            });
    }
}
