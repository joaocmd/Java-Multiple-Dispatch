package ist.meic.pava.MultipleDispatch;

import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Util {
    public static Class<?>[] getArgTypes(Object... args) {
        List<Class<?>> argTypesList = Arrays.stream(args).map(Object::getClass).collect(Collectors.toList());
        Class<?>[] argTypes = new Class[argTypesList.size()];
        argTypes = argTypesList.toArray(argTypes);
        return argTypes;
    }

    public static NoSuchMethodException buildNoSuchMethodException(Class<?> receiverType, Class<?>[] argTypes) {
        return new NoSuchMethodException(buildNoSuchMethodExceptionMessage(receiverType, argTypes));
    }

    private static String buildNoSuchMethodExceptionMessage(Class<?> receiverType, Class<?>[] argTypes) {
        return receiverType.getName() +
                '(' +
                Arrays.stream(argTypes)
                        .map(Class::getName)
                        .collect(Collectors.joining(", ")) +
                ')';
    }
}
