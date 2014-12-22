/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package photoarchiverjava;

import java.io.File;
import java.util.Calendar;
import java.util.List;
import javax.swing.JPanel;

/**
 *
 * @author adam
 */
public class ThumbnailGeneratorThread extends Thread{
    PALogic logic;
    private static Calendar start;
    private File[] files;
    private List<ImagePanel> imagePanels;
    private JPanel panel;
    private int segment, numberOfSegments;
    
    public ThumbnailGeneratorThread(PALogic l, File[] files, int segment, int numberOfSegments, List<ImagePanel> imagePanels, JPanel panel) {
        this.logic = l;
        this.files = files;
        this.imagePanels = imagePanels;
        this.panel = panel;
        this.segment = segment;
        this.numberOfSegments = numberOfSegments;
    }
    
    @Override
    public void run(){
        System.out.println("Thread running for segment " + segment);
        for(int i = files.length / numberOfSegments * (segment - 1); i < (int)(files.length / numberOfSegments * segment) && i < files.length; ++i){
            File f = files[i];
            System.out.println("Segment " + segment + " handling file " + i + "\t" + f.getName());
            imagePanels.get(i).setImage(logic.generateThumbnail(f, 260.0, 180.0));
            panel.updateUI();
        }
        System.out.println("SEGMENT " + segment + " finished");
    }
    
}
