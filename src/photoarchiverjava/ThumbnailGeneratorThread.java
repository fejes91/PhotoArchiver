/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package photoarchiverjava;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Calendar;
import javax.swing.JPanel;

/**
 *
 * @author adam
 */
public class ThumbnailGeneratorThread extends Thread{
    PALogic logic;
    private static Calendar start;
    private File[] files;
    private JPanel panel;
    private int segment;
    
    public ThumbnailGeneratorThread(PALogic l, File[] files, int segment, JPanel panel) {
        this.logic = l;
        this.files = files;
        this.panel = panel;
        this.segment = segment;
    }
    
    @Override
    public void run(){
        System.out.println("Thread running for segment " + segment);
        for(int i = 3 * (segment - 1); i < (int)(files.length / 3 * segment) && i < files.length; ++i){
            File f = files[i];
            System.out.println("Segment " + segment + " handling file " + i + "\t" + f.getName());
            BufferedImage img = logic.generateThumbnail(f, 260.0, 180.0);
            panel.add(new ImagePanel(img));
            panel.updateUI();
        }
        
    }
    
}
