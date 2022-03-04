
package edu.vt.workspace.components.utilities;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

/**
 *
 * @author cpa
 */
public class SimpleLine {

        public int x0,  x1,  y0,  y1;
        private AffineTransform transform = new AffineTransform();

        public SimpleLine(int x1, int y1, int x2, int y2) {
            setLine(x1, y1, x2, y2);
        }

        public SimpleLine(Rectangle r0, Rectangle r1, boolean directed) {
            float ratio;
            transform.setToIdentity();
            setLine(r0.x + r0.width / 2,
                    r0.y + r0.height / 2,
                    r1.x + r1.width / 2,
                    r1.y + r1.height / 2);
            if ((Math.abs(x0 - x1) < (r0.width + r1.width) / 2) &&
                    (Math.abs(y0 - y1) < (r0.height + r1.height) / 2)) {
                // the frames are overlapping - don't bother
                x0 = x1 = y0 = y1 = 0;
                return;
            }

            if (y0 == y1) {
                // a straight horizontal line, we can't play with triangles
                if (x0 < x1) {
                    x0 = r0.x + r0.width;
                    x1 = r1.x;
                } else {
                    x0 = r0.x;
                    x1 = r1.x + r1.width;
                }
                calcTransform();
                return;
            }
            if (x0 == x1) {
                // a straight vertical line, we can't play with triangles
                if (y0 < y1) {
                    y0 = r0.y + r0.height;
                    y1 = r1.y;
                } else {
                    y0 = r0.y;
                    y1 = r1.y + r1.height;
                }
                calcTransform();
                return;
            }


            ratio = Math.abs(x0 - x1) / (float) Math.abs(y0 - y1);
            if ((Math.abs(x0 - x1) < (r0.width + r1.width) / 2) &&
                    (Math.abs(y0 - y1) < (r0.height + r1.height) / 2)) {
                // the frames are overlapping - don't bother
                x0 = x1 = y0 = y1 = 0;
                return;
            }


            if (x0 < x1) {
                x0 += Math.round((r0.height / 2) * ratio);
                x0 = (x0 > (r0.x + r0.width)) ? (r0.x + r0.width) : x0;
                x1 -= Math.round((r1.height / 2) * ratio);
                x1 = (x1 < r1.x) ? r1.x : x1;
            } else if (x1 < x0) {
                x1 += Math.round((r1.height / 2) * ratio);
                x1 = (x1 > (r1.x + r1.width)) ? (r1.x + r1.width) : x1;
                x0 -= Math.round((r0.height / 2) * ratio);
                x0 = (x0 < r0.x) ? r0.x : x0;
            }

            if (y0 < y1) {
                y0 += Math.round((r0.width / 2) / ratio);
                y0 = (y0 > (r0.y + r0.height)) ? (r0.y + r0.height) : y0;
                y1 -= Math.round((r1.width / 2) / ratio);
                y1 = (y1 < r1.y) ? r1.y : y1;
            } else if (y1 < y0) {
                y1 += Math.round((r1.width / 2) / ratio);
                y1 = (y1 > (r1.y + r1.height)) ? (r1.y + r1.height) : y1;
                y0 -= Math.round((r0.width / 2) / ratio);
                y0 = (y0 < r0.y) ? r0.y : y0;
            }

            calcTransform();
        }

        private void calcTransform() {
            int adj = (x1 - x0);
            int opp = (y1 - y0);

            double hyp = Math.sqrt(adj * adj + opp * opp);
            if (hyp != 0) {
                double c = adj / hyp;
                double s = opp / hyp;
                transform.setTransform(c, s, -s, c, x1, y1);
            // transform = new AffineTransform(-c, -s, -s, c, s * ARROW_WIDTH / 2 + x1, -c * ARROW_HEIGHT / 2 + y1);
            }
        }

        public AffineTransform getTransform() {
            return transform;
        }

        public void setLine(int x1, int y1, int x2, int y2) {
            this.x0 = x1;
            this.y0 = y1;
            this.x1 = x2;
            this.y1 = y2;
        }


        public boolean containsPoint(Point p){
            return containsPoint(p, 0);
        }


        /**
         * This method finds the distance from the point to the line segment and returns true if the
         * point is within the slop range of the line, and false if it is not.
         *
         * @param p the Point we are checking
         * @param slop the amount of flexibility we are allowing around the line
         * @return
         */
        public boolean containsPoint(Point p, int slop){
            final int deltaX = x1 - x0;
            final int deltaY = y1 - y0;
           
            // the end points are the same point - bail out
            if (deltaX == 0 && deltaY == 0) return false;

            final float u = ((p.x - x0)* deltaX + (p.y - y0)*deltaY) / (float)((deltaX * deltaX) + (deltaY * deltaY));
           
            // if u is not between 0 and 1, then the intersection is off the line
            if (u < 0 || u > 1){
                return false;
            }

            // calculate new deltas based on the test point and the intersection point
            final float newDeltaX = p.x - (x0 + u*(x1 - x0));
            final float newDeltaY = p.y - (y0 + u*(y1 - y0));

            final float distanceSq = newDeltaX * newDeltaX + newDeltaY * newDeltaY;
           
            return distanceSq < (slop * slop);
        }

        /**
         * Describe <code>toString</code> method here.
         *
         * @return a <code>String</code> value
         */
        @Override
        public final String toString() {
            return new String("SimpleLine[" + x0 + ", " + y0 + ", " + x1 + ", " + y1 + "]");
        }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SimpleLine) {
            SimpleLine l = (SimpleLine) obj;
            return ((x0 == l.x0) && (x1 == l.x1) && (y0 == l.y0) && (y1 == l.y1));
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + this.x0;
        hash = 37 * hash + this.x1;
        hash = 37 * hash + this.y0;
        hash = 37 * hash + this.y1;
        return hash;
    }
} // end of SimpleLine
