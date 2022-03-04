package edu.vt.workspace.components;

import edu.vt.workspace.components.utilities.SimpleLine;
import edu.vt.workspace.components.utilities.SimpleLinkListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * This is a simple class for placing a label on links. Basically, this is just
 * white text on a black background that is placed appropriately.
 *
 * @author cpa
 */
public class LinkLabel extends JComponent{
    JLabel _label;
    JPanel _panel;

    public LinkLabel(){
        super();
        _label = new JLabel();
        
        _label.setForeground(Color.WHITE);
        _label.setFont(new Font("SanSerif", Font.PLAIN, 12));

        
        _panel = new JPanel();
        _panel.setLayout(new BorderLayout());
        _panel.add(_label, BorderLayout.CENTER);
        _panel.setBackground(Color.BLACK);
        _panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setLayout(new BorderLayout());
        add(_panel, BorderLayout.CENTER);
    }

    /**
     * This should be called when an edge should be labeled. This loads the
     * label text, places the label on the line and then hooks in to the link so it can
     * tell when the link is deselected (this is a temporary label that is only visible
     * when the link is selected).
     * @param text
     * @param link
     */
    public void showLabel(String text, final SimpleLink link){
        _label.setText("<html><p>"+text+"</p></html>");
        setSize(_panel.getPreferredSize());
        place(link.getLine());
        setVisible(true);

        link.addSimpleLinkListener(new SimpleLinkListener() {

            public void linkClosing(SimpleLink link) {
                setVisible(false);
                setLocation(0, -getHeight());
            }

            public void linkSelected(SimpleLink link) {

            }

            public void linkDeselected(SimpleLink link) {
                setVisible(false);
                setLocation(0, -getHeight());
                link.removeSimpleLinkListener(this);
            }

            public void linkChanging(SimpleLink link) {
                place(link.getLine());
            }
        });
        
    }



    /**
     * This places the label on the midpoint of the line.  We calculate this by
     * finding the midpoint of the line [(P0+P1) / 2 ]and then putting the middle of the
     * box [W/2, H/2] on top of it. The equation below just has the 2 factored out.
     * @param line
     */
    private void place(SimpleLine line){
        int xp, yp; // final point

        xp = (line.x0 + line.x1 - getWidth()) / 2;
        yp = (line.y0 + line.y1 - getHeight()) / 2;

        setLocation(xp, yp);
    }

}
