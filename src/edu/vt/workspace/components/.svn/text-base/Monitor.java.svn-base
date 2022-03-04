package edu.vt.workspace.components;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

/**
 * This class is responsible for handling all of the logging in the study.
 * 
 * @author cpa
 */
public class Monitor extends ComponentAdapter implements InternalFrameListener{
    private static Monitor _instance = new Monitor();
    private Logger _logger; // we save this _logger, but it is recoverable from anywhere
   

    private Monitor(){
        // setup the universal _logger
        _logger = Logger.getLogger("edu.vt.workspace");
        FileHandler handler;
        DateFormat format = new SimpleDateFormat("'logs/event_log_'yyyy-MM-dd'T'HH-mm-ss'.txt'");
        try {
            handler = new FileHandler(format.format(new Date()));
            _logger.addHandler(handler);
        } catch (IOException ex) {
            Logger.getLogger(AnalystsWorkspace.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(AnalystsWorkspace.class.getName()).log(Level.SEVERE, null, ex);
        }
        _logger.setLevel(Level.ALL);
    }
    
    public static Monitor getInstance(){
        return _instance;
    }


    // Monitor events on InternalFrames
    public void internalFrameOpened(InternalFrameEvent e) {
        _logger.fine(e.toString());

    }

    public void internalFrameClosing(InternalFrameEvent e) {

    }

    public void internalFrameClosed(InternalFrameEvent e) {
        JInternalFrame frame = e.getInternalFrame();
        _logger.fine(e.toString());
        frame.removeInternalFrameListener(this);
        frame.removeComponentListener(this);

    }

    public void internalFrameIconified(InternalFrameEvent e) {

    }

    public void internalFrameDeiconified(InternalFrameEvent e) {

    }

    public void internalFrameActivated(InternalFrameEvent e) {
         _logger.fine(e.toString());
    }

    public void internalFrameDeactivated(InternalFrameEvent e) {
         _logger.fine(e.toString());
    }

    @Override
    public void componentMoved(ComponentEvent ce) {
        _logger.fine(ce.toString());
    }

    @Override
    public void componentResized(ComponentEvent ce) {
         _logger.fine(ce.toString());
    }

    public void monitor(AWInternalFrame frame) {
        frame.addInternalFrameListener(this);
        frame.addComponentListener(this);
        _logger.log(Level.FINE, "OPEN {0}", frame);

    }

    
    
    
}
