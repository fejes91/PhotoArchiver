package photoarchiverjava;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

public class ImagePanel extends JPanel{

    private BufferedImage thumbnail;
    private Image image;

    public ImagePanel(Dimension d, Image image) {
        this.setPreferredSize(d);
        this.image = image;
    }
    
    public void setImage(BufferedImage thumbnail){
        this.thumbnail = thumbnail;
    }
    
    public MyMetaData getMetaData(){
        return image.getMeta();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(thumbnail, 0, 0, null); // see javadoc for more info on the parameters            
    }

    void addActionListener(ActionListener actionListener) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}