package vax.gui;

import java.awt.*;
import javax.swing.JPanel;

/**

 @author toor
 */
public class ImagePanel extends JPanel {
    private Image image;
    private final Dimension dim = new Dimension();

    public ImagePanel () {
        super();
    }

    public ImagePanel ( Image image ) {
        super();
        _setImage( image );
    }

    public ImagePanel ( LayoutManager layout ) {
        super( layout );
    }

    public ImagePanel ( Image image, LayoutManager layout ) {
        super( layout );
        _setImage( image );
    }

    @Override
    protected void paintComponent ( Graphics g ) {
        super.paintComponent( g );
        if ( image == null ) {
            return;
        }

        g.drawImage( image, 0, 0, null );
    }

    private void _setImage ( Image image ) {
        this.image = image;
        if ( image != null ) {
            dim.height = image.getHeight( null );
            dim.width = image.getWidth( null );
            setSize( dim );
            setMinimumSize( dim );
            setPreferredSize( dim );
            setMaximumSize( dim );
        }
        repaint();
    }

    public void setImage ( Image image ) {
        _setImage( image );
    }
}
