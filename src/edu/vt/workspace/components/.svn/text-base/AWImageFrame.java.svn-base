package edu.vt.workspace.components;

import edu.vt.workspace.data.AWDocument;
import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import java.io.File;
import javax.swing.JScrollPane;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.Scrollable;
import java.awt.Rectangle;
import java.awt.Component;
import javax.imageio.ImageIO;

/**
 * Describe class AWImageFrame here.
 *
 * @todo make this savable
 * Created: Thu Feb 19 15:23:11 2009
 *
 * @author <a href="mailto:cpa@gambit.cs.vt.edu">Christopher Andrews</a>
 * @version 1.0
 */
public class AWImageFrame extends AWDocumentView {

    static final long serialVersionUID = -3310754968414436552L;
    private ImagePane imagePane;
    private JScrollPane scrollArea;

    /**
     * Creates a new <code>AWImageFrame</code> instance.
     *
     * @param _doc
     * @param controller
     */
    public AWImageFrame(AWDocument doc, Queriable controller) {
        super(doc, controller);
        this._doc = doc;

        initialize();
    }


    private void initialize() {
        if (_doc == null) {
            scrollArea = new JScrollPane();
            scrollArea.setPreferredSize(new Dimension(200, 200));
        } else {
            imagePane = new ImagePane(_doc.getFile());
            scrollArea = new JScrollPane(imagePane);
            Dimension imageSize = imagePane.getPreferredSize();
            imageSize.width += 5;
            imageSize.height += 5;
            scrollArea.setPreferredSize(imageSize);
        }
        scrollArea.getViewport().setBackground(Color.gray);
        scrollArea.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollArea.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        add(scrollArea);
        pack();
    }

    /**
     *
     * @return
     */
    @Override
    public Rectangle getMaximizedBounds() {
        Rectangle bounds = getBounds();
        Dimension imageBounds = imagePane.getPreferredSize();
        Dimension scrollAreaBounds = imagePane.getParent().getSize();
        bounds.width = imageBounds.width + bounds.width - scrollAreaBounds.width;
        bounds.height = imageBounds.height + bounds.height - scrollAreaBounds.height;

        return bounds;
    }

//    @Override
//    public void writeExternal(ObjectOutput out) throws IOException {
//        super.writeExternal(out);
//    }
//
//    @Override
//    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
//        super.readExternal(in);
//        if (_doc != null){
//            imagePane = new ImagePane(_doc.getFile());
//            scrollArea.setViewportView(imagePane);
//            Dimension imageSize = imagePane.getPreferredSize();
//            imageSize.width += 5;
//            imageSize.height += 5;
//            scrollArea.setPreferredSize(imageSize);
//        }
//    }

    private class ImagePane extends JComponent implements Scrollable {
        static final long serialVersionUID = -1665752113972913068L;
        private File file;
        private transient BufferedImage image;

        public ImagePane(File file) {
            this.file = file;
            loadImage();
        }

        private void loadImage() {
            if (file == null) {
                return;
            }
            try {
                image = ImageIO.read(file);
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
                Logger.getLogger(AWImageFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }


        @Override
        public Dimension getPreferredSize() {
            if (image == null) {
                return new Dimension(0, 0);
            } else {
                return new Dimension(image.getWidth(), image.getHeight());
            }
        }

        @Override
        public void paintComponent(Graphics g) {
        
            if (image == null) {
                return;
            }
    
            // center the image if there is more room than image
            int x = (getWidth() - image.getWidth()) / 2;
            int y = (getHeight() - image.getHeight()) / 2;
            x = (x < 0) ? 0 : x;
            y = (y < 0) ? 0 : y;

            g.drawImage(image, x, y, null);

        }

        //@Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        //@Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 1;
        }

        //@Override
        public boolean getScrollableTracksViewportHeight() {
            // If the viewport is taller than the image, grow the ImagePane with the
            // viewport so we can center the image
            Component parent = (Component) getParent();
            return (parent.getHeight() > image.getHeight());
        }

        //@Override
        public boolean getScrollableTracksViewportWidth() {
            // If the viewport is wider than the image, grow the ImagePane with the
            // viewport so we can center the image
            Component parent = (Component) getParent();
            return (parent.getWidth() > image.getWidth());
        }

        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 1;
        }
    }
}
