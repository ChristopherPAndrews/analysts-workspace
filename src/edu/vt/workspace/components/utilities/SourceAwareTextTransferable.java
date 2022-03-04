/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.vt.workspace.components.utilities;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.plaf.UIResource;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;

/**
 * This is the second part of the enormous hack to add a little functionaity to text transfers while
 * retaining all of the original functionality. This is actually a mixture of classes: TextTransferable,
 * which was an inner class to the TextTransferHandler, and BasicTransferable, the parent of TextTransferable,
 * and a private class from within javax.swing.plaf.basic. The real kicker is that this even already stored the
 * source (albeit in the ill named c variable). Besides the merge, all I've don is add a single function to get the
 * source out.
 * @author cpa
 */
public class SourceAwareTextTransferable implements Transferable, UIResource {

    protected String plainData;
    protected String htmlData;
    private static DataFlavor[] htmlFlavors;
    private static DataFlavor[] stringFlavors;
    private static DataFlavor[] plainFlavors;
    private Position p0;
    private Position p1;
    private String mimeType;
    private String richText;
    private JTextComponent source;


    static {
        try {
            htmlFlavors = new DataFlavor[3];
            htmlFlavors[0] = new DataFlavor("text/html;class=java.lang.String");
            htmlFlavors[1] = new DataFlavor("text/html;class=java.io.Reader");
            htmlFlavors[2] = new DataFlavor("text/html;charset=unicode;class=java.io.InputStream");

            plainFlavors = new DataFlavor[3];
            plainFlavors[0] = new DataFlavor("text/plain;class=java.lang.String");
            plainFlavors[1] = new DataFlavor("text/plain;class=java.io.Reader");
            plainFlavors[2] = new DataFlavor("text/plain;charset=unicode;class=java.io.InputStream");

            stringFlavors = new DataFlavor[2];
            stringFlavors[0] = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=java.lang.String");
            stringFlavors[1] = DataFlavor.stringFlavor;

        } catch (ClassNotFoundException cle) {
            System.err.println("error initializing SourceAwareTextTranferable");
        }
    }

    SourceAwareTextTransferable(JTextComponent c, int start, int end) {

        source = c;
        System.out.println("creating one of mine");
        Document doc = c.getDocument();

        try {
            p0 = doc.createPosition(start);
            p1 = doc.createPosition(end);

            plainData = c.getSelectedText();

            if (c instanceof JEditorPane) {
                JEditorPane ep = (JEditorPane) c;

                mimeType = ep.getContentType();

                if (mimeType.startsWith("text/plain")) {
                    return;
                }

                StringWriter sw = new StringWriter(p1.getOffset() - p0.getOffset());
                ep.getEditorKit().write(sw, doc, p0.getOffset(), p1.getOffset() - p0.getOffset());

                if (mimeType.startsWith("text/html")) {
                    htmlData = sw.toString();
                } else {
                    richText = sw.toString();
                }
            }
        } catch (BadLocationException ble) {
        } catch (IOException ioe) {
        }
    }

    /**
     * Return the object that originated the DnD operation. [my only addition to this code]
     * @return the source of the data contained in this Transferable
     */
    public JTextComponent getSource(){
        return source;
    }
    
    void removeText() {
        if ((p0 != null) && (p1 != null) && (p0.getOffset() != p1.getOffset())) {
            try {
                Document doc = source.getDocument();
                doc.remove(p0.getOffset(), p1.getOffset() - p0.getOffset());
            } catch (BadLocationException e) {
            }
        }
    }

    /**
     * Returns an array of DataFlavor objects indicating the flavors the data
     * can be provided in.  The array should be ordered according to preference
     * for providing the data (from most richly descriptive to least descriptive).
     * @return an array of data flavors in which this data can be transferred
     */
    public DataFlavor[] getTransferDataFlavors() {
        DataFlavor[] richerFlavors = getRicherFlavors();
        int nRicher = (richerFlavors != null) ? richerFlavors.length : 0;
        int nHTML = (isHTMLSupported()) ? htmlFlavors.length : 0;
        int nPlain = (isPlainSupported()) ? plainFlavors.length : 0;
        int nString = (isPlainSupported()) ? stringFlavors.length : 0;
        int nFlavors = nRicher + nHTML + nPlain + nString;
        DataFlavor[] flavors = new DataFlavor[nFlavors];

        // fill in the array
        int nDone = 0;
        if (nRicher > 0) {
            System.arraycopy(richerFlavors, 0, flavors, nDone, nRicher);
            nDone += nRicher;
        }
        if (nHTML > 0) {
            System.arraycopy(htmlFlavors, 0, flavors, nDone, nHTML);
            nDone += nHTML;
        }
        if (nPlain > 0) {
            System.arraycopy(plainFlavors, 0, flavors, nDone, nPlain);
            nDone += nPlain;
        }
        if (nString > 0) {
            System.arraycopy(stringFlavors, 0, flavors, nDone, nString);
            nDone += nString;
        }
        return flavors;
    }

    /**
     * Returns whether or not the specified data flavor is supported for
     * this object.
     * @param flavor the requested flavor for the data
     * @return boolean indicating whether or not the data flavor is supported
     */
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        DataFlavor[] flavors = getTransferDataFlavors();
        for (int i = 0; i < flavors.length; i++) {
            if (flavors[i].equals(flavor)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns an object which represents the data to be transferred.  The class
     * of the object returned is defined by the representation class of the flavor.
     *
     * @param flavor the requested flavor for the data
     * @return 
     * @see DataFlavor#getRepresentationClass
     * @exception IOException                if the data is no longer available
     *              in the requested flavor.
     * @exception UnsupportedFlavorException if the requested data flavor is
     *              not supported.
     */
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        DataFlavor[] richerFlavors = getRicherFlavors();
        if (isRicherFlavor(flavor)) {
            return getRicherData(flavor);
        } else if (isHTMLFlavor(flavor)) {
            String data = getHTMLData();
            data = (data == null) ? "" : data;
            if (String.class.equals(flavor.getRepresentationClass())) {
                return data;
            } else if (Reader.class.equals(flavor.getRepresentationClass())) {
                return new StringReader(data);
            } else if (InputStream.class.equals(flavor.getRepresentationClass())) {
                return new StringBufferInputStream(data);
            }
        // fall through to unsupported
        } else if (isPlainFlavor(flavor)) {
            String data = getPlainData();
            data = (data == null) ? "" : data;
            if (String.class.equals(flavor.getRepresentationClass())) {
                return data;
            } else if (Reader.class.equals(flavor.getRepresentationClass())) {
                return new StringReader(data);
            } else if (InputStream.class.equals(flavor.getRepresentationClass())) {
                return new StringBufferInputStream(data);
            }
        // fall through to unsupported

        } else if (isStringFlavor(flavor)) {
            String data = getPlainData();
            data = (data == null) ? "" : data;
            return data;
        }
        throw new UnsupportedFlavorException(flavor);
    }

    // --- richer subclass flavors ----------------------------------------------
    protected boolean isRicherFlavor(DataFlavor flavor) {
        DataFlavor[] richerFlavors = getRicherFlavors();
        int nFlavors = (richerFlavors != null) ? richerFlavors.length : 0;
        for (int i = 0; i < nFlavors; i++) {
            if (richerFlavors[i].equals(flavor)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Some subclasses will have flavors that are more descriptive than HTML
     * or plain text.  If this method returns a non-null value, it will be
     * placed at the start of the array of supported flavors.
     * @return
     */
    protected DataFlavor[] getRicherFlavors() {
        if (richText == null) {
            return null;
        }

        try {
            DataFlavor[] flavors = new DataFlavor[3];
            flavors[0] = new DataFlavor(mimeType + ";class=java.lang.String");
            flavors[1] = new DataFlavor(mimeType + ";class=java.io.Reader");
            flavors[2] = new DataFlavor(mimeType + ";class=java.io.InputStream;charset=unicode");
            return flavors;
        } catch (ClassNotFoundException cle) {
            // fall through to unsupported (should not happen)
        }

        return null;
    }

    protected Object getRicherData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (richText == null) {
            return null;
        }

        if (String.class.equals(flavor.getRepresentationClass())) {
            return richText;
        } else if (Reader.class.equals(flavor.getRepresentationClass())) {
            return new StringReader(richText);
        } else if (InputStream.class.equals(flavor.getRepresentationClass())) {
            return new StringBufferInputStream(richText);
        }
        throw new UnsupportedFlavorException(flavor);
    }

    // --- html flavors ----------------------------------------------------------
    /**
     * Returns whether or not the specified data flavor is an HTML flavor that
     * is supported.
     * @param flavor the requested flavor for the data
     * @return boolean indicating whether or not the data flavor is supported
     */
    protected boolean isHTMLFlavor(DataFlavor flavor) {
        DataFlavor[] flavors = htmlFlavors;
        for (int i = 0; i < flavors.length; i++) {
            if (flavors[i].equals(flavor)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Should the HTML flavors be offered?  If so, the method
     * getHTMLData should be implemented to provide something reasonable.
     * @return
     */
    protected boolean isHTMLSupported() {
        return htmlData != null;
    }

    /**
     * Fetch the data in a text/html format
     * @return
     */
    protected String getHTMLData() {
        return htmlData;
    }

    // --- plain text flavors ----------------------------------------------------
    /**
     * Returns whether or not the specified data flavor is an plain flavor that
     * is supported.
     * @param flavor the requested flavor for the data
     * @return boolean indicating whether or not the data flavor is supported
     */
    protected boolean isPlainFlavor(DataFlavor flavor) {
        DataFlavor[] flavors = plainFlavors;
        for (int i = 0; i < flavors.length; i++) {
            if (flavors[i].equals(flavor)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Should the plain text flavors be offered?  If so, the method
     * getPlainData should be implemented to provide something reasonable.
     * @return
     */
    protected boolean isPlainSupported() {
        return plainData != null;
    }

    /**
     * Fetch the data in a text/plain format.
     * @return
     */
    protected String getPlainData() {
        return plainData;
    }

    // --- string flavorss --------------------------------------------------------
    /**
     * Returns whether or not the specified data flavor is a String flavor that
     * is supported.
     * @param flavor the requested flavor for the data
     * @return boolean indicating whether or not the data flavor is supported
     */
    protected boolean isStringFlavor(DataFlavor flavor) {
        DataFlavor[] flavors = stringFlavors;
        for (int i = 0; i < flavors.length; i++) {
            if (flavors[i].equals(flavor)) {
                return true;
            }
        }
        return false;
    }
}
