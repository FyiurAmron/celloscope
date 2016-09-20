package vax.celloscope;

import java.awt.event.ActionEvent;
import java.nio.file.Paths;
import javax.swing.*;
import javax.swing.event.ChangeEvent;

import org.opencv.core.*;

import vax.celloscope.ImageCv.Interpolation;
import vax.opencv.MatBufferedImage;
import vax.opencv.OpenCvUtils;
import vax.util.Logger;
import vax.util.StringUtils;
import vax.util.Vector2i;
import vax.gui.ImagePanel;

/**

 @author toor
 */
public class Main {
    private final static String WINDOW_NAME = "Threshold Demo";

    public final static Logger logger = new Logger();

    private static ImageCv imageCv1, imageCv2;
    private static int counter = 0; // actual images start from 1

    public static ImageCv loadNextImage () {
        counter++;
        return loadImage( counter );
    }

    public static ImageCv loadImage ( int nr ) {
        ImageCv imageCV = new ImageCv( "data/" + StringUtils.toStringPadded( nr, 4 ) + ".png" );
        return imageCV.rowsFrom( 1 ).applyMedianBlur( 1 ).swap().applyMedianBlur( 1 ).swap(); // initial filtering
    }

    private static void scaleMatchXY ( double norm, double scale, Interpolation interpolation, Rect r1, Rect r2,
            int offsetMaxBase, Vector2i offsets ) {
        ImageCv imageCv1cp = imageCv1.copy(),
                imageCv2cp = imageCv2.copy();
        imageCv1cp.resize( scale, interpolation );
        imageCv2cp.resize( scale, interpolation );
        double dist = OpenCvUtils.matchXY( imageCv1cp, imageCv2cp, r1, r2, (int) ( offsetMaxBase * scale ), offsets );
        imageCv1cp.rect( r1 );
        imageCv2cp.rect( r2 );

        imageCv1cp.absdiff( imageCv2cp.getSrc() );
        imageCv1cp.saveToFile( "result" + scale + "_" + interpolation + ".png" );

        logger.log( "Interpolation: " + interpolation + "\n"
                + "dist min (scale " + scale + "): " + Math.round( dist )
                + "\t normed: " + Math.round( dist / scale / norm * 1000 ) / 1000.0
                + "\t r1: " + r1 + "\t r2: " + r2 + "\t offsets: " + offsets );
    }

    public static void main ( String[] args ) {
        // load OpenCV v2.4.11 DLL first
        System.load( Paths.get( "opencv_java2411.dll" ).toAbsolutePath().normalize().toString() );

        // UI init
        JFrame jfMain = new JFrame( WINDOW_NAME );
        JFrame jfControl = new JFrame( "Control Panel" );
        jfMain.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        jfControl.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        jfControl.setLayout( new BoxLayout( jfControl.getContentPane(), BoxLayout.Y_AXIS ) );

        counter = 223;
        imageCv1 = loadNextImage();
        imageCv2 = loadNextImage();
        //imageCv1 = new ImageCv( "img01.png" );
        //imageCv2 = new ImageCv( "img02.png" );
        imageCv1.setAutoswap( true );
        imageCv2.setAutoswap( true );

        Rect r1 = new Rect(), r2 = new Rect();
        Vector2i offsets = new Vector2i();
        ImageCv imageCv1cp = imageCv1.copy(), imageCv2cp = imageCv2.copy();

        int offsetMaxBase = 10;
        double dist = OpenCvUtils.matchXY( imageCv1, imageCv2, r1, r2, offsetMaxBase, offsets );
        imageCv1cp.rect( r1 );
        imageCv2cp.rect( r2 );
        imageCv1cp.absdiff( imageCv2cp.getSrc() );
        imageCv1cp.saveToFile( "result1.0.png" );
        logger.log( "REFERENCE SIZE" + "\n"
                + "dist min (scale 1.0): " + Math.round( dist )
                + "\t normed: 1.0 "
                + "\t r1: " + r1 + "\t r2: " + r2 + "\t offsets: " + offsets );

        scaleMatchXY( dist, 2.0, Interpolation.Nearest, r1, r2, offsetMaxBase, offsets );
        scaleMatchXY( dist, 4.0, Interpolation.Nearest, r1, r2, offsetMaxBase, offsets );
        scaleMatchXY( dist, 8.0, Interpolation.Nearest, r1, r2, offsetMaxBase, offsets );
        scaleMatchXY( dist, 2.0, Interpolation.Linear, r1, r2, offsetMaxBase, offsets );
        scaleMatchXY( dist, 4.0, Interpolation.Linear, r1, r2, offsetMaxBase, offsets );
        scaleMatchXY( dist, 8.0, Interpolation.Linear, r1, r2, offsetMaxBase, offsets );
        scaleMatchXY( dist, 2.0, Interpolation.Cubic, r1, r2, offsetMaxBase, offsets );
        scaleMatchXY( dist, 4.0, Interpolation.Cubic, r1, r2, offsetMaxBase, offsets );
        scaleMatchXY( dist, 8.0, Interpolation.Cubic, r1, r2, offsetMaxBase, offsets );
        scaleMatchXY( dist, 2.0, Interpolation.Lanczos4, r1, r2, offsetMaxBase, offsets );
        scaleMatchXY( dist, 4.0, Interpolation.Lanczos4, r1, r2, offsetMaxBase, offsets );
        scaleMatchXY( dist, 8.0, Interpolation.Lanczos4, r1, r2, offsetMaxBase, offsets );

        if ( true ) {
            return;
        }

        /*
         Core.MinMaxLocResult mmlresult = Core.minMaxLoc( imageCV1.getSrc() );
         //System.out.println( mmlresult.maxVal );
         imageCV1.multiply( 255 / mmlresult.maxVal );
         imageCV1.threshold( 32, ImageCv.ThresholdType.ToZero );
         imageCV1.applyMedianBlur( 1 );
         imageCV1.applyMedianBlur( 2 );
         mmlresult = Core.minMaxLoc( imageCV1.getSrc() );
         //System.out.println( mmlresult.maxVal );
         imageCV1.multiply( 255 / mmlresult.maxVal );
         imageCV1.binarize();
         float[] radius = new float[1];
         Point center = new Point();
         Mat nonzero = new Mat();
         Core.findNonZero( imageCV1.getSrc(), nonzero );
         MatOfPoint mop = new MatOfPoint( nonzero );
         MatOfPoint2f mop2f = new MatOfPoint2f( mop.toArray() );
         Imgproc.minEnclosingCircle( mop2f, center, radius );
         //Core.circle( imageCv1.getSrc(), center, (int) radius[0], new Scalar( 255, 255, 255 ), 3 );
         */
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
