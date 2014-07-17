/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package photoarchiverjava;

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 *
 * @author adam
 */
public class Archiver extends Thread{
    PALogic logic;
    private static Calendar start;
    
    public Archiver(PALogic l) {
        logic = l;
    }
    
    
    @Override
    public void run(){
        start = new GregorianCalendar();
        File[] files = logic.getInbox().listFiles(new ImageFilter());
        
        Counter.reset();
        Counter.setAll(logic.getInboxFileCount());
        logic.getFrame().getProgressLabel().setText("0%");
        
        for (int i = 0; i < files.length; ++i) {
            File f = files[i];
            logic.handleFile(f, logic.getMetadata(f));
            logic.getFrame().getProgressLabel().setText((i + 1) * 100 / files.length+ "%"); 
            //System.out.println(getAllMetadataAsString(f));
        }
        
        logic.printToSummary(
                "------------------------------------------\n"
                +"SUMMARY: " + Counter.getAll()
                + " photos processed in " + getProcessTime() + " seconds \n\tSuccessfully archived: " + Counter.getMoved()
                + "\n\tMissing metadata: " + Counter.getMissingData()
                + "\n\tFailed to move: " + Counter.getFail()
                + "\n\n");
        logic.getWriter().stopWriting();
    }
    
    private String getProcessTime(){
        return String.valueOf((new GregorianCalendar().getTimeInMillis() - start.getTimeInMillis()) / 1000);
    }
    
}
