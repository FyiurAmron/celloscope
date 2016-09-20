package vax.opencv;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import org.opencv.core.Mat;

/**

 @author toor
 */
public class MatBufferedImage extends BufferedImage {
    public MatBufferedImage ( Mat mat ) {
        super( mat.width(), mat.height(), getBufferedImageType( mat ) );
        updateBufferedImage( this, mat );
    }

    public BufferedImage updateBufferedImage ( Mat mat ) {
        updateBufferedImage( this, mat );
        return this;
    }

    public static int getBufferedImageType ( Mat mat ) {
        switch ( mat.channels() ) {
            case 1:
                return BufferedImage.TYPE_BYTE_GRAY;
            case 3:
                return BufferedImage.TYPE_3BYTE_BGR;
            case 4:
                return BufferedImage.TYPE_4BYTE_ABGR;
        }
        throw new UnsupportedOperationException();
    }

    public static BufferedImage updateBufferedImage ( BufferedImage bufferedImage, Mat mat ) {
        mat.get( 0, 0, ( (DataBufferByte) bufferedImage.getRaster().getDataBuffer() ).getData() );
        return bufferedImage;
    }

    public static BufferedImage createBufferedImage ( Mat mat ) {
        BufferedImage bi = new BufferedImage( mat.width(), mat.height(), getBufferedImageType( mat ) );
        updateBufferedImage( bi, mat );
        return bi;
    }
}
