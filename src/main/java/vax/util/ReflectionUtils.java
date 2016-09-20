package vax.util;

import java.lang.reflect.Method;

/**

 @author toor
 */
public class ReflectionUtils {
    private ReflectionUtils () {
        throw new UnsupportedOperationException();
    }

    public static Class<?>[] toClasses ( Object... args ) {
        int len = args.length;
        Class<?>[] klasses = new Class<?>[len];
        for( int i = 0; i < len; i++ ) {
            klasses[i] = args[i].getClass();
        }
        return klasses;
    }

    public static void invoke ( Class<?> klass, String methodName, Object... args ) {
        try {
            Method m = klass.getDeclaredMethod( methodName, toClasses( args ) );
            m.setAccessible( true );
            m.invoke( null, args );
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException( ex );
        }
    }
}
