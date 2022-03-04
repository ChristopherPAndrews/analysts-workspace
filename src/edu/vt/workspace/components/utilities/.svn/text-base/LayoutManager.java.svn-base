package edu.vt.workspace.components.utilities;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout2;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.vt.workspace.components.AWInternalFrame;
import edu.vt.workspace.components.AWTextFrame;
import edu.vt.workspace.components.SimpleLink;
import edu.vt.workspace.components.WorkspacePane;
import edu.vt.workspace.data.AWDocument;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.Timer;

/**
 * This is a singleton class that handles specialized layout of components.
 * 
 * 
 * @author cpa
 */
public class LayoutManager {
     public static enum AlignmentEdge {

        NORTH, SOUTH, WEST, EAST, NORTH_SOUTH, EAST_WEST
    };

    public static enum DistibutionDirection {

        HORIZONTAL, VERTICAL
    };

    public static enum GraphLayoutType{
        CIRCULAR, FORCE_DIRECTED
    };

    private final AWDocumentComparator _comparator;
    private WorkspacePane _workspace;
    private static LayoutManager _instance = new LayoutManager();
    
    
    
    private LayoutManager() {
        _comparator = new AWDocumentComparator();
    }

    
    public static LayoutManager getInstance(){
        return _instance;
    }
    
    public void setWorkspace(WorkspacePane workspace){
        _workspace = workspace;
    }
    
    /**
     * This method is invoked to reorder the selected documents in temporal order
     */
    public void organizeSelectedTemporally() {
        WorkspacePaneDM dm = null;
        Rectangle bounds = null;
        try {
            dm = (WorkspacePaneDM) _workspace.getDesktopManager();
        } catch (ClassCastException cce) {
            System.out.println("Error - DesktopManager of the workspace is misinitialized");
        }

        if (dm != null) {
            Vector<MultiDraggableComponent> selectedObjects = dm.getSelectedObjects();
            if (selectedObjects.size() < 2) {
                return; // not enough components to rearragnge
            }
            ArrayList<AWTextFrame> frames = new ArrayList<AWTextFrame>(selectedObjects.size());
            ArrayList<FrameIndex> sortedFrames = new ArrayList<FrameIndex>(selectedObjects.size());

            for (MultiDraggableComponent component : selectedObjects) {
                if (component instanceof AWInternalFrame.MultiDragProxy
                        && ((AWInternalFrame.MultiDragProxy) component).getFrame() instanceof AWTextFrame) {
                    frames.add((AWTextFrame) ((AWInternalFrame.MultiDragProxy) component).getFrame());
                }
            }


            for (int i = 0; i < frames.size(); i++) {
                JComponent component = frames.get(i);
                sortedFrames.add(new FrameIndex(i, frames));
                if (bounds == null) {
                    bounds = component.getBounds();
                } else {
                    bounds.add(component.getBounds());
                }
            }

            _comparator.setSortType(AWDocumentComparator.SortType.DATE);
            Collections.sort(sortedFrames);
            Point p = bounds.getLocation();

            int largest = 0; // used to track the largest width/height, so when we reset for new column/row we are the right distance over/down
            for (FrameIndex frameIndex : sortedFrames) {
                AWTextFrame frame = frames.get(frameIndex.getIndex());

                frame.setLocation(p);

                if (bounds.height > bounds.width) { // roughly a column, so make columns
                    p.y += frame.getHeight();
                    largest = frame.getWidth() > largest ? frame.getWidth() : largest;
                    if (p.y > bounds.height + bounds.y) {
                        p.y = bounds.y;
                        p.x += largest;
                        largest = 0;
                    }
                } else { // roughly a row, so make rows
                    p.x += frame.getWidth();
                    largest = frame.getHeight() > largest ? frame.getHeight() : largest;
                    if (p.x > bounds.width + bounds.x) {
                        p.x = bounds.x;
                        p.y += largest;
                        largest = 0;
                    }
                }
            }
        }
    }

    /**
     * This method is invoked to layout the selected objects based on a graph layout algorithm
     *
     * @param layoutType the {@code GraphLayoutType} to be used to layout the selected objects
     */
    public void layoutGraph(GraphLayoutType layoutType) {
        WorkspacePaneDM dm = null;
        Rectangle bounds = null;
        Point2D position;
        try {
            dm = (WorkspacePaneDM) _workspace.getDesktopManager();
        } catch (ClassCastException cce) {
            System.out.println("Error - DesktopManager of the workspace is misinitialized");
        }

        if (dm == null){
            // bail out - nothing we can do
            return;
        }

        Vector<MultiDraggableComponent> selectedObjects = dm.getSelectedObjects();
        if (selectedObjects.size() < 2) {
            return; // not enough components to rearragnge
        }

        // find the bounds of the whole collection
        for (MultiDraggableComponent component : selectedObjects) {
            if (bounds == null) {
                bounds = component.getBounds();
            } else {
                bounds.add(component.getBounds());
            }
        }
        
        // Build a graph of the selected objects

        
        // add all verticies to the graph
       Graph<MultiDraggableComponent, SimpleLink> graph = new SparseMultigraph<MultiDraggableComponent, SimpleLink>();
        for (MultiDraggableComponent component : selectedObjects) {
            graph.addVertex(component);
        }

        
        // add all of the links to the graph
        //@todo fix this to use the new links, or some other solution
        Vector<SimpleLink> links = null;//_workspace.getCanvas().getLinks();

        for (SimpleLink link: links){
            if (link.isVisible()){
                JInternalFrame[] frames = link.getFrames();
                if (frames[0] instanceof AWInternalFrame 
                        && frames[1] instanceof AWInternalFrame
                        && selectedObjects.contains(((AWInternalFrame)frames[0]).getDragProxy())
                        && selectedObjects.contains(((AWInternalFrame)frames[1]).getDragProxy())){
                    graph.addEdge(link, ((AWInternalFrame) frames[0]).getDragProxy(), ((AWInternalFrame) frames[1]).getDragProxy());
                }
            }
        }

        
        // create the layout
        Layout<MultiDraggableComponent, SimpleLink> layout;

        if (layoutType == GraphLayoutType.FORCE_DIRECTED){
            layout = new FRLayout2(graph);
        } else{
            layout = new CircleLayout(graph);
        }

        layout.setSize(bounds.getSize());
        
        
       
        // set the initial positions of the items in the graph
        position = new Point2D.Double();
        for (MultiDraggableComponent component : selectedObjects) {
            position.setLocation(component.getX() - bounds.x, component.getY() - bounds.y);
            layout.setLocation(component, position);
        }

        layout.initialize();

        
        Point p = new Point();
        if (layoutType == GraphLayoutType.FORCE_DIRECTED ) {
           GraphLayoutAnimator animator = new GraphLayoutAnimator(layout, bounds.getLocation());
           animator.start();

        } else {

            for (MultiDraggableComponent component : selectedObjects) {
                position = layout.transform(component);
                p.x = (int) (Math.round(position.getX()) + bounds.x);
                p.y = (int) (Math.round(position.getY()) + bounds.y);
                component.setLocation(p);
            }
        }
    }



    /**
     * This method is called to align all of the currently selected sortedFrames along one of the four edges.
     * 
     * @param edge the edge we are aligning to
     */
    public void alignSelected(AlignmentEdge edge) {
        WorkspacePaneDM dm = null;
        Rectangle bounds = null;

        try {
            dm = (WorkspacePaneDM) _workspace.getDesktopManager();
        } catch (ClassCastException cce) {
            System.out.println("Error - DesktopManager of the workspace is misinitialized");
        }

        if (dm != null) {
            Vector<MultiDraggableComponent> selectedObjects = dm.getSelectedObjects();
            for (MultiDraggableComponent component : selectedObjects) {
                if (bounds == null) {
                    bounds = component.getBounds();
                } else {
                    bounds.add(component.getBounds());
                }
            }

            Point p = new Point(0, 0);
            int centerLine;
            for (MultiDraggableComponent component : selectedObjects) {
                p = component.getLocation(p);
                switch (edge) {
                    case NORTH:
                        p.y = bounds.y;
                        break;
                    case SOUTH:
                        p.y = bounds.y + bounds.height - component.getHeight();
                        break;
                    case WEST:
                        p.x = bounds.x;
                        break;
                    case EAST:
                        p.x = bounds.x + bounds.width - component.getWidth();
                        break;
                    case NORTH_SOUTH:
                        centerLine = bounds.x + bounds.width / 2;
                        p.x = centerLine - component.getWidth() / 2;
                        break;
                    case EAST_WEST:
                        centerLine = bounds.y + bounds.height / 2;
                        p.y = centerLine - component.getHeight() / 2;
                        break;
                }

                component.setLocation(p);
            }
        }
    }

    public void distributeSelected(DistibutionDirection direction) {
        WorkspacePaneDM dm = null;
        Rectangle bounds = null;
        try {
            dm = (WorkspacePaneDM) _workspace.getDesktopManager();
        } catch (ClassCastException cce) {
            System.out.println("Error - DesktopManager of the workspace is misinitialized");
        }

        if (dm != null) {
            int consumedSpace = 0;
            Vector<MultiDraggableComponent> selectedComponents = dm.getSelectedObjects();
            Vector<MultiDraggableComponent> orderedComponents = new Vector<MultiDraggableComponent>(selectedComponents.size());
            for (MultiDraggableComponent component : selectedComponents) {
                //if (component instanceof AWTextFrame) {
                if (bounds == null) {
                    bounds = component.getBounds();
                } else {
                    bounds.add(component.getBounds());
                }
                if (direction == DistibutionDirection.VERTICAL) {
                    consumedSpace += component.getHeight();
                } else {
                    consumedSpace += component.getWidth();
                }

                int i;
                boolean inserted = false;
                for (i = 0; i < orderedComponents.size(); i++) {

                    if ((direction == DistibutionDirection.VERTICAL && orderedComponents.get(i).getY() > component.getY())
                            || (direction == DistibutionDirection.HORIZONTAL && orderedComponents.get(i).getX() > component.getX())) {
                        orderedComponents.add(i, component);
                        inserted = true;
                        break;
                    }
                }
                if (!inserted) {
                    orderedComponents.add(i, component);
                }
                //}
            }


            int spacer;
            if (direction == DistibutionDirection.VERTICAL) {
                spacer = (int) Math.floor((bounds.height - consumedSpace) / (selectedComponents.size() - 1));
            } else {
                spacer = (int) Math.floor((bounds.width - consumedSpace) / (selectedComponents.size() - 1));
            }

            if (spacer < 0) {
                // there isn't enough room for everything to not overlap - we need to add some additional space

                spacer = 0;
                // can we actually fit it in?
                if (direction == DistibutionDirection.VERTICAL) {
                    int additionalSpace = consumedSpace - bounds.height;
                    int newHeight = additionalSpace + bounds.height;
                    int freespace = _workspace.getBounds().height - (bounds.y + newHeight);
                    if (freespace < 0) {
                        // can't just use more space down - need to move the column up a bit
                        if (newHeight > _workspace.getBounds().height) { // going to have to overlap, no matter what
                            bounds.y = 0;
                            spacer = -(newHeight - _workspace.getBounds().height) / orderedComponents.size();
                        } else {
                            bounds.y = bounds.y + freespace;
                        }
                    }
                } else {
                    int additionalSpace = consumedSpace - bounds.width;
                    int newWidth = additionalSpace + bounds.width;
                    int freespace = _workspace.getBounds().width - (bounds.y + newWidth);
                    if (freespace < 0) {
                        // can't just use more space to the right - need to move the column over a bit
                        if (newWidth > _workspace.getBounds().width) { // going to have to overlap, no matter what
                            bounds.x = 0;
                            spacer = -(newWidth - _workspace.getBounds().width) / orderedComponents.size();
                        } else {
                            bounds.x = bounds.x + freespace;
                        }
                    }
                }
            }

            Point p = new Point(0, 0);
            int offset = (direction == DistibutionDirection.VERTICAL) ? bounds.y : bounds.x;
            for (MultiDraggableComponent component : orderedComponents) {
                p = component.getLocation(p);
                if (direction == DistibutionDirection.VERTICAL) {
                    p.y = offset;
                    offset += component.getHeight() + spacer;
                } else {
                    p.x = offset;
                    offset += component.getWidth() + spacer;
                }
                component.setLocation(p);
            }
        }
    }

    /**
     * Clear the current multiselection out.
     */
    public void clearSelected() {
        WorkspacePaneDM dm = null;
        try {
            dm = (WorkspacePaneDM) _workspace.getDesktopManager();
        } catch (ClassCastException cce) {
            System.out.println("Error - DesktopManager of the workspace is misinitialized");
        }
        dm.clearMultiSelect();
    }

    /**
     * Add a frame to the current multi selection.
     * @param frame
     */
    public void addFrameToSelection(JInternalFrame frame) {
        WorkspacePaneDM dm = null;
        try {
            dm = (WorkspacePaneDM) _workspace.getDesktopManager();
        } catch (ClassCastException cce) {
            System.out.println("Error - DesktopManager of the workspace is misinitialized");
        }
        if (frame instanceof AWInternalFrame){
            dm.toggleMultiSelect(((AWInternalFrame) frame).getDragProxy());
        }
    }
    
    
    /**
     * This class allows us to sort text frames based on their contained documents
     */
    private class FrameIndex implements Comparable {

        private int _index;
        private ArrayList<AWTextFrame> _frames;

        public FrameIndex(int index, ArrayList<AWTextFrame> frames) {
            _index = index;
            _frames = frames;
        }

        public int getIndex() {
            return _index;
        }

        public void setIndex(int index) {
            _index = index;
        }

        public int compareTo(Object t) {
            AWDocument doc1 = _frames.get(_index).getDocument();
            AWDocument doc2 = _frames.get(((FrameIndex) t).getIndex()).getDocument();

            int comparison = _comparator.compare(doc1, doc2);

            return comparison;

        }
    }
    
    
    
    
    
    
    /**
     * This is a private class that handles the animation of objects for graph layout algorithms
     * that can be animated. In truth, the use of animation is of questionable value here, but it may help the
     * user a little to be able to follow particular objects around. On the plus side, the algorithms move quickly
     * and it does look nice.
     */
    private class GraphLayoutAnimator implements ActionListener{
        Layout<MultiDraggableComponent, SimpleLink> _layout;
        Timer _timer;
        Point _origin;
        
        public GraphLayoutAnimator(Layout<MultiDraggableComponent, SimpleLink> layout, Point origin){
            _layout = layout;
            _origin = origin;
            if (_layout instanceof IterativeContext){
                _timer = new Timer(25, this);
            }            
        }

        public void start(){
            if (_timer != null){
                _timer.start();
            }
        }

        public void actionPerformed(ActionEvent e) {
            Point2D position;
            Point p = new Point();
            if (((IterativeContext) _layout).done()){
                _timer.stop();
                return;
            }


            for (MultiDraggableComponent component : _layout.getGraph().getVertices()) {
                position = _layout.transform(component);
                p.x = (int) (Math.round(position.getX()) + _origin.x);
                p.y = (int) (Math.round(position.getY()) + _origin.y);
                component.setLocation(p);

            }
            ((IterativeContext) _layout).step();
        }
    
    } 

}
