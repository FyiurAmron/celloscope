package vax.celloscope;

/**

 @author toor
 */
public class Main {
    public static void main ( String[] args ) {
        nu.pattern.OpenCV.loadShared();
        //System.out.println( "test" );
        Binarize b = new Binarize();
        //b.demo( "test.png" );
        b.demo( "E:\\Develop\\Java\\_projects\\celloscope\\src\\main\\resources\\test.png" );
        b.thresholdTest();
    }
}
