package edu.vt.workspace.components.utilities;

import java.awt.Color;
import java.awt.Graphics;


/**
 * This is the user highlight painter. This creates yellow highlights that are selectable.
 * 
 * @author cpa
 */
public class UserHighlightPainter extends ColoredHighlightPainter {
    private boolean _selected = false;

    public UserHighlightPainter() {
        super(Color.YELLOW);
    }

    public void setSelected(boolean selected) {
        _selected = selected;
    }

    @Override
    protected void highlightRect(Graphics g, int x, int y, int width, int height) {
        g.fillRect(x, y, width, height);
        if (_selected) {
            g.setColor(Color.RED);
            g.drawRect(x - 2, y - 2, width + 4, height + 4);
            g.setColor(_color);
        }
    }
} // end of UnderlineHighlightPainter
