package vax.celloscope;

import javax.swing.JFrame;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import static org.opencv.imgproc.Imgproc.*;
import static org.opencv.highgui.Highgui.*;

/**

 @author toor
 */
public class Binarize {
    final int max_BINARY_value = 255;

    Mat src, src_gray = new Mat(), dst = new Mat();
    String window_name = "Threshold Demo";

    void demo ( String srcName ) {
        /// Load an image
        src = imread( srcName, 1 );
        if ( src == null ) {
            return;
        }

        JFrame jf = new JFrame( window_name );
        jf.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
        jf.pack();
        jf.setVisible( true );

        /// Convert the image to Gray
        cvtColor( src, src_gray, COLOR_BGR2GRAY );

    }

    void thresholdTest () {
        if ( src == null ) {
            return;
        }
        //threshold( src_gray, dst, threshold_value, max_BINARY_value, threshold_type );
        threshold( src_gray, dst, 0, max_BINARY_value, THRESH_OTSU );

        //imshow( window_name, dst );
        imwrite( "output.png", dst );
    }
}
