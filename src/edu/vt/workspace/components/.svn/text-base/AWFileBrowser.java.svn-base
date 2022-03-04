 /*
 * AWFileBrowser.java
 *
 * Created on Jun 26, 2010, 11:04:37 PM
 */

package edu.vt.workspace.components;

import edu.vt.workspace.components.utilities.AWDocumentComparator;
import edu.vt.workspace.components.utilities.FileListCellRenderer;
import edu.vt.workspace.components.utilities.LayoutManager;
import edu.vt.workspace.components.utilities.SortedListModel;
import edu.vt.workspace.data.AWController;
import edu.vt.workspace.data.AWDocument;
import edu.vt.workspace.data.AWSavable;
import edu.vt.workspace.data.AWWriter;
import edu.vt.workspace.data.FileManager;
import java.awt.Container;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * @todo hook this up to update automatically on new files
 * @todo need to change selections after switch to new view
 * @author cpa
 */
public class AWFileBrowser extends AWInternalFrame implements AWSavable{
    private FileListCellRenderer cellRenderer = null;
    private AWDocumentComparator _comparator;
    
    /** Creates new form AWFileBrowser
     * 
     */
    public AWFileBrowser() {
        super("File Browser");
        initComponents();
    }

    
    private void initializeFileList(){
        _comparator = new AWDocumentComparator(AWDocumentComparator.SortType.DATE);
        ListModel model = jList1.getModel();

        jList1.setModel(new SortedListModel(model, _comparator));
        cellRenderer = new FileListCellRenderer(jList1);
        jList1.setCellRenderer(cellRenderer);
        jList1.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent me) {
                if (me.getClickCount() == 2) {
                    int index = jList1.locationToIndex(me.getPoint());
                    AWDocument doc = (AWDocument) (jList1.getModel().getElementAt(index));
                   AWController.getInstance().displayFile(doc, true);
                }
            }
        });

        jList1.addKeyListener(new KeyAdapter(){

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER){
                   int[] selected =  jList1.getSelectedIndices();
                   if (selected.length > 0)
                       LayoutManager.getInstance().clearSelected();
                   for (int i = 0; i < selected.length; i++){
                       AWDocument doc = (AWDocument) (jList1.getModel().getElementAt(selected[i]));
                       AWInternalFrame frame = AWController.getInstance().displayFile(doc, false);
                       LayoutManager.getInstance().addFrameToSelection(frame);
                       
                    }
                }
            }
       });

        jList1.addListSelectionListener(new SelectionHelper());
    }

    @Override
    protected void postOpenInit() {
        Container parent = getParent();
        parent.addContainerListener(new ContainerAdapter() {

            @Override
            public void componentAdded(ContainerEvent ce) {
                jList1.repaint();
            }

            @Override
            public void componentRemoved(ContainerEvent ce){
                jList1.repaint();
            }
        });
        
        setDisplayMode(getDisplayMode());
    }

    public void setDisplayMode(String mode){
        if ("BY_TITLE".equals(mode)){
            //displayByTitle(null);
            titleButton.doClick();
        }else if ("BY_NAME".equals(mode)){
            //displayByName(null);
            nameButton.doClick();
        }else if ("BY_DATE".equals(mode)){
            //displayByDate(null);
            dateButton.doClick();
        }else{
            System.out.println("Unknown FileBRowser mode encountered");
        }

    }
    
    
    public String getDisplayMode(){
        return cellRenderer.getDisplayMode().toString();
    }


    @Override
     public void writeData(AWWriter writer) {
         super.writeData(writer);
         writer.write("displayMode", cellRenderer.getDisplayMode().toString());
    }


    private void fixSelections(Object[] selectedItems){
        int[] newSelections = new int[selectedItems.length];
        int k = 0;
        jList1.clearSelection();
        ListModel model = jList1.getModel();
        for (int i = 0; i < model.getSize(); i++){
            Object obj = model.getElementAt(i);
            for (int j = 0; j < selectedItems.length;j++){
                if (selectedItems[j] == obj){
                    newSelections[k++] = i;
                }

            }
        }
        jList1.setSelectedIndices(newSelections);
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new JList(FileManager.getInstance().getDocuments());
        initializeFileList();
        nameButton = new javax.swing.JToggleButton();
        titleButton = new javax.swing.JToggleButton();
        dateButton = new javax.swing.JToggleButton();

        setTitle("File Browser");
        setMinimumSize(new java.awt.Dimension(225, 150));
        setPreferredSize(new java.awt.Dimension(450, 496));

        jScrollPane1.setViewportView(jList1);

        buttonGroup1.add(nameButton);
        nameButton.setText("By Name");
        nameButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayByName(evt);
            }
        });

        buttonGroup1.add(titleButton);
        titleButton.setSelected(true);
        titleButton.setText("By Title");
        titleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayByTitle(evt);
            }
        });

        buttonGroup1.add(dateButton);
        dateButton.setText("By Date");
        dateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayByDate(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(titleButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nameButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dateButton)
                .addContainerGap(122, Short.MAX_VALUE))
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 426, Short.MAX_VALUE)
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {nameButton, titleButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(titleButton)
                    .addComponent(nameButton)
                    .addComponent(dateButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 373, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void displayByName(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displayByName

        if (cellRenderer != null)
            cellRenderer.setDisplayMode(FileListCellRenderer.DisplayMode.BY_NAME);
        _comparator.setSortType(AWDocumentComparator.SortType.NAME);
         Object[] selectedItems = jList1.getSelectedValues();
        if (jList1.getModel() instanceof SortedListModel){
            ((SortedListModel)jList1.getModel()).sort();
        }
        fixSelections(selectedItems);
        jList1.repaint();
    }//GEN-LAST:event_displayByName

    private void displayByTitle(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displayByTitle
        if (cellRenderer != null)
            cellRenderer.setDisplayMode(FileListCellRenderer.DisplayMode.BY_TITLE);
        _comparator.setSortType(AWDocumentComparator.SortType.TITLE);
        Object[] selectedItems = jList1.getSelectedValues();
        if (jList1.getModel() instanceof SortedListModel) {
            ((SortedListModel) jList1.getModel()).sort();
        }
        fixSelections(selectedItems);
        jList1.repaint();
    }//GEN-LAST:event_displayByTitle

    private void displayByDate(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displayByDate
        if (cellRenderer != null)
            cellRenderer.setDisplayMode(FileListCellRenderer.DisplayMode.BY_DATE);
        _comparator.setSortType(AWDocumentComparator.SortType.DATE);
        Object[] selectedItems = jList1.getSelectedValues();
        if (jList1.getModel() instanceof SortedListModel) {
            ((SortedListModel) jList1.getModel()).sort();
        }
        fixSelections(selectedItems);
        jList1.repaint();
    }//GEN-LAST:event_displayByDate


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JToggleButton dateButton;
    private javax.swing.JList jList1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToggleButton nameButton;
    private javax.swing.JToggleButton titleButton;
    // End of variables declaration//GEN-END:variables

    /**
     * A simple class to bring up the preview pane on selection events
     */
    private class SelectionHelper implements ListSelectionListener {

        public void valueChanged(ListSelectionEvent e) {
             JList list = (JList) e.getSource();
             AWDocument doc;
             int selectedCount = 0;
             int selectedIndex = 0;

             for (int i = e.getFirstIndex(); i <= e.getLastIndex(); i++) {
                if (list.isSelectedIndex(i)) {
                    selectedCount++;
                    selectedIndex = i;
                 }
            }

             // preview text if we've only selected a single item
            if (selectedCount == 1) {
                // only one frame selected, show it
                doc = (AWDocument) (list.getModel().getElementAt(selectedIndex));
                AWPopupTextWindow.getInstance().showDocument(doc, null, AWFileBrowser.this);

            }else{
                 AWPopupTextWindow.getInstance().showDocument(null, null, AWFileBrowser.this);
            }

        }

    }

}
