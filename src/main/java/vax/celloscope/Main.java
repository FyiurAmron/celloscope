package vax.celloscope;

import java.awt.event.ActionEvent;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;

import org.opencv.core.*;

import static org.opencv.core.CvType.*;
import org.opencv.imgproc.Imgproc;
import static org.opencv.imgproc.Imgproc.CV_HOUGH_GRADIENT;
import static org.opencv.imgproc.Imgproc.MORPH_ELLIPSE;
import vax.celloscope.ImageCv.Interpolation;
import vax.opencv.MatBufferedImage;
import vax.opencv.OpenCvUtils;
import vax.util.Logger;
import vax.util.StringUtils;
import vax.util.Vector2i;
import vax.gui.ImagePanel;
import vax.opencv.Color3;

/**

 @author toor
 */
public class Main {
    private final static String WINDOW_NAME = "Threshold Demo";

    public final static Logger logger = new Logger();

    public static ImageCv imageCv1, imageCv2;
    public static int counter = 0; // actual images start from 1

    public static final int PRESCAN_COUNT = 10;

    public static ImageCv loadNextImage () {
        counter++;
        return loadImage( counter );
    }

    public static ImageCv loadImage ( int nr ) {
        ImageCv imageCv = new ImageCv( "data/" + StringUtils.toStringPadded( nr, 4 ) + ".png", ImageCv.ImageReadType.Grayscale8 );
        imageCv.setAutoswap( true );
        return imageCv.rowRange( 1, -1 ).applyMedianBlur( 1 ).applyMedianBlur( 1 ); // initial filtering
    }

    public static InputStream getResourceStream ( Class<?> resourceClass, String resourceName ) {
        return resourceClass.getResourceAsStream(
                "/" + resourceClass.getPackage().getName().replace( '.', '/' ) + "/" + resourceName );
    }

    public static void loadLibrary ( Class<?> resourceClass, String nativeLibraryName ) throws IOException {
        String fullName = System.mapLibraryName( nativeLibraryName );
        Path libPath = Paths.get( fullName );
        if ( !libPath.toFile().exists() ) {
            Files.copy( getResourceStream( resourceClass, fullName ), libPath ); // extracts DLL to running dir
        }
        System.loadLibrary( nativeLibraryName );
    }

    public static void main ( String[] args ) throws URISyntaxException, IOException {
        // load OpenCV DLL first
        loadLibrary( org.opencv.NativeDll.class, "opencv_java310" );
        processAllImages();
        //initUI();
    }

    private static void saveToFile ( ImageCv imageCv, String suffix ) {
        imageCv.saveToFile( "output/diff" + StringUtils.toStringPadded( counter - 1, 3 ) + suffix + ".png" );
    }

    public static void prescanImage ( Rect r1, Rect r2, Vector2i offsets ) {
        ImageCv imageCv1cp, imageCv2cp;
        //imageCv2 = new ImageCv( "img02.png" );

        imageCv1cp = imageCv1.copy();
        imageCv2cp = imageCv2.copy();
        imageCv1cp.resize( 2.0, Interpolation.Linear );
        imageCv2cp.resize( 2.0, Interpolation.Linear );
        double dist = OpenCvUtils.matchXY( imageCv1cp, imageCv2cp, r1, r2, 10 * 2, offsets );

        imageCv1cp.rect( r1 );
        imageCv2cp.rect( r2 );

        imageCv1cp.absdiff( imageCv2cp );
        saveToFile( imageCv1cp, "_01" );

        imageCv1cp.resize( 0.5, Interpolation.Linear );
        saveToFile( imageCv1cp, "_01b" );
        imageCv1cp.normalize();
        saveToFile( imageCv1cp, "_02" );
        imageCv1cp.threshold( 32, ImageCv.ThresholdType.ToZero );
        saveToFile( imageCv1cp, "_03" );
        imageCv1cp.applyMedianBlur( 1 );
        saveToFile( imageCv1cp, "_04" );
        imageCv1cp.applyMedianBlur( 2 );
        saveToFile( imageCv1cp, "_05" );
        imageCv1cp.normalize();
        saveToFile( imageCv1cp, "_06" );
        ImageCv m06 = imageCv1cp.copy();
        imageCv1cp.binarize();
        saveToFile( imageCv1cp, "_07" );
        imageCv1cp.applyMedianBlur( 4 );
        saveToFile( imageCv1cp, "_07b" );
        float[] radiusArr = new float[1];
        Point center1 = new Point(), center2 = new Point(), center3 = new Point();
        Mat nonzero = new Mat(), avg = new Mat();
        Core.findNonZero( imageCv1cp.getSrc(), nonzero );
        MatOfPoint mop = new MatOfPoint( nonzero );
        MatOfPoint2f mop2f = new MatOfPoint2f( mop.toArray() );

        int //
                cellSizeMaxX = 400, // src.cols();
                cellSizeMaxY = 400; // src.rows();
        Core.reduce( mop2f, avg, 0, Core.REDUCE_AVG, CV_32FC2 );
        double[] avgD = avg.get( 0, 0 );
        center2.set( avgD ); // weighted avg center
        // halfcrop first!
        Mat src = imageCv1cp.getSrc();
        r1.width = cellSizeMaxX;
        r1.height = cellSizeMaxY;
        if ( center2.x + 0.5 * cellSizeMaxX > src.cols() ) {
            r1.x = r1.width - cellSizeMaxX;
        } else if ( center2.x < 0.5 * cellSizeMaxX ) {
            r1.x = 0;
        } else {
            r1.x = (int) ( center2.x - 0.5 * cellSizeMaxX );
        }
        if ( center2.y + 0.5 * cellSizeMaxY > src.rows() ) {
            r1.y = r1.height - cellSizeMaxY;
        } else if ( center2.y < 0.5 * cellSizeMaxY ) {
            r1.y = 0;
        } else {
            r1.y = (int) ( center2.y - 0.5 * cellSizeMaxY );
        }
        try {
            imageCv1cp.rect( r1 );
        } catch (CvException ex) {
            logger.warning( "" + ex + "\n" + r1 );
        }

        // now check circle-y enclosings
        Core.findNonZero( imageCv1cp.getSrc(), nonzero );
        mop = new MatOfPoint( nonzero );
        mop2f = new MatOfPoint2f( mop.toArray() );
        Imgproc.minEnclosingCircle( mop2f, center1, radiusArr );
        Core.reduce( mop2f, avg, 0, Core.REDUCE_AVG, CV_32FC2 );
        avgD = avg.get( 0, 0 );
        center2.set( avgD ); // weighted avg center, take 2
        float radius1 = radiusArr[0];

        if ( radius1 > 200 ) {
            logger.warning( "[" + counter + "] radius > 200!" );
        }

        logger.log( "center: " + center1 + " radius: " + radius1 );
        imageCv1cp.convertColor( ImageCv.ColorConversion.GRAY2BGR );
        src = imageCv1cp.getSrc();
        int thin = 1, thick = 3, filled = -1, dotSize = 3;
        Imgproc.circle( src, center1, dotSize, Color3.Red.getScalar(), thick );
        Imgproc.circle( src, center1, (int) radius1, Color3.Yellow.getScalar(), thin );

        double radius2 = 0.9 * radius1;
        logger.log( "weighted center: (" + avgD[0] + "," + avgD[1] + ") smaller radius: " + radius2 );
        Imgproc.circle( src, center2, dotSize, Color3.Blue.getScalar(), thick );
        Imgproc.circle( src, center2, (int) radius2, Color3.Green.getScalar(), thin );

        double kEnclosing = 0.25, kWeighted = 1 - kEnclosing;
        center3.x = kEnclosing * center1.x + kWeighted * center2.x;
        center3.y = kEnclosing * center1.y + kWeighted * center2.y;
        double radius3 = radius2 * 0.9;
        logger.log( "avg center: (" + avgD[0] + "," + avgD[1] + ") smallest radius: " + radius3 );
        Imgproc.circle( src, center3, dotSize, Color3.Cyan.getScalar(), thick );
        Imgproc.circle( src, center3, (int) radius3, Color3.Magenta.getScalar(), thin );
        saveToFile( imageCv1cp, "_08" );
        double radius4 = radius1 * 1.33;

        imageCv1cp = imageCv1.copy();
        imageCv1cp.rect( r1 );
        Scalar mean = imageCv1cp.mean();
        imageCv1cp.convertColor( ImageCv.ColorConversion.GRAY2BGR );
        src = imageCv1cp.getSrc();
        Imgproc.circle( src, center1, (int) radius1, Color3.Yellow.getScalar(), thin );
        Imgproc.circle( src, center2, (int) radius2, Color3.Green.getScalar(), thin );
        Imgproc.circle( src, center3, (int) radius3, Color3.Magenta.getScalar(), thin );
        Imgproc.circle( src, center3, (int) radius4, Color3.Red.getScalar(), thick );
        saveToFile( imageCv1cp, "_09" );

        imageCv1cp = imageCv1.copy();
        center1.x += r1.x;
        center2.x += r1.x;
        center3.x += r1.x;
        center1.y += r1.y;
        center2.y += r1.y;
        center3.y += r1.y;
        imageCv1cp.convertColor( ImageCv.ColorConversion.GRAY2BGR );
        src = imageCv1cp.getSrc();
        Imgproc.circle( src, center1, (int) radius1, Color3.Yellow.getScalar(), thin );
        Imgproc.circle( src, center2, (int) radius2, Color3.Green.getScalar(), thin );
        Imgproc.circle( src, center3, (int) radius3, Color3.Magenta.getScalar(), thin );
        Imgproc.circle( src, center3, (int) radius4, Color3.Red.getScalar(), thick );
        saveToFile( imageCv1cp, "_10" );
        ImageCv //
                roi = new ImageCv( new Mat( src.rows(), src.cols(), CV_8UC1, OpenCvUtils.ZERO ) ),
                //cropped = new ImageCv( new Mat( src.rows(), src.cols(), CV_8UC1, OpenCvUtils.ONE ) );
                masked = new ImageCv( imageCv1 ),
                bkgd = new ImageCv( new Mat( src.rows(), src.cols(), CV_8UC1, mean ) );
        masked.setAutoswap( true );
        roi.setAutoswap( true );
        bkgd.setAutoswap( true );
        Imgproc.circle( roi.getSrc(), center3, (int) ( radius1 * 1.0 ), Color3.White.getScalar(), filled );
        roi.applyGaussianBlur( 120 );
        saveToFile( roi, "_10roi" );
        //saveToFile( cropped, "_10cropped" );

        masked.multiplyElements( roi );
        roi.invertColors();
        //roi.multiply( -1 );
        //roi.addToElements( 255 );
        bkgd.multiplyElements( roi ); //imageCv1.getSrc().copyTo( cropped.getSrc(), roi.getSrc() );
        saveToFile( bkgd, "_10bkgd" );
        saveToFile( masked, "_11" );
        masked.add( bkgd );
        saveToFile( masked, "_11bkgd" );
        // 100% OK till this point

        ImageCv can, morpho;
        double lowThreshold = 64, k1 = 1, k2 = 2, k3 = 3, k4 = 4;
        Size //
                ksize1 = new Size( k1, k1 ),
                ksize2 = new Size( k2, k2 ),
                ksize3 = new Size( k3, k3 ),
                ksize4 = new Size( k4, k4 );
        Mat kernel1 = Imgproc.getStructuringElement( MORPH_ELLIPSE, ksize1 );
        Mat kernel2 = Imgproc.getStructuringElement( MORPH_ELLIPSE, ksize2 );
        Mat kernel3 = Imgproc.getStructuringElement( MORPH_ELLIPSE, ksize3 );
        Mat kernel4 = Imgproc.getStructuringElement( MORPH_ELLIPSE, ksize4 );
        //Mat kernel3 = new Mat( 1, 1, CV_8UC1, new Scalar( 1 ) );
        //new Mat( 3, 3, CV_8UC1, OpenCvUtils.ONE );
        ImageCv cpy = masked.copy(), cantest = masked.copy();
        masked.applyMedianBlur( 5 );
        masked.applyMedianBlur( 3 );
        masked.applyMedianBlur( 2 );
        masked.applyMedianBlur( 2 );
        //masked.applyBilateralFilter( 9, 200);
        //masked.applyMedianBlur( 2 );
        //masked.applyMedianBlur( 2 );
        //masked.applyMedianBlur( 2 );
        //masked.applyMedianBlur( 2 );
        //masked.applyMedianBlur( 2 );
        //masked.applyMedianBlur( 2 );
        cpy.sub( masked );
        saveToFile( cpy, "_11cpysub" );
        cpy.normalize();
        cpy.applyGaussianBlur( 3 );
        cpy.edgeCanny( 20, 60 );
        saveToFile( cpy, "_11cpysubcanny" );
        saveToFile( masked, "_11masked" );
        masked.edgeCanny( 15, 45, 3, true );
        saveToFile( masked, "_11maskedcan" );
        //masked.applyGaussianBlur( 20 );
        Mat circles = new Mat();
        Imgproc.HoughCircles( masked.getSrc(), circles, CV_HOUGH_GRADIENT,
                1, 10, 15, 45, (int) ( 0.2 * radius1 ), (int) radius1 );
        Point hcenter = new Point();
        double hradius = 0;
        for( int x = 0; x < circles.cols(); x++ ) {
            double[] vec = circles.get( 0, x );
            hcenter.x = vec[0];
            hcenter.y = vec[1];
            hradius = vec[2];
            Imgproc.circle( masked.getSrc(), hcenter, (int) hradius, Color3.White.getScalar(), 1 );
        }
        saveToFile( masked, "_11maskedcircles" );
        Imgproc.HoughCircles( m06.getSrc(), circles, CV_HOUGH_GRADIENT,
                1, (int) ( 0.25 * radius1 ), 15, 45, (int) ( 0.5 * radius1 ), (int) ( radius1 ) );
        for( int x = 0; x < circles.cols(); x++ ) {
            double[] vec = circles.get( 0, x );
            hcenter.x = vec[0];
            hcenter.y = vec[1];
            hradius = vec[2];
            Imgproc.circle( m06.getSrc(), hcenter, (int) hradius, Color3.White.getScalar(), 1 );
        }
        saveToFile( m06, "_06circles" );

        can = masked.copy();
        can.edgeCanny( 15, 45, 3, true );
        saveToFile( can, "_11canpre1" );

        morpho = can.copy();
        morpho.dilate( kernel3 );
        morpho.erode( kernel4 );
        //morpho.dilate( kernel3 );
        //can.bitwiseAnd( morpho );
        saveToFile( morpho, "_11morpho" );
        saveToFile( can, "_11can" );
        saveToFile( masked, "_11prep0" );
        /*
         masked.getSrc().convertTo( masked.getDst(), CV_8UC1 );
         masked.swap();
         */
        //masked.swap();
        masked.applyBilateralFilter( 5, 200 );
        //masked.threshold( 5, ImageCv.ThresholdType.ToZero );
        masked.normalize();
        saveToFile( masked, "_11prep1" );
        masked.applyBilateralFilter( 5, 100 );
        //masked.threshold( 28, ImageCv.ThresholdType.ToZero );
        //masked.swap();
        masked.applyMedianBlur( 2 );
        masked.applyMedianBlur( 2 );

        saveToFile( masked, "_11prep2" );

        can = cantest.copy();
        can.edgeCanny( lowThreshold );
        saveToFile( can, "_12a" );

        can = cantest.copy();
        can.edgeCanny( lowThreshold * 1.25 );
        saveToFile( can, "_12b" );

        can = cantest.copy();
        can.edgeCanny( lowThreshold * 0.66 );
        saveToFile( can, "_12c" );
        /*
         can.dilate( kernel );
         can.erode( kernel );
         */
        saveToFile( can, "_13" );
    }

    public static void processImage (/* Rect r1, Rect r2, Vector2i offsets */) {

    }

    public static void processAllImages () {
        imageCv2 = loadNextImage();
        long start = System.currentTimeMillis();
        Rect r1 = new Rect(), r2 = new Rect();
        Vector2i offsets = new Vector2i();

        counter = 1;
        while( counter < 2/* 79 */ ) {
            imageCv1 = imageCv2;
            imageCv2 = loadNextImage(); // new ImageCv( "img01.png" );
            prescanImage( r1, r2, offsets );
        }
        /*
         while( counter < PRESCAN_COUNT ) {
         imageCv1 = imageCv2;
         imageCv2 = loadNextImage(); // new ImageCv( "img01.png" );
         prescanImage( r1, r2, offsets );
         processImage();
         }
         while( counter < 279 ) {
         imageCv1 = imageCv2;
         imageCv2 = loadNextImage(); // new ImageCv( "img01.png" );
         prescanImage( r1, r2, offsets ); // TEMP!
         processImage();
         }
         */
        logger.log( "elapsed time: " + ( System.currentTimeMillis() - start ) + " ms" );
        /*
         if ( true ) {
         return;
         }
         */
    }

    public static void initUI () {
        JFrame jfMain = new JFrame( WINDOW_NAME );
        JFrame jfControl = new JFrame( "Control Panel" );
        jfMain.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        jfControl.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        jfControl.setLayout( new BoxLayout( jfControl.getContentPane(), BoxLayout.Y_AXIS ) );

        MatBufferedImage mbi1 = new MatBufferedImage( imageCv1.getSrc() );
        MatBufferedImage mbi2 = new MatBufferedImage( imageCv2.getSrc() );

        //ImageIcon ii = new ImageIcon(mbi );
        //JLabel ip = new JLabel( ii );
        ImagePanel ip = new ImagePanel( mbi1 );
        jfMain.add( ip );
        imageCv1.setAutoswap( false );

        JToggleButton jbApplyDistanceTransform = new JToggleButton( "apply distance transf." );
        JSlider jsGaussianBlurSize = new JSlider( 0, 100, 0 );
        jsGaussianBlurSize.addChangeListener( (ChangeEvent e) -> {
            mbi1.updateBufferedImage( ( jbApplyDistanceTransform.isSelected()
                    ? imageCv1.applyDistanceTransform( jsGaussianBlurSize.getValue() )
                    : imageCv1.applyGaussianBlur( jsGaussianBlurSize.getValue() ) ).getDst() );
            ip.repaint();
        } );
        jfControl.add( new JLabel( "Gaussian Blur:" ) );
        jfControl.add( jsGaussianBlurSize );

        JSlider jsMedianBlurSize = new JSlider( 0, 100, 0 );
        jsMedianBlurSize.addChangeListener( (ChangeEvent e) -> {
            mbi1.updateBufferedImage( imageCv1.applyMedianBlur( jsMedianBlurSize.getValue() ).getDst() );
            ip.repaint();
        } );
        jfControl.add( new JLabel( "Median Blur:" ) );
        jfControl.add( jsMedianBlurSize );

        jbApplyDistanceTransform.addActionListener( (ActionEvent e) -> {
            if ( jbApplyDistanceTransform.isSelected() ) {
                mbi1.updateBufferedImage( imageCv1.applyDistanceTransform( jsGaussianBlurSize.getValue() ).getDst() );
                ip.repaint();
            }
        } );
        jfControl.add( jbApplyDistanceTransform );

        jfMain.pack();
        jfMain.setVisible( true );

        jfControl.pack();
        jfControl.setLocation( jfMain.getX() + jfMain.getWidth(), 0 );
        jfControl.setVisible( true );
    }
}
