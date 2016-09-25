package vax.celloscope;

import org.opencv.core.*;
import static org.opencv.imgcodecs.Imgcodecs.*;
import static org.opencv.imgproc.Imgproc.*;
import org.opencv.imgproc.Imgproc;

/**

 @author toor
 */
public class ImageCv {
    final static public int MAX_8BIT_VALUE = 255;
    final static public double DEFAULT_CANNY_RATIO = 3.0;

    private Mat src;
    private Mat dst = new Mat();
    private final Size tmpSize = new Size();
    private final Scalar tmpScalar = new Scalar( 0 );

    private boolean autoswap = false;
    private Interpolation defaultInterpolation = Interpolation.Cubic;

    /**
     @param src initially not copied, referenced only
     */
    public ImageCv ( Mat src ) {
        this.src = src;
    }

    /**
     initially not copied, referenced only

     @param src
     @param autoswap
     */
    public ImageCv ( Mat src, boolean autoswap ) {
        this.src = src;
        this.autoswap = autoswap;
    }

    public ImageCv ( ImageCv imageCv ) {
        this( imageCv, true );
    }

    public ImageCv ( ImageCv imageCV, boolean deepCopy ) {
        this( deepCopy ? imageCV.src.clone() : imageCV.src, imageCV.autoswap );
    }

    public ImageCv ( String srcName ) {
        this( srcName, ImageReadType.UnchangedAny );
    }

    @Deprecated
    public ImageCv ( String srcName, int flags ) {
        this( imread( srcName, flags ) );
    }

    public ImageCv ( String srcName, ImageReadType imageReadType ) {
        this( imread( srcName, imageReadType.toInt() ) );
    }

    public ImageCv convertColor ( ColorConversion colorConversion ) {
        cvtColor( src, dst, colorConversion.toInt() );
        return autoswap();
    }

    public ImageCv saveToFile ( String dstName ) {
        imwrite( dstName, src );
        return this;
    }

    /**
     Note: equal to #getSrc()

     @return src
     */
    public Mat getMat () {
        return src;
    }

    public Mat getSrc () {
        return src;
    }

    public Mat getDst () {
        return dst;
    }

    public void setDefaultInterpolation ( Interpolation defaultInterpolation ) {
        this.defaultInterpolation = defaultInterpolation;
    }

    public Interpolation getDefaultInterpolation () {
        return defaultInterpolation;
    }

    public void setAutoswap ( boolean autoswap ) {
        this.autoswap = autoswap;
    }

    public boolean isAutoswap () {
        return autoswap;
    }

    private ImageCv autoswap () {
        return autoswap ? swap() : this;
    }

    public ImageCv swap () {
        Mat tmp = dst;
        dst = src;
        src = tmp;
        return this;
    }

    public ImageCv blitClone () {
        src = dst.clone();
        dst = src;
        return autoswap();
    }

    public ImageCv copy () {
        return new ImageCv( this );
    }

    public ImageCv copy ( boolean deepCopy ) {
        return new ImageCv( this, deepCopy );
    }

    public double norm () {
        return Core.norm( src );
    }

    public double dist ( Mat m2 ) {
        return Core.norm( src, m2 );
    }

    public ImageCv rowRange ( int from, int to ) {
        int rows = src.rows();
        if ( from < 0 ) {
            from += rows;
        }
        if ( to < 0 ) {
            to += rows;
        }
        dst = src.rowRange( from, to );
        return autoswap();
    }

    public ImageCv rect ( Rect rect ) {
        dst = new Mat( src, rect );
        return autoswap();
    }

    public ImageCv equalizeHistogram () {
        equalizeHist( src, dst );
        return autoswap();
    }

    public ImageCv resize ( double factor ) {
        return resize( factor, factor );
    }

    public ImageCv resize ( double factor, Interpolation interpolation ) {
        return resize( factor, factor, interpolation );
    }

    public ImageCv resize ( double factorX, double factorY ) {
        return resize( factorX, factorY, defaultInterpolation );
    }

    public ImageCv resize ( double factorX, double factorY, Interpolation interpolation ) {
        tmpSize.height = 0;
        tmpSize.width = 0;
        // a zero-set Size is needed to actually use x/y factors here
        Imgproc.resize( src, dst, tmpSize, factorX, factorY, interpolation.toInt() );
        return autoswap();
    }

    public ImageCv resize ( Size size ) {
        return resize( size, defaultInterpolation );
    }

    public ImageCv resize ( Size size, Interpolation interpolation ) {
        Imgproc.resize( src, dst, size, 0, 0, interpolation.toInt() );
        return autoswap();
    }

    public ImageCv multiplyElements ( ImageCv imageCv ) {
        Core.multiply( src, imageCv.src, dst, 1.0 / MAX_8BIT_VALUE );
        return autoswap();
    }

    public Scalar toScalar ( double value ) {
        tmpScalar.val[0] = value;
        return tmpScalar;
    }

    public ImageCv multiply ( double scale ) {
        Core.multiply( src, toScalar( scale ), dst );
        return autoswap();
    }

    public ImageCv binarize () {
        Imgproc.threshold( src, dst, 0, MAX_8BIT_VALUE, THRESH_OTSU );
        return autoswap();
    }

    public ImageCv binarize ( double threshold ) {
        return threshold( threshold, ThresholdType.Binary );
    }

    public ImageCv threshold ( double threshold, ThresholdType thresholdType ) {
        Imgproc.threshold( src, dst, threshold, MAX_8BIT_VALUE, thresholdType.toInt() );
        return autoswap();
    }

    public Scalar mean () {
        return Core.mean( src );
    }

    public ImageCv add ( ImageCv imageCv ) {
        Core.add( src, imageCv.src, dst );
        return autoswap();
    }

    public ImageCv sub ( ImageCv imageCv ) {
        Core.subtract( src, imageCv.src, dst );
        return autoswap();
    }

    public ImageCv absdiff ( ImageCv imageCv ) {
        Core.absdiff( src, imageCv.src, dst );
        return autoswap();
    }

    public ImageCv addToElements ( double value ) {
        Core.add( src, toScalar( value ), dst );
        return this;
    }

    public ImageCv bitwiseAnd ( ImageCv imageCv ) {
        Core.bitwise_and( src, imageCv.src, dst );
        return autoswap();
    }

    public ImageCv bitwiseOr ( ImageCv imageCv ) {
        Core.bitwise_or( src, imageCv.src, dst );
        return autoswap();
    }

    public ImageCv bitwiseXor ( ImageCv imageCv ) {
        Core.bitwise_xor( src, imageCv.src, dst );
        return autoswap();
    }

    public ImageCv invertColors () {
        Core.bitwise_not( src, dst );
        return autoswap();
    }

    public ImageCv normalize () {
        Core.normalize( src, dst, 0, MAX_8BIT_VALUE, Core.NORM_MINMAX );
        return autoswap();
    }

    public ImageCv edgeCanny ( double lowThreshold ) {
        return edgeCanny( lowThreshold, DEFAULT_CANNY_RATIO * lowThreshold );
    }

    public ImageCv edgeCanny ( double lowThreshold, double highThreshold ) {
        return edgeCanny( lowThreshold, highThreshold, 3, false );
    }

    /**

     @param lowThreshold
     @param highThreshold
     @param sobelApertureSize 3, 5 or 7
     @param l2gradient should a more precise metric be used?
     @return
     */
    public ImageCv edgeCanny ( double lowThreshold, double highThreshold, int sobelApertureSize, boolean l2gradient ) {
        Canny( src, dst, lowThreshold, highThreshold, sobelApertureSize, l2gradient );
        return autoswap();
    }

    public ImageCv dilate ( Mat kernel ) {
        Imgproc.dilate( src, dst, kernel );
        return autoswap();
    }

    public ImageCv erode ( Mat kernel ) {
        Imgproc.erode( src, dst, kernel );
        return autoswap();
    }

    public ImageCv applyBilateralFilter ( int d, double sigma ) {
        return applyBilateralFilter( d, sigma, sigma );
    }

    public ImageCv applyBilateralFilter ( int d, double sigmaColor, double sigmaSpace ) {
        Imgproc.bilateralFilter( src, dst, d, sigmaColor, sigmaSpace );
        return autoswap();
    }

    /**

     @param strength non-negative; 0 for no blur, any positive higher value corresponds to <code>strength * 2 + 1</code> kernel size
     @return
     */
    public ImageCv applyMedianBlur ( int strength ) {
        if ( strength < 0 ) {
            throw new IllegalArgumentException();
        }
        if ( strength != 0 ) {
            medianBlur( src, dst, strength * 2 + 1 );
        }
        return autoswap();
    }

    /**

     @param strength non-negative; 0 for no blur, any positive higher value corresponds to <code>strength * 2 - 1</code> kernel size
     @return
     */
    public ImageCv applyGaussianBlur ( int strength ) {
        if ( strength < 0 ) {
            throw new IllegalArgumentException();
        }
        if ( strength != 0 ) {
            tmpSize.height = strength * 2 - 1;
            tmpSize.width = tmpSize.height;
            //if ( dst == null ) { dst = new Mat(); }
            GaussianBlur( src, dst, tmpSize, 0 /* auto */ );
        }
        return autoswap();
    }

    // TODO/TMP/FIXME
    public static float lowThreshold = 0.01f, ratio = 3.0f;

    public ImageCv applyDistanceTransform ( int blurStrength ) {
        applyGaussianBlur( blurStrength );
        Imgproc.threshold( src, dst, 0, MAX_8BIT_VALUE, THRESH_OTSU );
        Canny( dst, dst, lowThreshold, lowThreshold * ratio, 3, true );
        Core.bitwise_not( dst, dst );

        distanceTransform( dst, dst, CV_DIST_L2, CV_DIST_MASK_PRECISE );
        Core.normalize( dst, dst, 0, MAX_8BIT_VALUE, Core.NORM_MINMAX );
        //System.out.println( dst.depth() );
        dst.convertTo( dst, CvType.CV_8UC1 );

        return autoswap();
    }
    // end of TODO/TMP/FIXME

    @Override
    public String toString () {
        return src.toString();
    }

    // inner classes etc.
    public enum ThresholdType {
        Binary( THRESH_BINARY ),
        BinaryInv( THRESH_BINARY_INV ),
        Trunc( THRESH_TRUNC ),
        ToZero( THRESH_TOZERO ),
        ToZeroInv( THRESH_TOZERO_INV ),;

        private final int value;

        private ThresholdType ( int val ) {
            this.value = val;
        }

        public int toInt () {
            return value;
        }
    }

    /**

     */
    public enum Interpolation {
        Nearest( Imgproc.INTER_NEAREST ),
        Linear( Imgproc.INTER_LINEAR ),
        Cubic( Imgproc.INTER_CUBIC ),
        Lanczos4( Imgproc.INTER_LANCZOS4 ),
        Area( Imgproc.INTER_AREA ), //
        ;

        private final int value;

        private Interpolation ( int value ) {
            this.value = value;
        }

        public int toInt () {
            return value;
        }
    }

    public enum ColorConversion {
        BGR2GRAY( Imgproc.COLOR_BGR2GRAY ),
        GRAY2BGR( Imgproc.COLOR_GRAY2BGR ), // TODO add the rest of them
        ;

        private final int value;

        private ColorConversion ( int value ) {
            this.value = value;
        }

        public int toInt () {
            return value;
        }
    }

    public enum ImageReadType {
        UnchangedAny( CV_LOAD_IMAGE_ANYDEPTH | CV_LOAD_IMAGE_ANYCOLOR ),
        GrayscaleAny( CV_LOAD_IMAGE_ANYDEPTH ),
        ColorAny( CV_LOAD_IMAGE_ANYDEPTH | CV_LOAD_IMAGE_COLOR ),
        Unchanged8( CV_LOAD_IMAGE_UNCHANGED ),
        Grayscale8( CV_LOAD_IMAGE_GRAYSCALE ),
        Color8( CV_LOAD_IMAGE_COLOR ), //
        ;

        private final int value;

        private ImageReadType ( int value ) {
            this.value = value;
        }

        public int toInt () {
            return value;
        }
    }

}
