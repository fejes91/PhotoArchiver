/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package photoarchiverjava;

import java.io.File;

/**
 *
 * @author adam
 */
public class Archiver extends Thread{
    PALogic logic;
    
    public Archiver(PALogic l) {
        logic = l;
    }
    
    
    @Override
    public void run(){
        File[] files = logic.getInbox().listFiles();
        Counter.setAll(files.length);
        for (File f : files) {
            logic.handleFile(f, logic.getMetadata(f));
            //System.out.println(getAllMetadataAsString(f));
        }
        
        logic.printToSummary(
                "------------------------------------------\n"
                +"SUMMARY: " + Counter.getAll()
                + " photos processed. \n\tSuccessfully archived: " + Counter.getMoved()
                + "\n\tMissing metadata: " + Counter.getMissingData()
                + "\n\tFailed to archive: " + Counter.getFail()
                + "\n\n\n\n");
    }
    
}
