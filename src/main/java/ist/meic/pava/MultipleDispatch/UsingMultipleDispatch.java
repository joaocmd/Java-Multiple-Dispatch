package ist.meic.pava.MultipleDispatch;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class UsingMultipleDispatch {
    static Object invoke(Object receiver, String name, Object... args) {
        try {
            Method method = bestMethod(receiver.getClass(), name, args.getClass());
            return method.invoke(receiver, args);
        } catch (IllegalAccessException
                | InvocationTargetException
                | NoSuchMethodException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    static Method bestMethod(Class type, String name, Class argType) throws NoSuchMethodException {
        try {
            return type.getMethod(name, argType);
        } catch (NoSuchMethodException e) {
            if (argType == Object.class) {
                throw e;
            } else {
                return bestMethod(type, name, argType.getSuperclass());
            }
        }
    }
}
