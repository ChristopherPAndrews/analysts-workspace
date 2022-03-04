package edu.vt.workspace.components;

import edu.vt.workspace.data.AWLinkManager;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

/**
 * The {@code WorkspaceGraphicsPane} provides the background for the workspace. 
 * 
 *
 *
 * Created: Wed Feb 11 11:43:22 2009
 *
 * @author <a href="mailto:cpa@cs.vt.edu">Christopher Andrews</a>
 */
public class WorkspaceGraphicsPane extends JPanel {


    /**
     * Creates a new {@code WorkspaceGraphicsPane} instance.
     *
     * @param wp
     */
    public WorkspaceGraphicsPane(WorkspacePane wp) {
        super();
  

    }
    
    
    
    @Override
    public void paintComponent(Graphics g){
       AWLinkManager.getInstance().paintLinks((Graphics2D)g);         
    }
    
    

}
