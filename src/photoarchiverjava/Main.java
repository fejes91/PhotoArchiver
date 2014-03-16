package photoarchiverjava;

import java.io.File;
import com.drew.metadata.Metadata;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Tag;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static final String NO_DATA = "FILE HAS NOT METADATA!";
    private static File vault;
    private static File inbox;

    public static void main(String[] args) {

        vault = new File("C:\\Users\\adam_fejes_dell\\Desktop\\Archiver Vault");
        inbox = new File("C:\\Users\\adam_fejes_dell\\Desktop\\Archiver Inbox");
        File[] files = inbox.listFiles();
        Counter.setAll(files.length);
        for (File f : files) {
            process(f);
            //System.out.println(getAllMetadataAsString(f));
        }
        System.out.println("----------------------------------------------------------------------");
        System.out.println("SUMMARY: " + Counter.getAll()
                + " photos processed. \n \t Successfully archived: " + Counter.getMoved()
                + "\n \t Missing metadata: " + Counter.getMissingData()
                + "\n \t Failed to archive: " + Counter.getFail());
    }

    private static void process(File f) {
        MyMetaData myMeta;
        
        String name = f.getName();
        Calendar date = new GregorianCalendar();
        String desc = "";
        LinkedList<String> keywords = new LinkedList<>();
        String cameraSettings = "";
        
        Path path = Paths.get(f.getAbsolutePath());
        BasicFileAttributes attributes;
        try {
            attributes = Files.readAttributes(path, BasicFileAttributes.class);
            FileTime time = attributes.creationTime();
            date.setTimeInMillis(time.toMillis());
        } catch (IOException ex) {
            System.err.println("File attribute reading error: " + ex.getMessage());
        }


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
                    for (Tag tag : directory.getTags()) {
                        if (tag.getTagName().toLowerCase().equals("keywords")) {
                            haveData = true;
                            keywords.add((tag.getDescription()));
                        }
                    }
                }
                if (directory.getName().toLowerCase().equals("exif ifd0")) {
                    for (Tag tag : directory.getTags()) {
                        if (tag.getTagName().toLowerCase().equals("model")) {
                            haveData = true;
                            cameraSettings += ("|" + tag.getDescription() + "|");
                        }
                    }
                }
                if (directory.getName().toLowerCase().equals("exif subifd")) {
                    for (Tag tag : directory.getTags()) {
                        if (tag.getTagName().toLowerCase().equals("exposure time")
                                || tag.getTagName().toLowerCase().equals("f-number")
                                || tag.getTagName().toLowerCase().equals("iso speed ratings")
                                || tag.getTagName().toLowerCase().equals("focal length")
                                || tag.getTagName().toLowerCase().equals("lens model")) {
                            cameraSettings += ("|" + tag.getDescription() + "|");
                        }

                        if (tag.getTagName().toLowerCase().equals("date/time original")) {
                            DateFormat df = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.ENGLISH);
                            try {
                                date.setTime(df.parse(tag.getDescription()));
                            } catch (ParseException ex) {
                                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                    cameraSettings = cameraSettings.trim();
                }
            }

            if (!haveData) {
                Counter.incMissingData();
            }
        }

        myMeta = new MyMetaData(date, keywords, name, desc, cameraSettings);
        moveFile(f, myMeta.getDate());
        //saveToDB(f, myMeta);
        System.out.println("MyMeta: " + myMeta);
    }

    private static void moveFile(File file, Calendar date) {
        vault.mkdir();

        File year = new File(vault.getAbsolutePath() + "\\" + date.get(Calendar.YEAR));
        year.mkdir();

        File month = new File(year.getAbsolutePath() + "\\" + String.valueOf(date.get(Calendar.MONTH) + 1));
        month.mkdir();

        File day = new File(month.getAbsolutePath() + "\\" + date.get(Calendar.DAY_OF_MONTH));
        day.mkdir();
        try {
            //Files.move(Paths.get(file.getAbsolutePath()), Paths.get(day.getAbsolutePath() + "\\" + file.getName()));
            Files.copy(Paths.get(file.getAbsolutePath()), Paths.get(day.getAbsolutePath() + "\\" + file.getName()));
            Counter.incMoved();
        } catch (FileAlreadyExistsException ex) {
            Counter.incFail();
            System.err.println("File already exists: " + ex.getMessage());
            //TODO újra más névvel
        } catch (IOException ex) {
            Counter.incFail();
            System.err.println("File moving error: " + ex.getMessage());
        }
    }

    private static String getAllMetadataAsString(File f) {
        String str = "";
        Metadata metadata = null;
        try {
            metadata = ImageMetadataReader.readMetadata(f);
        } catch (ImageProcessingException | IOException ex) {
            System.err.println("Metadata reading error: " + ex.getMessage() + "\t" + ex.getClass());
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
            System.err.println("Metadata reading error: " + ex.getMessage() + "\t" + ex.getClass());
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
                Counter.incMissingData();
            }
        }
        str += "\n----------------------------------------------------------------------";
        return str;
    }
}