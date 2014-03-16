package photoarchiverjava;

import java.io.File;
import com.drew.metadata.Metadata;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Tag;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    
    private static final String NO_DATA = "FILE HAS NOT METADATA!";

    public static void main(String[] args) throws ImageProcessingException, IOException {

        File folder = new File("C:\\Users\\adam_fejes_dell\\Desktop\\Archiver Inbox");
        File[] files = folder.listFiles();

        for (File f : files) {
            //System.out.println(getAllMetadata(f));
            System.out.println(getMetadata(f));
        }
        
        System.out.println("----------------------------------------------------------------------");
        System.out.println("SUMMARY: " + Counter.getAll() + 
                " photos processed. \n \t Success: " + Counter.getSuccess() + 
                "\n \t Fail: " + Counter.getFail());
    }

    private static String getAllMetadata(File f) {
        String str = "";
        Metadata metadata = null;
        try {
            metadata = ImageMetadataReader.readMetadata(f);


        } catch (ImageProcessingException | IOException ex) {
            Logger.getLogger(Main.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        str += "Filename: " + f.getName() + "\n";
        if (metadata != null) {
            for (Directory directory : metadata.getDirectories()) {
                for (Tag tag : directory.getTags()) {
                    str += "\t" + tag + "\n";
                }
            }
        }
        str += "\n----------------------------------------------------------------------";

        return str;
    }

    private static String getMetadata(File f) {
        Boolean haveData = false;
        String str = "";
        Metadata metadata = null;
        try {
            metadata = ImageMetadataReader.readMetadata(f);
        } catch (ImageProcessingException | IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (metadata != null) {
            str += "Filename: " + f.getName() + "\n";
            for (Directory directory : metadata.getDirectories()) {
                if (directory.getName().toLowerCase().equals("iptc")) {
                    haveData = true;
                    for (Tag tag : directory.getTags()) {
                        if (tag.getTagName().toLowerCase().equals("keywords") || tag.getTagName().toLowerCase().equals("date created")) {
                            str += ("\t" + tag.getDescription() + "\n");
                        }
                    }
                }
            }
            
            if(!haveData){
                str += "\t"+ NO_DATA + "\n";
                Counter.incFail();
            }
            else{
                Counter.incSuccess();
            }
        }
        
        str += "\n----------------------------------------------------------------------";
        
        return str;
    }
}