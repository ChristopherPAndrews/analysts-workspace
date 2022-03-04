package edu.vt.workspace.plaf;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIDefaults;
import javax.swing.plaf.metal.MetalLookAndFeel;

/**
 * This class provides the specialized look and feel for AW. For the most part, this
 * just uses the metal look and feel. This makes the application consistent across all
 * platforms, if not perhaps the prettiest. At the moment, the only things that
 * are added in are new UI classes for the JInternalFrames and JDesktopIcons. These
 * additions allow me to implement multidragging and to give the DesktopIcon for the
 * entity view a new look and behavior.
 * 
 * @author cpa
 */
public class AWLookAndFeel extends MetalLookAndFeel{

    /**
     * Returns the name of this look and feel. This returns
     * {@code "AW - Metal}.
     * @return the name of this look and feel
     */
    @Override
    public String getName(){
        return "AW - Metal";
    }

     /**
     * Returns an identifier of this look and feel. This returns
     * {@code "AW - Metal}.
     * @return the identifier of this look and feel
     */
    @Override
    public String getID(){
        return "AW - Metal";
    }


    /**
     * Returns a short description of this look and feel. This returns
     * {@code "A specialized add-on to the default Metal theme"}.

     * @return a short description for the look and feel
     */
    @Override
    public String getDescription() {
        return "A specialized add-on to the default Metal theme";
    }


    /**
     *  Populates the {@code table} with the default UI delegates. This leaves
     * most delegates untouched, adding new ones for the JInternalFrames and the JDesktopIcons.
     * @param table
     */
    @Override
    protected void initClassDefaults (UIDefaults table){
        String className;
        Class uiClass;
        super.initClassDefaults(table);
        try {
            className = "edu.vt.workspace.plaf.AWInternalFrameUI";
            uiClass = Class.forName(className);
            table.put("InternalFrameUI", className);
            table.put(className, uiClass);
        } catch (ClassNotFoundException ex) {
            System.out.println("Unable to find AWInternalFrameUI");
            Logger.getLogger(AWLookAndFeel.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            className = "edu.vt.workspace.plaf.AWDesktopIconUI";
            uiClass = Class.forName(className);
            table.put("DesktopIconUI", className);
            table.put(className, uiClass);
        } catch (ClassNotFoundException ex) {
            System.out.println("Unable to find AWDesktopIconUI");
            Logger.getLogger(AWLookAndFeel.class.getName()).log(Level.SEVERE, null, ex);
        }

       


    }

}
