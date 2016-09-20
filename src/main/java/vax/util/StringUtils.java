package vax.util;

/**

 @author toor
 */
public class StringUtils {
    private StringUtils () {
        throw new UnsupportedOperationException();
    }

    private static final StringBuilder stringBuilder = new StringBuilder();

    /**
     All the arguments are required to be strictly non-null.

     @param args
     @return
     @throws NullPointerException if any of the arguments is null
     */
    public static String[] toStrings ( Object[] args ) {
        int len = args.length;
        String[] retS = new String[len];
        for( int i = 0; i < len; i++ ) {
            retS[i] = args[i].toString();
        }
        return retS;
    }

    /**
     All the arguments are required to be strictly non-null.

     @param arg
     @param args
     @return
     @throws NullPointerException if any of the arguments is null
     */
    public static String[] toStrings ( Object arg, Object... args ) {
        int len = args.length;
        String[] retS = new String[len + 1];
        for( int i = 0; i < len; i++ ) {
            retS[i + 1] = args[i].toString();
        }
        return retS;
    }

    public static String toString ( Object arg, Object... args ) {
        stringBuilder.setLength( 0 );
        stringBuilder.append( arg );
        for( Object o : args ) {
            stringBuilder.append( o );
        }
        return stringBuilder.toString();
    }

    public static String toShortString ( StackTraceElement ste ) {
        String fileName = ste.getFileName();
        int lineNumber = ste.getLineNumber();
        return ste.getMethodName()
                + ( ste.isNativeMethod() ? "(Native Method)"
                        : ( fileName != null && lineNumber >= 0
                                ? "(" + fileName + ":" + lineNumber + ")"
                                : ( fileName != null ? "(" + fileName + ")" : "(Unknown Source)" ) ) );
    }

    /**
     Please note: supplying n too large to fit in the buffer results in undefined behaviour.

     @param n
     @param chars
     @return chars array for chaining
     */
    @SuppressWarnings( "AssignmentToMethodParameter" )
    public static char[] toChars ( int n, char[] chars ) {
        int i = chars.length - 1;
        if ( n >= 0 ) {
            for( ; i >= 0 && n != 0; i-- ) {
                chars[i] = (char) ( n % 10 + '0' );
                n /= 10;
            }
            for( ; i >= 0; i-- ) {
                chars[i] = '0';
            }
        } else {
            n = -n;
            for( ; i >= 0 && n != 0; i-- ) {
                chars[i] = (char) ( n % 10 + '0' );
                n /= 10;
            }
            for( ; i > 0; i-- ) {
                chars[i] = '0';
            }
            chars[0] = '-';
        }
        return chars;
    }

    /**
     calls <code>#toStringSeparated( n, 3 )</code>

     @param n
     @return
     */
    public static String toStringDotted ( int n ) {
        return toStringSeparated( n, 3, '.' );
    }

    /**
     Converts an integer to corresponding signed base-10 String,
     separated by <code>separatorChar</code> every <code>separatorSpacing</code> digits.

     @param n number to convert
     @param separatorSpacing how many digits are between the separators
     @param separatorChar
     @return
     */
    @SuppressWarnings( "AssignmentToMethodParameter" )
    public static String toStringSeparated ( int n, int separatorSpacing, char separatorChar ) {
        if ( n == 0 ) {
            return "0";
        }
        boolean negative;
        if ( n < 0 ) {
            negative = true;
            n = -n;
        } else {
            negative = false;
        }
        int digits = (int) Math.log10( n ) + 1;
        int len = digits + ( digits - 1 ) / separatorSpacing;
        if ( negative ) {
            len++;
        }
        char[] chars = new char[len];
        for( int i = len - 1, j = 0; i >= 0; i--, j++ ) {
            if ( j < separatorSpacing ) {
                chars[i] = (char) ( n % 10 + '0' );
                n /= 10;
            } else {
                chars[i] = separatorChar;
                j = 0;
            }
        }
        if ( negative ) {
            chars[0] = '-';
        }
        return new String( chars );
    }

    private static int getIntCharLength ( int n ) {
        if ( n == 0 ) {
            return 1;
        } else if ( n > 0 ) {
            return (int) Math.log10( n ) + 1;
        } else {
            return (int) Math.log10( -n ) + 2;
        }
    }

    public static String toStringPadded ( int n, int totalLength ) {
        int charLen = getIntCharLength( n );
        if ( charLen > totalLength ) {
            throw new IllegalArgumentException( "n == " + n + " : charLen [" + charLen + "] > totalLength [" + totalLength + "]" );
        }
        return new String( toChars( n, new char[totalLength] ) );
    }

    /*
     static public String toStringPadded ( int number, int paddedLength ) {
     //return String.format( "%0" + paddedLength + "d", number );
     }
     */

 /*
     public static String toString( int n ) {
     return ( n == 0 ) ? "0" : new String( toChars( n, new char[getIntCharLength( n )] ) );
     }
     */
}
