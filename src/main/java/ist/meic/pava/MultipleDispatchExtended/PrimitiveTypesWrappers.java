package ist.meic.pava.MultipleDispatchExtended;

import java.util.HashMap;
import java.util.Map;

public class PrimitiveTypesWrappers {
    private static final Map<Class<?>, Class<?>> WRAPPERS = new HashMap<Class<?>, Class<?>>() {{
        put(boolean.class, Boolean.class);
        put(byte.class, Byte.class);
        put(char.class, Character.class);
        put(double.class, Double.class);
        put(float.class, Float.class);
        put(int.class, Integer.class);
        put(long.class, Long.class);
        put(short.class, Short.class);
        put(void.class, Void.class);
    }};

    public static boolean isAssignableFrom(Class<?> receiver, Class<?> cls) {
        return receiver.isAssignableFrom(cls)
            || (WRAPPERS.containsKey(receiver) && cls == WRAPPERS.get(receiver));
    }
}
