package ist.meic.pava.MultipleDispatch;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class UsingMultipleDispatch {
    static Object invoke(Object receiver, String name, Object... args) {
        try {
            List<Class> argTypesList = Arrays.stream(args).map(Object::getClass).collect(Collectors.toList());
            Class[] argTypes = new Class[argTypesList.size()];
            argTypes = argTypesList.toArray(argTypes);

            Method method = bestMethod(receiver.getClass(), name, argTypes);
            return method.invoke(receiver, args);
        } catch (IllegalAccessException
                | InvocationTargetException
                | NoSuchMethodException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    static Method bestMethod(Class type, String name, Class[] argTypes) throws NoSuchMethodException {
        Queue<Class[]> queue = new ArrayDeque<>();
        queue.add(argTypes);

        while (queue.peek() != null) {
            Class[] currArgTypes = queue.poll();

            try {
                return type.getMethod(name, currArgTypes);
            } catch (NoSuchMethodException e) {}

            for (int i = 0; i < currArgTypes.length; i++) {
                if (currArgTypes[i] != Object.class) {
                    Class[] moreGeneralArgTypes = currArgTypes.clone();
                    moreGeneralArgTypes[i] = currArgTypes[i].getSuperclass();
                    queue.add(moreGeneralArgTypes);
                }
            }
        }

        throw new NoSuchMethodException();
    }
}
