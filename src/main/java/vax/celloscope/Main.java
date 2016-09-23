package vax.celloscope;

import java.awt.event.ActionEvent;
import java.nio.file.Paths;
import javax.swing.*;
import javax.swing.event.ChangeEvent;

import org.opencv.core.*;

import static org.opencv.core.CvType.*;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
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

    public static void main ( String[] args ) {
        // load OpenCV v2.4.11 DLL first
        System.load( Paths.get( "opencv_java2411.dll" ).toAbsolutePath().normalize().toString() );

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
        /*
         int offset = -1;
         r1.y += offset;
         r1.height -= offset;
         r2.height -= offset;
         */
 /* r2.y = r1.y;
         r1.y = 0;
         */
        imageCv1cp.rect( r1 );
        imageCv2cp.rect( r2 );

        imageCv1cp.absdiff( imageCv2cp.getSrc() );
        //imageCv1cp.saveToFile( "result1.png" );
        saveToFile( imageCv1cp, "_01" );

        imageCv1cp.resize( 0.5, Interpolation.Linear );
        saveToFile( imageCv1cp, "_01b" );
        //Core.MinMaxLocResult mmlResult = Core.minMaxLoc( imageCv1cp.getSrc() );
        //logger.log( "max value (1): " + mmlResult.maxVal );
        //imageCv1cp.multiply( 255 / mmlResult.maxVal );
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
        Core.circle( src, center1, dotSize, Color3.Red.getScalar(), thick );
        Core.circle( src, center1, (int) radius1, Color3.Yellow.getScalar(), thin );

        double radius2 = 0.9 * radius1;
        logger.log( "weighted center: (" + avgD[0] + "," + avgD[1] + ") smaller radius: " + radius2 );
        Core.circle( src, center2, dotSize, Color3.Blue.getScalar(), thick );
        Core.circle( src, center2, (int) radius2, Color3.Green.getScalar(), thin );

        double kEnclosing = 0.25, kWeighted = 1 - kEnclosing;
        center3.x = kEnclosing * center1.x + kWeighted * center2.x;
        center3.y = kEnclosing * center1.y + kWeighted * center2.y;
        double radius3 = radius2 * 0.9;
        logger.log( "avg center: (" + avgD[0] + "," + avgD[1] + ") smallest radius: " + radius3 );
        Core.circle( src, center3, dotSize, Color3.Cyan.getScalar(), thick );
        Core.circle( src, center3, (int) radius3, Color3.Magenta.getScalar(), thin );
        saveToFile( imageCv1cp, "_08" );
        double radius4 = radius1 * 1.33;

        imageCv1cp = imageCv1.copy();
        //imageCv1cp.resize( 2.0 );
        imageCv1cp.rect( r1 );
        Scalar mean = imageCv1cp.mean();
        //imageCv1cp.resize( 0.5 );
        imageCv1cp.convertColor( ImageCv.ColorConversion.GRAY2BGR );
        src = imageCv1cp.getSrc();
        Core.circle( src, center1, (int) radius1, Color3.Yellow.getScalar(), thin );
        Core.circle( src, center2, (int) radius2, Color3.Green.getScalar(), thin );
        Core.circle( src, center3, (int) radius3, Color3.Magenta.getScalar(), thin );
        Core.circle( src, center3, (int) radius4, Color3.Red.getScalar(), thick );
        saveToFile( imageCv1cp, "_09" );

        imageCv1cp = imageCv1.copy();
        //imageCv1cp.resize( 2.0 );
        //imageCv1cp.rect( r1 );
        //imageCv1cp.resize( 0.5 );
        center1.x += r1.x;
        center2.x += r1.x;
        center3.x += r1.x;
        center1.y += r1.y;
        center2.y += r1.y;
        center3.y += r1.y;
        imageCv1cp.convertColor( ImageCv.ColorConversion.GRAY2BGR );
        src = imageCv1cp.getSrc();
        Core.circle( src, center1, (int) radius1, Color3.Yellow.getScalar(), thin );
        Core.circle( src, center2, (int) radius2, Color3.Green.getScalar(), thin );
        Core.circle( src, center3, (int) radius3, Color3.Magenta.getScalar(), thin );
        Core.circle( src, center3, (int) radius4, Color3.Red.getScalar(), thick );
        saveToFile( imageCv1cp, "_10" );
        ImageCv //
                roi = new ImageCv( new Mat( src.rows(), src.cols(), CV_8UC1, OpenCvUtils.ZERO ) ),
                //cropped = new ImageCv( new Mat( src.rows(), src.cols(), CV_8UC1, OpenCvUtils.ONE ) );
                masked = new ImageCv( imageCv1 ),
                bkgd = new ImageCv( new Mat( src.rows(), src.cols(), CV_8UC1, mean ) );
        masked.setAutoswap( true );
        roi.setAutoswap( true );
        bkgd.setAutoswap( true );
        Core.circle( roi.getSrc(), center3, (int) ( radius1 * 1.0 ), Color3.White.getScalar(), filled );
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
/*
         masked.getSrc().convertTo( masked.getDst(), CV_32SC1 );
         masked.swap();
         Core.subtract( masked.getSrc(), mean, masked.getDst(), new Mat(), CV_32SC1 );
         masked.swap();
         */
//masked.applyGaussianBlur( 5 );
        //masked.applyMedianBlur( 5 );
        //Core.absdiff( masked.getSrc(), mean, masked.getDst() );
        //masked.swap();
        saveToFile( masked, "_11prep0" );
        /*
         masked.getSrc().convertTo( masked.getDst(), CV_8UC1 );
         masked.swap();
         */
        //masked.swap();
        Imgproc.bilateralFilter( masked.getSrc(), masked.getDst(), 5, 200, 200 );
        masked.swap();
        //masked.threshold( 5, ImageCv.ThresholdType.ToZero );
        masked.normalize();
        saveToFile( masked, "_11prep1" );
        Imgproc.bilateralFilter( masked.getSrc(), masked.getDst(), 5, 100, 100 );
        //masked.threshold( 28, ImageCv.ThresholdType.ToZero );
        //masked.swap();
        masked.applyMedianBlur( 2 );
        masked.applyMedianBlur( 2 );
        //masked.applyMedianBlur( 5 );
        //masked.applyMedianBlur( 5 );
        //masked.threshold( 32, ImageCv.ThresholdType.ToZero );
        //masked.applyGaussianBlur( 3 );
        //masked.applyMedianBlur( 3 );
        saveToFile( masked, "_11prep2" );
        /*

         saveToFile( masked, "_11prnorm" );
         masked.applyGaussianBlur( 15 );
         saveToFile( masked, "_11prnormask" );
         //masked.binarize();
         //saveToFile( masked, "_11prep2" );
         masked.normalize();
         saveToFile( masked, "_11prnorm2" );
         */
        float lowThreshold = 64f, ratio = 3f;
        //Imgproc.Canny( masked.getSrc(), masked.getDst(), lowThreshold, lowThreshold * ratio,7, true );
        Imgproc.Canny( masked.getSrc(), masked.getDst(), lowThreshold, lowThreshold * ratio );
        masked.swap();
        saveToFile( masked, "_12" );

    }

    public static void processImage (/* Rect r1, Rect r2, Vector2i offsets */) {

    }

    public static void processAllImages () {
        imageCv2 = loadNextImage();
        long start = System.currentTimeMillis();
        Rect r1 = new Rect(), r2 = new Rect();
        Vector2i offsets = new Vector2i();

        counter = 50;
        while( counter < 80 ) {
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
