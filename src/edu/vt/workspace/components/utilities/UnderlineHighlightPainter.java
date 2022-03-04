package edu.vt.workspace.components.utilities;

import java.awt.Color;
import java.awt.Graphics;


/**
 * This is a simple highlighter that underlines rather than drawing blocks of color.
 * @author cpa
 */
public class UnderlineHighlightPainter extends ColoredHighlightPainter {
    private static final int THICKNESS = 2;

    public UnderlineHighlightPainter(Color c) {
        super(c);
    }

    @Override
    protected void highlightRect(Graphics g, int x, int y, int width, int height) {
        g.fillRect(x, y + height - THICKNESS, width, THICKNESS);
        }
} // end of UnderlineHighlightPainter
