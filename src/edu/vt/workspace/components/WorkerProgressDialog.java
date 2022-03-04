/*
 * WorkerProgressDialog.java
 *
 * Created on Apr 18, 2011, 1:44:33 PM
 */
package edu.vt.workspace.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.SwingWorker;
import javax.swing.Timer;

/**
 * The {@code WorkerProgressDialog} monitors the progress of a collection of SwingWorker objects.
 * @author Christopher Andrews (cpa@cs.vt.edu)
 */
public class WorkerProgressDialog extends javax.swing.JDialog {
private static final int HIDE_DELAY = 10000; // ten seconds 
    private List<SwingWorker> _workers;
    private int _completed;
    private Timer _timer;

    /** 
     * Create a new {@code WorkerProgressDialog}
     * 
     * @param label 
     * @param parent
     * @param workers 
     * @param modal
     */
    public WorkerProgressDialog(String label, List<SwingWorker> workers, java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        _label.setText(label);
        _workers = workers;
        _progressBar.setMaximum(100 * _workers.size());
        _completed = 0;
        ActionListener timeAction = new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
            
        };
        
        _timer = new Timer(HIDE_DELAY, timeAction);
        _timer.setRepeats(false);
        PropertyChangeListener listener = new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if ("progress".equals(evt.getPropertyName())) {
                    int current = _progressBar.getValue();
                    current -= (Integer) evt.getOldValue();
                    current += (Integer) evt.getNewValue();
                    _progressBar.setValue(current);
                    if (((SwingWorker) (evt.getSource())).isDone()) {
                        _completed++;
                    }

                    if (_completed == _workers.size()) { // we are done, lose the dialog box
                        _progressBar.setValue(_progressBar.getMaximum());
                        _button.setText("Done");
                        _timer.start();
                    }
                }
            }
        };

        for (SwingWorker worker : workers) {
            worker.addPropertyChangeListener(listener);
            worker.execute();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        _progressBar = new javax.swing.JProgressBar();
        _button = new javax.swing.JButton();
        _label = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                WorkerProgressDialog.this.windowClosed(evt);
            }
        });

        _button.setText("Stop");
        _button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonHandler(evt);
            }
        });

        _label.setText("Progress:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(_button)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(_progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
                        .addGap(20, 20, 20))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(_label)
                        .addContainerGap(322, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(_label)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 24, Short.MAX_VALUE)
                .addComponent(_progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(_button)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonHandler(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonHandler
        if (_completed == _workers.size()) {
            for (SwingWorker worker : _workers) {
                worker.cancel(true);
            }
        }
        setVisible(false);
    }//GEN-LAST:event_buttonHandler

    private void windowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_windowClosed
        if (_completed == _workers.size()) {
            for (SwingWorker worker : _workers) {
                worker.cancel(true);
            }
        }
    }//GEN-LAST:event_windowClosed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton _button;
    private javax.swing.JLabel _label;
    private javax.swing.JProgressBar _progressBar;
    // End of variables declaration//GEN-END:variables
}