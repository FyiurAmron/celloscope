package vax.celloscope;

import java.awt.event.ActionEvent;
import java.nio.file.Paths;
import javax.swing.*;
import javax.swing.event.ChangeEvent;

import org.opencv.core.*;

import static org.opencv.core.CvType.*;
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

        processImages();
        //initUI();
    }

    private static void saveToFile ( ImageCv imageCv, String suffix ) {
        imageCv.saveToFile( "output/diff" + StringUtils.toStringPadded( counter - 1, 3 ) + suffix + ".png" );
    }

    public static void processImages () {
        //counter = 200;
        imageCv2 = loadNextImage();
        ImageCv imageCv1cp, imageCv2cp;
        Rect r1 = new Rect(), r2 = new Rect();
        Vector2i offsets = new Vector2i();
        long start = System.currentTimeMillis();
        while( counter < 279 /* 279 */ ) {
            imageCv1 = imageCv2;
            imageCv2 = loadNextImage(); // new ImageCv( "img01.png" );
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
            saveToFile( imageCv1cp, "_1" );

            imageCv1cp.resize( 0.5, Interpolation.Linear );
            saveToFile( imageCv1cp, "_1b" );
            Core.MinMaxLocResult mmlresult = Core.minMaxLoc( imageCv1cp.getSrc() );
            logger.log( "max value (1): " + mmlresult.maxVal );
            imageCv1cp.multiply( 255 / mmlresult.maxVal );
            saveToFile( imageCv1cp, "_2" );
            imageCv1cp.threshold( 32, ImageCv.ThresholdType.ToZero );
            saveToFile( imageCv1cp, "_3" );
            imageCv1cp.applyMedianBlur( 1 );
            saveToFile( imageCv1cp, "_4" );
            imageCv1cp.applyMedianBlur( 2 );
            saveToFile( imageCv1cp, "_5" );
            mmlresult = Core.minMaxLoc( imageCv1.getSrc() );
            logger.log( "max value (2):" + mmlresult.maxVal );
            imageCv1cp.multiply( 255 / mmlresult.maxVal );
            saveToFile( imageCv1cp, "_6" );
            imageCv1cp.binarize();
            saveToFile( imageCv1cp, "_7" );
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
            /*
             if ( radius1 > 200 ) {
             logger.warning( "[" + counter + "] radius > 200!" );
             }
             */
            logger.log( "center: " + center1 + " radius: " + radius1 );
            imageCv1cp.convertColor( ImageCv.ColorConversion.GRAY2BGR );
            src = imageCv1cp.getSrc();
            int thin = 1, thick = 3, dotSize = 3;
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
            saveToFile( imageCv1cp, "_8" );

            imageCv1cp = imageCv1.copy();
            //imageCv1cp.resize( 2.0 );
            imageCv1cp.rect( r1 );
            //imageCv1cp.resize( 0.5 );
            imageCv1cp.convertColor( ImageCv.ColorConversion.GRAY2BGR );
            src = imageCv1cp.getSrc();
            Core.circle( src, center1, (int) radius1, Color3.Yellow.getScalar(), thin );
            Core.circle( src, center2, (int) radius2, Color3.Green.getScalar(), thin );
            Core.circle( src, center3, (int) radius3, Color3.Magenta.getScalar(), thin );
            saveToFile( imageCv1cp, "_9" );
        }
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
