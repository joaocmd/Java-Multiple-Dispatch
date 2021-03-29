package ist.meic.pava.UsingMultipleDispatchExtended;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class UsingMultipleDispatch {

    /**
     * Invokes the method with name and args of the receiver. Implements dynamic dispatch for the arguments.
     * Throws RunTimeException on illegal access, missing method, or exception of the invoked method.
     * @param receiver  the receiver object, that is, the object that contains the method
     * @param name      the name of the method
     * @param args      the arguments to pass to the method
     * @return          the object returned by the method called
     */
    static Object invoke(Object receiver, String name, Object... args) {
        // Get class type of each Object in args
        List<Class<?>> argTypesList = Arrays.stream(args).map(Object::getClass).collect(Collectors.toList());
        Class<?>[] argTypes = new Class[argTypesList.size()];
        argTypes = argTypesList.toArray(argTypes);
        try {

            Method method = bestMethod(receiver.getClass(), name, argTypes);
            return method.invoke(receiver, args);
        } catch (IllegalAccessException
                | InvocationTargetException
                | NoSuchMethodException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Implements a Breadth-First-Search for finding and returning the most specific method
     * for the given argument types.
     * @param receiverType      the type of the receiver
     * @param name      the name of the method to find
     * @param argTypes  an Array with the ordered argument types
     * @return          the specified Method
     * @throws NoSuchMethodException if no implementation was found
     */
    static Method bestMethod(Class<?> receiverType, String name, Class<?>[] argTypes) throws NoSuchMethodException {
        // BFS over argument types
        Queue<Class<?>[]> queue = new ArrayDeque<>();
        queue.add(argTypes);

        while (queue.peek() != null) {
            Class<?>[] currArgTypes = queue.poll();

            try {
                // this is the most specialized method available, use it
                return receiverType.getMethod(name, currArgTypes);
            } catch (NoSuchMethodException e) {
                System.err.println(e.getMessage());
            }

            // enqueue more general argument types
            for (int i = 0; i < currArgTypes.length; i++) {
                if (currArgTypes[i] != Object.class) {
                    Class<?>[] moreGeneralArgTypes = currArgTypes.clone();
                    moreGeneralArgTypes[i] = currArgTypes[i].getSuperclass();
                    queue.add(moreGeneralArgTypes);
                }
            }
        }

        String msgBuilder = receiverType.getName() +
                '(' +
                Arrays.stream(argTypes)
                        .map(Class::getName)
                        .collect(Collectors.joining(", ")) +
                ')';
        throw new NoSuchMethodException(msgBuilder);
    }
}
