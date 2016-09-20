package vax.celloscope;

import org.opencv.core.*;
import static org.opencv.imgproc.Imgproc.*;
import static org.opencv.highgui.Highgui.*;
import org.opencv.imgproc.Imgproc;

/**

 @author toor
 */
public class ImageCv {
    /**

     */
    public enum Interpolation {
        Nearest( Imgproc.INTER_NEAREST ),
        Linear( Imgproc.INTER_LINEAR ),
        Cubic( Imgproc.INTER_CUBIC ),
        Lanczos4( Imgproc.INTER_LANCZOS4 ),
        Area( Imgproc.INTER_AREA ),;

        private final int value;

        private Interpolation ( int value ) {
            this.value = value;
        }

        public int getValue () {
            return value;
        }
    }

    final static public int MAX_8BIT_VALUE = 255;

    private Mat src;
    private Mat dst = new Mat();
    private final Size tmpSize = new Size();
    private final Scalar tmpScalar = new Scalar( 0 );

    private boolean autoswap = false;
    private Interpolation defaultInterpolation = Interpolation.Cubic;

    private float lowThreshold = 0.01f, ratio = 3.0f;

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
        this( imread( srcName, 1 ) );
        cvtColor( src, src, COLOR_BGR2GRAY );
    }

    public ImageCv saveToFile ( String dstName ) {
        imwrite( dstName, src );
        return autoswap();
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

    public ImageCv sub ( Mat m2 ) {
        Core.subtract( src, m2, dst );
        return autoswap();
    }

    public ImageCv absdiff ( Mat m2 ) {
        Core.absdiff( src, m2, dst );
        return autoswap();
    }

    public double norm () {
        return Core.norm( src );
    }

    public double dist ( Mat m2 ) {
        return Core.norm( src, m2 );
    }

    public ImageCv rowsFrom ( int nr ) {
        src = src.rowRange( nr, src.rows() );
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
        Imgproc.resize( src, dst, tmpSize, factorX, factorY, interpolation.getValue() );
        return autoswap();
    }

    public ImageCv resize ( Size size ) {
        return resize( size, defaultInterpolation );
    }

    public ImageCv resize ( Size size, Interpolation interpolation ) {
        Imgproc.resize( src, dst, size, 0, 0, interpolation.getValue() );
        return autoswap();
    }

    public ImageCv multiply ( double scale ) {
        tmpScalar.val[0] = scale;
        Core.multiply( src, tmpScalar, dst );
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
        Imgproc.threshold( src, dst, threshold, MAX_8BIT_VALUE, thresholdType.getVal() );
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

    public ImageCv applyDistanceTransform ( int blurStrength ) {
        applyGaussianBlur( blurStrength );
        Imgproc.threshold( dst, dst, 0, MAX_8BIT_VALUE, THRESH_OTSU );
        Canny( dst, dst, lowThreshold, lowThreshold * ratio, 3, true );
        Core.bitwise_not( dst, dst );

        distanceTransform( dst, dst, CV_DIST_L2, CV_DIST_MASK_PRECISE );
        Core.normalize( dst, dst, 0, 255, Core.NORM_MINMAX );
        //System.out.println( dst.depth() );
        dst.convertTo( dst, CvType.CV_8UC1 );

        return autoswap();
    }

    //DEBUG
    public Mat test () {
        //threshold( src_gray, dst, threshold_value, MAX_8BIT_VALUE, threshold_type );
        applyGaussianBlur( 7 );
        binarize();
        Canny( dst, dst, lowThreshold, lowThreshold * ratio, 3, true );

        //imshow( window_name, dst );
        imwrite( "output.png", dst );
        return dst;
    }

    @Override
    public String toString () {
        return src.toString();
    }

    public enum ThresholdType {
        Binary( THRESH_BINARY ),
        BinaryInv( THRESH_BINARY_INV ),
        Trunc( THRESH_TRUNC ),
        ToZero( THRESH_TOZERO ),
        ToZeroInv( THRESH_TOZERO_INV ),;

        private final int val;

        private ThresholdType ( int val ) {
            this.val = val;
        }

        public int getVal () {
            return val;
        }

    }
}
