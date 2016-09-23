package vax.opencv;

//import java.awt.image.*;
import vax.util.Vector2i;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import vax.celloscope.ImageCv;

/**

 @author toor
 */
public class OpenCvUtils {

    public static final Scalar //
            ZERO = new Scalar( 0 ),
            ONE = new Scalar( 1 );

    /**
     also sets both x and y to 0

     @param rect
     @param height
     @param width
     @return
     */
    public static Rect setSize ( Rect rect, int height, int width ) {
        rect.x = 0;
        rect.y = 0;
        rect.height = height;
        rect.width = width;
        return rect;
    }

    /**

     @param imageCv1
     @param imageCv2
     @param outR1
     @param outR2
     @param shiftMax
     @param outOffset
     @return distance (norm) measured after the correction (i.e. the lowest encountered)
     */
    public static double matchXY ( ImageCv imageCv1, ImageCv imageCv2, Rect outR1, Rect outR2, int shiftMax, Vector2i outOffset ) {
        Mat m = imageCv1.getSrc();
        int cols = m.cols(), rows = m.rows();
        setSize( outR1, rows, cols );
        setSize( outR2, rows, cols );
        ImageCv img1 = imageCv1.copy();
        ImageCv img2 = imageCv2.copy();
        img1.absdiff( img2.getSrc() );
        double distLast1 = img1.norm(), distLast2 = distLast1, distCur;
        img1.swap();

        int i;
        outR1.y = 1;

        for( i = 0; i < shiftMax; i++ ) {
            //outR1.y++;
            outR1.height--;
            outR2.height--;
            img1.rect( outR1 );
            img2.rect( outR2 );
            img1.absdiff( img2.getSrc() );
            distCur = img1.norm();
            img1.swap();
            if ( distCur >= distLast1 ) {
                break;
            }
            distLast1 = distCur;
        }
        outOffset.y = i;
        outR1.y = 0;
        outR2.y = 1;
        outR1.height = rows;
        outR2.height = rows;

        img1 = imageCv1.copy();
        img2 = imageCv2.copy();
        for( i = 0; i > -shiftMax; i-- ) {
            outR1.height--;
            outR2.height--;
            img1.rect( outR1 );
            img2.rect( outR2 );
            img1.absdiff( img2.getSrc() );
            distCur = img1.norm();
            img1.swap();
            if ( distCur >= distLast2 ) {
                break;
            }
            distLast2 = distCur;
        }
        if ( i != 0 && distLast2 < distLast1 ) {
            outOffset.y = i;
            distLast1 = distLast2;
        } else {
            distLast2 = distLast1;
        }
        outR1.height = rows;
        if ( outOffset.y != 0 ) {
            if ( outOffset.y > 0 ) {
                outR1.y = outOffset.y;
                outR2.y = 0;
                outR1.height -= outOffset.y;
            } else {
                outR2.y = -outOffset.y;
                outR1.height += outOffset.y;
            }
        } else {
            outR2.y = 0;
        }
        outR2.height = outR1.height;

        ImageCv img1y = imageCv1.copy();
        ImageCv img2y = imageCv2.copy();
        img1y.rect( outR1 );
        img2y.rect( outR2 );
        outR1.y = 0;
        outR2.y = 0;

        outR1.x = 1;
        img1 = img1y.copy();
        img2 = img2y.copy();
        for( i = 0; i < shiftMax; i++ ) {
            outR1.width--;
            outR2.width--;
            img1.rect( outR1 );
            img2.rect( outR2 );
            img1.absdiff( img2.getSrc() );
            distCur = img1.norm();
            img1.swap();
            if ( distCur >= distLast1 ) {
                break;
            }
            distLast1 = distCur;
        }
        outOffset.x = i;

        outR1.x = 0;
        outR2.x = 1;
        outR1.width = cols;
        outR2.width = cols;

        img1 = img1y.copy();
        img2 = img2y.copy();
        for( i = 0; i > -shiftMax; i-- ) {
            outR1.width--;
            outR2.width--;
            img1.rect( outR1 );
            img2.rect( outR2 );
            img1.absdiff( img2.getSrc() );
            distCur = img1.norm();
            img1.swap();
            if ( distCur >= distLast2 ) {
                break;
            }
            distLast2 = distCur;
        }
        if ( i != 0 && distLast2 < distLast1 ) {
            outOffset.x = i;
            distLast1 = distLast2;
        }
        /* else {
         distLast2 = distLast1;
         } */

        if ( outOffset.y > 0 ) {
            outR1.y = outOffset.y;
        } else {
            outR2.y = -outOffset.y;
        }

        outR1.width = cols;
        if ( outOffset.x != 0 ) {
            if ( outOffset.x > 0 ) {
                outR1.x = outOffset.x;
                outR2.x = 0;
                outR1.width -= outOffset.x;
            } else {
                outR2.x = -outOffset.x;
                outR1.width += outOffset.x;
            }
        } else {
            outR2.x = 0;
        }
        outR2.width = outR1.width;

        return distLast1;
    }

    /**

     @param imageCv1
     @param imageCv2
     @param outR1
     @param outR2
     @param shiftMax
     @param outOffset
     @return distance (norm) measured after the correction (i.e. the lowest encountered)
     */
    public static double _matchXY ( ImageCv imageCv1, ImageCv imageCv2, Rect outR1, Rect outR2, int shiftMax, Vector2i outOffset ) {
        Mat m = imageCv1.getSrc();
        int cols = m.cols(), rows = m.rows();
        setSize( outR1, rows, cols );
        setSize( outR2, rows, cols );
        ImageCv img1 = imageCv1.copy();
        ImageCv img2 = imageCv2.copy();
        img1.absdiff( img2.getSrc() );
        double distLast1 = img1.norm(), distLast2 = distLast1, distCur;

        int i;
        for( i = 0; i < shiftMax; i++ ) {
            outR1.y++;
            outR1.height--;
            outR2.height--;
            img1 = imageCv1.copy();
            img2 = imageCv2.copy();
            img1.rect( outR1 );
            img2.rect( outR2 );
            img1.absdiff( img2.getSrc() );
            distCur = img1.norm();
            if ( distCur >= distLast1 ) {
                break;
            }
            distLast1 = distCur;
        }
        outOffset.y = i;
        outR1.y = 0;
        outR1.height = rows;
        outR2.height = rows;
        for( i = 0; i > -shiftMax; i-- ) {
            outR2.y++;
            outR1.height--;
            outR2.height--;
            img1 = imageCv1.copy();
            img2 = imageCv2.copy();
            img1.rect( outR1 );
            img2.rect( outR2 );
            img1.absdiff( img2.getSrc() );
            distCur = img1.norm();
            if ( distCur >= distLast2 ) {
                break;
            }
            distLast2 = distCur;
        }
        if ( i != 0 && distLast2 < distLast1 ) {
            outOffset.y = i;
            distLast1 = distLast2;
        } else {
            distLast2 = distLast1;
        }
        outR1.height = rows;
        if ( outOffset.y != 0 ) {
            if ( outOffset.y > 0 ) {
                outR1.y = outOffset.y;
                outR2.y = 0;
                outR1.height -= outOffset.y;
            } else {
                outR2.y = -outOffset.y;
                outR1.height += outOffset.y;
            }
        } else {
            outR2.y = 0;
        }
        outR2.height = outR1.height;

        for( i = 0; i < shiftMax; i++ ) {
            outR1.x++;
            outR1.width--;
            outR2.width--;
            img1 = imageCv1.copy();
            img2 = imageCv2.copy();
            img1.rect( outR1 );
            img2.rect( outR2 );
            img1.absdiff( img2.getSrc() );
            distCur = img1.norm();
            if ( distCur >= distLast1 ) {
                break;
            }
            distLast1 = distCur;
        }
        outOffset.x = i;
        outR1.x = 0;
        outR1.width = cols;
        outR2.width = cols;
        for( i = 0; i > -shiftMax; i-- ) {
            outR2.x++;
            outR1.width--;
            outR2.width--;
            img1 = imageCv1.copy();
            img2 = imageCv2.copy();
            img1.rect( outR1 );
            img2.rect( outR2 );
            img1.absdiff( img2.getSrc() );
            distCur = img1.norm();
            if ( distCur >= distLast2 ) {
                break;
            }
            distLast2 = distCur;
        }
        if ( i != 0 && distLast2 < distLast1 ) {
            outOffset.x = i;
            distLast1 = distLast2;
        }
        /* else {
         distLast2 = distLast1;
         } */

        outR1.width = cols;
        if ( outOffset.x != 0 ) {
            if ( outOffset.x > 0 ) {
                outR1.x = outOffset.x;
                outR2.x = 0;
                outR1.width -= outOffset.x;
            } else {
                outR2.x = -outOffset.x;
                outR1.width += outOffset.x;
            }
        } else {
            outR2.x = 0;
        }
        outR2.width = outR1.width;

        return distLast1;
    }

    /*
     public static BufferedImage createBufferedImage ( Mat mat ) {
     int type;
     switch ( mat.channels() ) {
     case 1:
     type = BufferedImage.TYPE_BYTE_GRAY;
     break;
     case 3:
     type = BufferedImage.TYPE_3BYTE_BGR;
     break;
     case 4:
     type = BufferedImage.TYPE_4BYTE_ABGR;
     break;
     default:
     throw new UnsupportedOperationException();
     }

     BufferedImage image = new BufferedImage( mat.width(), mat.height(), type );
     WritableRaster raster = image.getRaster();
     DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
     byte[] data = dataBuffer.getData();
     mat.get( 0, 0, data );

     return image;
     }
     */
    private OpenCvUtils () {
        throw new UnsupportedOperationException();
    }
}
