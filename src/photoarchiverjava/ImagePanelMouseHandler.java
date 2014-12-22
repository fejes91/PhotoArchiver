package photoarchiverjava;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ImagePanelMouseHandler extends MouseAdapter {

    private JPanel imageEditorPanel;

    public ImagePanelMouseHandler(JPanel imageEditorPanel) {
        this.imageEditorPanel = imageEditorPanel;
    }
    
        
    @Override
    public void mousePressed(MouseEvent e) {
        ImagePanel panel = (ImagePanel) e.getSource();
        System.out.println("Clicked image meta: " + panel.getMetaData().getName());
        imageEditorPanel.add(new JLabel("Clicked image meta: " + panel.getMetaData().getName()));
        imageEditorPanel.updateUI();
    }
}
