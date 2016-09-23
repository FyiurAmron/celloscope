package vax.celloscope;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import static org.opencv.highgui.Highgui.imwrite;
import static org.opencv.imgproc.Imgproc.Canny;
import static vax.celloscope.Main.*;
import vax.opencv.OpenCvUtils;
import vax.util.Vector2i;

/**

 @author toor
 */
public class Test {
    private static void scaleMatchXY ( double norm, double scale, ImageCv.Interpolation interpolation, Rect r1, Rect r2,
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

    public void testMatchXY () {
        counter = 223;

        Rect r1 = new Rect(), r2 = new Rect();
        Vector2i offsets = new Vector2i();
        ImageCv imageCv1cp = imageCv1.copy(), imageCv2cp = imageCv2.copy();

        int offsetMaxBase = 10;
        double dist = OpenCvUtils.matchXY( Main.imageCv1, Main.imageCv2, r1, r2, offsetMaxBase, offsets );
        imageCv1cp.rect( r1 );
        imageCv2cp.rect( r2 );
        imageCv1cp.absdiff( imageCv2cp.getSrc() );
        imageCv1cp.saveToFile( "result1.0.png" );
        logger.log( "REFERENCE SIZE" + "\n"
                + "dist min (scale 1.0): " + Math.round( dist )
                + "\t normed: 1.0 "
                + "\t r1: " + r1 + "\t r2: " + r2 + "\t offsets: " + offsets );

        scaleMatchXY( dist, 2.0, ImageCv.Interpolation.Nearest, r1, r2, offsetMaxBase, offsets );
        scaleMatchXY( dist, 4.0, ImageCv.Interpolation.Nearest, r1, r2, offsetMaxBase, offsets );
        scaleMatchXY( dist, 8.0, ImageCv.Interpolation.Nearest, r1, r2, offsetMaxBase, offsets );
        scaleMatchXY( dist, 2.0, ImageCv.Interpolation.Linear, r1, r2, offsetMaxBase, offsets );
        scaleMatchXY( dist, 4.0, ImageCv.Interpolation.Linear, r1, r2, offsetMaxBase, offsets );
        scaleMatchXY( dist, 8.0, ImageCv.Interpolation.Linear, r1, r2, offsetMaxBase, offsets );
        scaleMatchXY( dist, 2.0, ImageCv.Interpolation.Cubic, r1, r2, offsetMaxBase, offsets );
        scaleMatchXY( dist, 4.0, ImageCv.Interpolation.Cubic, r1, r2, offsetMaxBase, offsets );
        scaleMatchXY( dist, 8.0, ImageCv.Interpolation.Cubic, r1, r2, offsetMaxBase, offsets );
        scaleMatchXY( dist, 2.0, ImageCv.Interpolation.Lanczos4, r1, r2, offsetMaxBase, offsets );
        scaleMatchXY( dist, 4.0, ImageCv.Interpolation.Lanczos4, r1, r2, offsetMaxBase, offsets );
        scaleMatchXY( dist, 8.0, ImageCv.Interpolation.Lanczos4, r1, r2, offsetMaxBase, offsets );
    }

    //DEBUG
    public void testCanny ( ImageCv imageCv ) {
        //threshold( src_gray, dst, threshold_value, MAX_8BIT_VALUE, threshold_type );
        imageCv.applyGaussianBlur( 7 );
        imageCv.binarize();
        Canny( imageCv.getSrc(), imageCv.getDst(), ImageCv.lowThreshold, ImageCv.lowThreshold * ImageCv.ratio, 3, true );

        //imshow( window_name, dst );
        imwrite( "output.png", imageCv.getDst() );
    }
}
