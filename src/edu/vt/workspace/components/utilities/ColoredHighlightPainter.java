package edu.vt.workspace.components.utilities;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import javax.swing.plaf.TextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.LayeredHighlighter;
import javax.swing.text.Position;
import javax.swing.text.View;


    /**
     * This is a pure rip of the DefaultHighlightPainter that allows the _color to be changed. The default painter
     *  allows the _color to be set, but not changed (and any overrides would have had to reimplement anyway).
     */
    public class ColoredHighlightPainter extends LayeredHighlighter.LayerPainter{
        protected Color _color;
        protected boolean _visible;

        /**
         * Constructs a new highlight painter. If <code>c</code> is null,
         * the JTextComponent will be queried for its selection _color.
         *
         * @param c the _color for the highlight
         */
        public ColoredHighlightPainter(Color c) {
            _color = c;
            _visible = true;
        }

        /**
         * Returns the _color of the highlight.
         *
         * @return the _color
         */
        public Color getColor() {
            return _color;
        }

        public void setColor(Color color) {
            this._color = color;
        }

        public boolean isVisible() {
            return _visible;
        }

        public void setVisible(boolean visible) {
            this._visible = visible;
        }


        protected void highlightRect(Graphics g, int x, int y, int width, int height){
            g.fillRect(x,y,width, height);
        }

        // --- HighlightPainter methods ---------------------------------------
        /**
         * Paints a highlight.
         *
         * @param g the graphics context
         * @param offs0 the starting  offset >= 0
         * @param offs1 the ending model offset >= offs1
         * @param bounds the bounding box for the highlight
         * @param c the editor
         */
        public void paint(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c) {
            if (!isVisible()) {
                return;
            }
            Rectangle alloc = bounds.getBounds();
            try {
                // --- determine locations ---
                TextUI mapper = c.getUI();
                Rectangle p0 = mapper.modelToView(c, offs0);
                Rectangle p1 = mapper.modelToView(c, offs1);

                g.setColor(_color);

                if (p0.y == p1.y) {
                    // same line, render a rectangle
                    Rectangle r = p0.union(p1);
                    highlightRect(g,r.x, r.y, r.width, r.height);
                } else {
                    // different lines
                    int p0ToMarginWidth = alloc.x + alloc.width - p0.x;
                    highlightRect(g, p0.x, p0.y, p0ToMarginWidth, p0.height);
                    if ((p0.y + p0.height) != p1.y) {
                        highlightRect(g,alloc.x, p0.y + p0.height, alloc.width,
                                p1.y - (p0.y + p0.height));
                    }
                    highlightRect(g,alloc.x, p1.y, (p1.x - alloc.x), p1.height);
                }
            } catch (BadLocationException e) {
                // can't render
            }
        }

        // --- LayerPainter methods ----------------------------
        /**
         * Paints a portion of a highlight.
         *
         * @param g the graphics context
         * @param offs0 the starting model offset >= 0
         * @param offs1 the ending model offset >= offs1
         * @param bounds the bounding box of the view, which is not
         *        necessarily the region to paint.
         * @param c the editor
         * @param view View painting for
         * @return region drawing occured in
         */
        public Shape paintLayer(Graphics g, int offs0, int offs1,
                Shape bounds, JTextComponent c, View view) {
            if (!isVisible()) {
                return null;
            }

            g.setColor(_color);


            Rectangle r;

            if (offs0 == view.getStartOffset() &&
                    offs1 == view.getEndOffset()) {
                // Contained in view, can just use bounds.
                if (bounds instanceof Rectangle) {
                    r = (Rectangle) bounds;
                } else {
                    r = bounds.getBounds();
                }
            } else {
                // Should only render part of View.
                try {
                    // --- determine locations ---
                    Shape shape = view.modelToView(offs0, Position.Bias.Forward,
                            offs1, Position.Bias.Backward,
                            bounds);
                    r = (shape instanceof Rectangle) ? (Rectangle) shape : shape.getBounds();
                } catch (BadLocationException e) {
                    // can't render
                    r = null;
                }
            }

            if (r != null) {
                // If we are asked to highlight, we should draw something even
                // if the model-to-view projection is of zero width (6340106).
                r.width = Math.max(r.width, 1);

                highlightRect(g, r.x, r.y, r.width, r.height);
            }

            return r;
        }
    } // end of ColoredHighlightPainter