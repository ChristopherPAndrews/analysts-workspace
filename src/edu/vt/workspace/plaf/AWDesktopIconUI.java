package edu.vt.workspace.plaf;

import edu.vt.workspace.components.AWEntityView;
import edu.vt.workspace.components.AWInternalFrame;
import edu.vt.workspace.components.AWTextFrame;
import edu.vt.workspace.components.utilities.ColoredIcon;
import edu.vt.workspace.components.utilities.EntityFreqComparator;
import edu.vt.workspace.components.utilities.EntityMenuHelper;
import edu.vt.workspace.components.utilities.LayoutMenuHelper;
import edu.vt.workspace.components.utilities.SortableListEntity;
import edu.vt.workspace.components.utilities.WorkspacePaneDM;
import edu.vt.workspace.data.AWDocument;
import edu.vt.workspace.data.AWEntity;
import edu.vt.workspace.data.EntityManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.Collections;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.DesktopManager;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicDesktopIconUI;

/**
 * This is the AW specific UI for DesktopIcons. This has two main roles.
 * The first is to change the behavior of desktopIcons so that they remain
 * tied to the host position and also to allow multi-selection. The second purpose
 * is to adjust the look as needed. Currently, the only components that I allow to 
 * iconify are entities, so there is special code for setting up and breaking down
 * entity specific icons.
 * @author cpa
 */
public class AWDesktopIconUI extends BasicDesktopIconUI  {
    // Universal L&F fields
    private static final Color SELECTED_COLOR = new Color(151,189,230);
    private static final Color PANEL_NORMAL_COLOR = Color.white;
    private static final Color TEXT_COLOR = new Color(200, 200, 200);

    // multi-drag fields
    private boolean _inMultiSelect = false;
    private boolean _multiDragging = false;
    private boolean _highlighted = false;
    // Entity Icon specific fields
    private JLabel _label;
    private JPanel _panel;
    private ComponentListener _listener;

    public static ComponentUI createUI(JComponent c) {
        return new AWDesktopIconUI();
    }

    public AWDesktopIconUI() {

        _listener = new ComponentAdapter(){
            @Override
            public void componentMoved(ComponentEvent e) {
                if (frame != null && frame.isIcon()){
                    frame.setLocation(AWDesktopIconUI.this.desktopIcon.getLocation());
                }
            }

            
        };
    }

    /**
     * This code sets up the actual look of the desktop icon. At the moment,
     * it calls special code if it is an {@code AWEntityView}, otherwise it
     * uses the default {@code Metal} look.
     */
    @Override
    protected void installComponents() {
        frame = desktopIcon.getInternalFrame();
        if (frame instanceof AWEntityView) {
            installEntityViewComponents((AWEntityView) frame);
        } else if (frame instanceof AWTextFrame) {
             installTextFrameComponents((AWTextFrame) frame);
        }else {
            super.installComponents();
        }
        desktopIcon.addComponentListener(_listener);
    }

    /**
     * This tears down the desktop icon representation. At the moment, it calls
     * special code to handle {@code AWEntityView} objects, otherwise it performs
     * the default functionality.
     */
    @Override
    protected void uninstallComponents() {
        frame = desktopIcon.getInternalFrame();
        if (frame instanceof AWEntityView) {
            removeEntityViewComponents();
        } else if (frame instanceof AWTextFrame){
            removeTextFrameComponents();
        } else{
            super.uninstallComponents();
        }
        desktopIcon.removeComponentListener(_listener);
    }

    /***************** EntityView specific code *****************/
    /**
     * Create the actualy iconified form of the EntityView. In this case,
     * this is simply a label with an icon that is fetched from the EntityManager.
     * If none can be found, this just uses a plain colored square as an icon.
     *
     * @param view the {@code AWEntityView} that is associated with the icon
     */
    private void installEntityViewComponents(AWEntityView view) {
        String title = view.getTitle();
        Color color = Color.WHITE;
        Icon icon;
        URL url = null;
        if (view.getEntity() != null) {
            AWEntity entity = view.getEntity();
            color = EntityManager.getInstance().getColor(entity);
            url = EntityManager.getInstance().getIconURL(entity);
        }

        if (url != null) {
            icon = new ImageIcon(url);
        } else {
            icon = new ColoredIcon(color, 20);
        }
        _label = new JLabel(title, icon, JLabel.CENTER);
        _label.setVerticalTextPosition(JLabel.BOTTOM);
        _label.setHorizontalTextPosition(JLabel.CENTER);
        _label.setForeground(TEXT_COLOR);


        _panel = new JPanel();
        _panel.setBackground(SELECTED_COLOR);

        _panel.setLayout(new BorderLayout());
        _panel.add(_label, BorderLayout.CENTER);
        _panel.setOpaque(false);


        desktopIcon.setBackground(SELECTED_COLOR);
        desktopIcon.setOpaque(false);
        desktopIcon.setBorder(null);
        desktopIcon.setLayout(new BorderLayout());
        desktopIcon.add(_panel, BorderLayout.CENTER);
    }

    /**
     * This breaks down the iconified form of the desktop icon. It removes
     * the label from the desktopIcon and throws it away.
     * @param view the {@code AWEntityView} that is associated with the icon
     */
    private void removeEntityViewComponents() {
        desktopIcon.remove(_panel);
        _panel = null;
        _label = null;
    }


    /***************** InternalTextFrame specific code *****************/

    private void installTextFrameComponents(AWTextFrame textFrame) {
       StringBuffer buffer = new StringBuffer();
       buffer.append("<html>");
       buffer.append("<b>"+textFrame.getTitle()+"</b><br /><hr /> <br />");
       AWDocument doc = textFrame.getDocument();
       if (doc != null){
           Vector<SortableListEntity> data = new Vector<SortableListEntity>( doc.getEntities().size());
           for (AWEntity entity: doc.getEntities()){
               data.add(new SortableListEntity(entity));
           }

           Collections.sort(data, new EntityFreqComparator());


        for (SortableListEntity sle: data){
            AWEntity entity = sle.getEntity();
            int c = EntityManager.getInstance().getColor(entity).getRGB();
            c = c & 0xffffff; // trim off the alpha channel
            buffer.append("<font color=#" + Integer.toHexString(c) + ">"+ entity.getValue() + "</font> <br />");
        }   
       }
       
       buffer.append("</html>");



        _label = new JLabel(buffer.toString());
        _label.setFont(new Font("SanSerif", Font.PLAIN, 12));



        _panel = new JPanel();
        _panel.setBackground(SELECTED_COLOR);

        _panel.setLayout(new BorderLayout());
        _panel.add(_label, BorderLayout.CENTER);
        _panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        _panel.setOpaque(false);

        desktopIcon.setBackground(PANEL_NORMAL_COLOR);
        desktopIcon.setBorder(null);
        desktopIcon.setLayout(new BorderLayout());
        desktopIcon.add(_panel, BorderLayout.CENTER);

    }


    private void removeTextFrameComponents() {
        desktopIcon.remove(_panel);
        _panel = null;
        _label = null;
    }




    /***************** Multi-select specific code *****************/
    /**
     * This creates the mouse handler used by the desktopicon. We
     * override the original version to provide our own handler.
     * @return
     */
    @Override
    protected MouseInputListener createMouseInputListener() {
        return new MouseInputHandler2();
    }

//    public void setMultiSelectMember(boolean select) {
//        _inMultiSelect = select;
//    }

    public void hilight(boolean on) {
        _highlighted = on;
        if (_panel != null){
            _panel.setOpaque(on);
        }
    }

//    public boolean isMultiSelectMember() {
//        return _inMultiSelect;
//    }

    public class MouseInputHandler2 extends MouseInputHandler {
        Point ap = new Point(); // absolute location (within app, not display)

        // this is to get around a bug (mine or Swing, not sure) where a non-drag event
        // gets passed off as a drag to Swing's desktop manager rather than mine and the
        // release event causes a stack dump due to a class type issue.
        private boolean _normalUse = false;



        @Override
        public void mouseClicked(MouseEvent me) {
            if (me.isControlDown()) {
                // add the current object to the list of selected objects or subtract it
                // if it is already present
                DesktopManager dm = desktopIcon.getDesktopPane().getDesktopManager();
                if (dm instanceof WorkspacePaneDM) {
                    ((WorkspacePaneDM) dm).toggleMultiSelect(((AWInternalFrame)frame).getDragProxy());
                }
            } else {
                super.mouseClicked(me);
            }
        }

        @Override
        public void mousePressed(MouseEvent me) {
            if (me.isPopupTrigger()) {
                if (_highlighted){
                    JPopupMenu menu = LayoutMenuHelper.getInstance().getMenu();
//                    menu.insert(new JMenuItem("test"), 0);
//                    menu.insert(new JPopupMenu.Separator(), 1);
                    menu.show(desktopIcon,
                            me.getX(),
                            me.getY());
                } else if (desktopIcon.getInternalFrame() instanceof AWEntityView) {
                    AWEntityView view = (AWEntityView)desktopIcon.getInternalFrame();
                    JPopupMenu menu = EntityMenuHelper.getInstance().loadMenu(view.getEntity(),(AWInternalFrame) frame);
                     menu.show(desktopIcon,
                            me.getX(),
                            me.getY());
                }
            } else {
                // if this is one of the selected objects, send event to DM
                // DM should respond if it wants to handle this (more than one view)
                // or if it should go to default
                DesktopManager dm = desktopIcon.getDesktopPane().getDesktopManager();
                _multiDragging = false;
                if (me.isControlDown() || ((AWInternalFrame)frame).getDragProxy().isMultiSelectMember()) {
                    if (me.isControlDown()
                            || !((dm instanceof WorkspacePaneDM) && ((WorkspacePaneDM) dm).isMultipleDrag())) {
                        _normalUse = true;
                        super.mousePressed(me);
                        return;
                    }

                    // do multi drag stuff stuff
                    ap = SwingUtilities.convertPoint((Component) me.getSource(),
                            me.getX(), me.getY(), null);

                    if (dm instanceof WorkspacePaneDM) {
                        ((WorkspacePaneDM) dm).beginDraggingFrames(((AWInternalFrame)frame).getDragProxy());
                    }
                    _multiDragging = true;


                } else {
                    // this isn't in a multi-selection and it isn't being added
                    // reset the selected frames to a single item
                    if (dm instanceof WorkspacePaneDM) {
                        ((WorkspacePaneDM) dm).clearMultiSelect(((AWInternalFrame)frame).getDragProxy());
                    }
                    _normalUse = true;
                    super.mousePressed(me);
                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent me) {
            if (_normalUse){
                super.mouseReleased(me);
               
            } 
            desktopIcon.getParent().repaint();
        }

    @Override
    public void mouseDragged(MouseEvent me) {
        if (_multiDragging) {
            DesktopManager dm = desktopIcon.getDesktopPane().getDesktopManager();
            Point p = SwingUtilities.convertPoint((Component) me.getSource(),
                    me.getX(), me.getY(), null);
            int deltaX = p.x - ap.x;
            int deltaY = p.y - ap.y;
            ap.setLocation(p);
            if (dm instanceof WorkspacePaneDM) {
                ((WorkspacePaneDM) dm).dragFrames(((AWInternalFrame)frame).getDragProxy(), deltaX, deltaY);
            }
        }
        super.mouseDragged(me);
    }
}

}
