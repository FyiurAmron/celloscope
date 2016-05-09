package vax.celloscope;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import javax.swing.JFrame;

import org.opencv.core.Mat;
import static org.opencv.imgproc.Imgproc.*;
import static org.opencv.highgui.Highgui.*;

/**

 @author toor
 */
public class Binarize {
    final static public int MAX_8BIT_VALUE = 255;

    private Mat src;
    private final Mat srcGray = new Mat(), dst = new Mat();

    public Mat loadImage ( String srcName ) {
        // Load an image
        src = imread( srcName, 1 );

        // Convert the image to Gray
        cvtColor( src, srcGray, COLOR_BGR2GRAY );
        return srcGray;
    }

    public Mat thresholdTest () {
        //threshold( src_gray, dst, threshold_value, MAX_8BIT_VALUE, threshold_type );
        threshold( srcGray, dst, 0, MAX_8BIT_VALUE, THRESH_OTSU );

        //imshow( window_name, dst );
        imwrite( "output.png", dst );
        return dst;
    }

    public static BufferedImage createAwtImage ( Mat mat ) {
        int type;
        switch ( mat.channels() ) {
            case 1:
                type = BufferedImage.TYPE_BYTE_GRAY;
                break;
            case 3:
                type = BufferedImage.TYPE_3BYTE_BGR;
                break;
            default:
                return null;
        }

        BufferedImage image = new BufferedImage( mat.width(), mat.height(), type );
        WritableRaster raster = image.getRaster();
        DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
        byte[] data = dataBuffer.getData();
        mat.get( 0, 0, data );

        return image;
    }
}
