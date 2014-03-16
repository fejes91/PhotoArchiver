package photoarchiverjava;

import java.io.File;
import com.drew.metadata.Metadata;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Tag;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static final String NO_DATA = "FILE HAS NOT METADATA!";

    public static void main(String[] args) {

        File folder = new File("C:\\Users\\adam_fejes_dell\\Desktop\\Archiver Inbox");
        File[] files = folder.listFiles();

        for (File f : files) {
            process(f);
            //System.out.println(getAllMetadataAsString(f));
            //System.out.println(getMetadataAsString(f));
            
        }

        System.out.println("----------------------------------------------------------------------");
        System.out.println("SUMMARY: " + Counter.getAll()
                + " photos processed. \n \t Success: " + Counter.getSuccess()
                + "\n \t Fail: " + Counter.getFail());
    }

    private static void process(File f) {
        MyMetaData myMeta;

        String name = f.getName();
        Date date = null;
        String desc = "";
        LinkedList<String> keywords = new LinkedList<>();
        String cameraSettings = "";


        Boolean haveData = false;
        Metadata metadata = null;
        try {
            metadata = ImageMetadataReader.readMetadata(f);
        } catch (ImageProcessingException | IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (metadata != null) {
            for (Directory directory : metadata.getDirectories()) {
                if (directory.getName().toLowerCase().equals("iptc")) {
                    haveData = true;
                    for (Tag tag : directory.getTags()) {
                        if (tag.getTagName().toLowerCase().equals("keywords")) {
                            keywords.add((tag.getDescription()));
                        } else if (tag.getTagName().toLowerCase().equals("date created")) {
                            DateFormat df = new SimpleDateFormat("EEE MMM dd kk:mm:ss z yyyy", Locale.ENGLISH);
                            try {
                                date = df.parse(tag.getDescription());
                            } catch (ParseException ex) {
                                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                }

                if (directory.getName().toLowerCase().equals("exif subifd")) {
                    haveData = true;
                    for (Tag tag : directory.getTags()) {
                        if (tag.getTagName().toLowerCase().equals("exposure time")
                                || tag.getTagName().toLowerCase().equals("f-number")
                                || tag.getTagName().toLowerCase().equals("iso speed ratings")
                                || tag.getTagName().toLowerCase().equals("focal length")
                                || tag.getTagName().toLowerCase().equals("lens model")) {
                            cameraSettings += ("|" + tag.getDescription() + "|");
                        }
                    }
                    cameraSettings = cameraSettings.trim();
                }
            }

            if (!haveData) {
                Counter.incFail();
            } else {
                Counter.incSuccess();
            }
        }

        myMeta = new MyMetaData(date, keywords, name, desc, cameraSettings);
        /*TODO
            create folder structures
            move file to folders
            save meta to db
        */
        System.out.println("MyMeta: " + myMeta);                
    }

    private static String getAllMetadataAsString(File f) {
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

    private static String getMetadataAsString(File f) {
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

            if (!haveData) {
                str += "\t" + NO_DATA + "\n";
                Counter.incFail();
            } else {
                Counter.incSuccess();
            }
        }

        str += "\n----------------------------------------------------------------------";

        return str;
    }
}