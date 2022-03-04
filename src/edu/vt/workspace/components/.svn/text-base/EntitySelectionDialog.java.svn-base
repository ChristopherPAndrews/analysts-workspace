package edu.vt.workspace.components;

import edu.vt.workspace.components.utilities.EntityListPanel;
import edu.vt.workspace.data.AWEntity;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author cpa
 */
public class EntitySelectionDialog extends JDialog implements ActionListener {
    private AWEntity _value = null;
    private EntityListPanel _entityPanel;

    public EntitySelectionDialog(Frame owner, String title, boolean modal, String query, String initialType){
        super(owner, title, modal);
        JLabel queryLabel = new JLabel(query);
        _entityPanel = new EntityListPanel();
        if (initialType != null && !initialType.isEmpty()) {
            _entityPanel.setCurrentType(initialType);
        }


        //Create and initialize the buttons.
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        final JButton selectButton = new JButton("Select Entity");
        selectButton.setActionCommand("select");
        selectButton.addActionListener(this);
        selectButton.setEnabled(false);
        getRootPane().setDefaultButton(selectButton);

        _entityPanel.entityList.addListSelectionListener(new ListSelectionListener(){

            public void valueChanged(ListSelectionEvent lse) {
                selectButton.setEnabled(true);
            }
        });

        _entityPanel.entityList.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent me){
                if (me.getClickCount() == 2) {
                    _value = _entityPanel.getSelectedEntity();
                    setVisible(false);
                }
            }
        });


        //Lay out the buttons from left to right.
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(cancelButton);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(selectButton);

        //Put everything together, using the content pane's BorderLayout.
        Container contentPane = getContentPane();
        contentPane.add(queryLabel, BorderLayout.NORTH);
        contentPane.add(_entityPanel, BorderLayout.CENTER);
        contentPane.add(buttonPane, BorderLayout.PAGE_END);

        pack();

    }




    public void actionPerformed(ActionEvent ae) {
       if ("select".equals(ae.getActionCommand())) {
            _value = _entityPanel.getSelectedEntity();
        }
        setVisible(false);
    }


    public AWEntity getValue(){
        return _value;
    }
}
