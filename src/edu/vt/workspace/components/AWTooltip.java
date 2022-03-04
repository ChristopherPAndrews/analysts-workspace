/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.vt.workspace.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import javax.swing.JEditorPane;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.text.html.HTMLDocument;

/**
 *
 * @author cpa
 */
public class AWTooltip  extends AWInternalFrame {
    private static final AWTooltip INSTANCE = new AWTooltip();
    private JEditorPane text;

    private AWTooltip() {
        setupContents();
    }

     public static AWTooltip getInstance() {
        return INSTANCE;
    }

     private void setupContents() {
        text = new JEditorPane("text/html","");
        Font font = UIManager.getFont("Label.font");
        String bodyRule = "body { font-family: " + font.getFamily() + "; " +
                "font-size: " + font.getSize() + "pt; }";
        ((HTMLDocument)text.getDocument()).getStyleSheet().addRule(bodyRule);
         text.setEditable(false);
        getContentPane().add(text, BorderLayout.CENTER);
        
        setBorder(null);
        ((javax.swing.plaf.basic.BasicInternalFrameUI) getUI()).setNorthPane(null);
        setMaximizable(false);
        //setResizable(false);
        setClosable(false);
        text.setBackground(new Color(255,255,204));

        text.setBorder(new LineBorder(Color.black));

        setVisible(false);
        addInternalFrameListener(new InternalFrameAdapter(){
            @Override
            public void internalFrameDeactivated(InternalFrameEvent e){
                setVisible(false);
            }
        });

    }


     public void showTip(String tip, Point location){
         
         text.setText(tip);
         setLocation(location); 
         pack();
        setVisible(true);
     }

}
