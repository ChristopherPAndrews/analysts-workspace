package edu.vt.workspace.components;

import edu.vt.workspace.data.AWController;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

/**
 * The SearchTool class provides a simple dialog that accepts a String query and then passes it
 * off to the controller to perform a search. It is designed to disappear as soon as it has been used.
 *
 * Created: Fri Feb 27 12:07:24 2009
 *
 * @author <a href="mailto:cpa@gambit.cs.vt.edu">Christopher Andrews</a>
 * @version 1.0
 */
public class SearchTool extends AWInternalFrame {
    static final SearchTool _instance = new SearchTool();
    private JTextField searchField;
    private AWInternalFrame _source;
    private String _query = "";

    /**
     * Creates a new <code>SearchTool</code> instance.
     *
     * @param controller
     */
    public SearchTool(Queriable controller) {
        super("Search Tool", controller);

        JLabel searchLabel = new JLabel("Find:");
        searchField = new JTextField(25);
        JButton searchButton = new JButton("Find");
        JPanel searchControls = new JPanel();
        SearchActionListener searchActionListener = new SearchActionListener();
        searchControls.add(searchLabel);
        searchControls.add(searchField);
        searchControls.add(searchButton);
        add(searchControls);
        setMaximizable(false);
        pack();
        searchButton.addActionListener(searchActionListener);
        searchField.addActionListener(searchActionListener);
        setDefaultCloseOperation(HIDE_ON_CLOSE);

        addInternalFrameListener(new InternalFrameAdapter() {

            @Override
            public void internalFrameActivated(InternalFrameEvent e) {
                searchField.selectAll();
            }
        });
    }

    public SearchTool() {
        this(null);
    }


    public static SearchTool getInstance(){
        return _instance;
    }


    public void setQuery(String query, AWInternalFrame source){
        _query = query;
        _source = source;
        searchField.setText(_query);
    }



    private class SearchActionListener implements ActionListener {

        public void actionPerformed(ActionEvent ae) {
            String query = searchField.getText();
            if (query != null && _controller != null) {
                if (_query.equals(query)){
                    AWController.getInstance().displaySearchResults(query, _source);
                }else{
                    AWController.getInstance().displaySearchResults(query, null);
                }
                setVisible(false);
                searchField.selectAll();
            }
        }
    } // end of SearchActionListener
} // end of SearchTool
