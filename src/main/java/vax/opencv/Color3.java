package vax.opencv;

import org.opencv.core.Scalar;

/**

 @author toor
 */
public enum Color3 {
    Red( 1, 0, 0 ),
    Green( 0, 1, 0 ),
    Blue( 0, 0, 1 ),
    Cyan( 0, 1, 1 ),
    Magenta( 1, 0, 1 ),
    Yellow( 1, 1, 0 ),
    Black( 0, 0, 0 ),
    White( 1, 1, 1 ),
    Gray( 0.5, 0.5, 0.5 ), //
    ;

    private final static int RGB8_MAX = ( 1 << 8 ) - 1;
    //
    private final Scalar scalar;

    private Color3 ( double r, double g, double b ) {
        scalar = new Scalar( b * RGB8_MAX, g * RGB8_MAX, r * RGB8_MAX );
    }

    public Scalar getScalar () {
        return scalar;
    }
}
