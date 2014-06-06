package photoarchiverjava;

import java.io.IOException;
import java.io.OutputStream;
 
import javax.swing.JTextArea;
 
/**
 * This class extends from OutputStream to redirect output to a JTextArrea
 * @author www.codejava.net
 *
 */
public class CustomOutputStream extends OutputStream {
    private JTextArea textArea;
     
    public CustomOutputStream(JTextArea textArea) {
        this.textArea = textArea;
    }
     
    @Override
    public void write(int b) throws IOException {
        textArea.append(String.valueOf((char)b));
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }
}
