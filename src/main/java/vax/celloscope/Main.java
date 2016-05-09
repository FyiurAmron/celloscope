package vax.celloscope;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**

 @author toor
 */
public class Main {
    final static private String WINDOW_NAME = "Threshold Demo";

    public static void main ( String[] args ) {
        nu.pattern.OpenCV.loadShared();

        JFrame jf = new JFrame( WINDOW_NAME );
        jf.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

        //System.out.println( "test" );
        Binarize b = new Binarize();
        //b.loadImage( "test.png" );
        b.loadImage( "E:\\Develop\\Java\\_projects\\celloscope\\src\\main\\resources\\test.png" );
        ImageIcon image = new ImageIcon( Binarize.createAwtImage( b.thresholdTest() ) );
        jf.add( new JLabel( image ) );

        jf.pack();
        jf.setVisible( true );
    }
}
