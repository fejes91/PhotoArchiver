package photoarchiverjava;

import java.io.File;
import com.drew.metadata.Metadata;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Tag;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
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

        vault = new File("C:\\Users\\fejes_000\\Desktop\\PhotoArchiver Vault");
        inbox = new File("C:\\Users\\fejes_000\\Desktop\\PhotoArchiver Inbox");
        File[] files = inbox.listFiles();
        Counter.setAll(files.length);
        for (File f : files) {
            handleFile(f, getMetadata(f));
            
            //System.out.println(getAllMetadataAsString(f));
        }
        System.out.println("----------------------------------------------------------------------");
        System.out.println("SUMMARY: " + Counter.getAll()
                + " photos processed. \n \t Successfully archived: " + Counter.getMoved()
                + "\n \t Missing metadata: " + Counter.getMissingData()
                + "\n \t Failed to archive: " + Counter.getFail());
    }
    
    private static void storeMetadata(String path, MyMetaData md) {
        File metaDir = new File(vault.getAbsolutePath() + "\\meta");
        metaDir.mkdir();

        for (String keyword : md.getKeywords()) {
            File keywordDir = new File(metaDir.getAbsolutePath() + "\\" + keyword.charAt(0));
            keywordDir.mkdir();

            File keywordFile = new File(keywordDir.getAbsolutePath() + "\\" + keyword);
            try {
                keywordFile.createNewFile();
            } catch (IOException ex) {
                System.err.println(ex.getMessage());
            }
            //System.out.println("insert meta for file " + path + " to keyword file " + keywordFile.getName());
            try {
                PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(keywordFile, true)));
                out.println(path);
                out.flush();
            } catch (IOException ex) {
                System.err.println(ex.getMessage());
            }
            
            Counter.incMoved();
        }
    }

    private static MyMetaData getMetadata(File f) {
        String name = f.getName();
        Calendar date = new GregorianCalendar();
        String desc = "";
        LinkedList<String> keywords = new LinkedList<>();
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
                            String temp = tag.getDescription();
                            if(temp != null){
                                keywords = new LinkedList(Arrays.asList(temp.split(";")));
                            }
                        }
                    }
                }
                if (directory.getName().toLowerCase().equals("exif subifd")) {
                    for (Tag tag : directory.getTags()) {
                        if (tag.getTagName().toLowerCase().equals("date/time original")) {
                            DateFormat df = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.ENGLISH);
                            try {
                                date.setTime(df.parse(tag.getDescription()));
                            } catch (ParseException ex) {
                                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                }
            }
            if (!haveData) {
                Counter.incMissingData();
            }
        }
        return new MyMetaData(date, keywords, name, desc);
    }

    private static void handleFile(File file, MyMetaData md) {
        //System.out.println("MyMeta: " + myMeta);
        vault.mkdir();
        File year = new File(vault.getAbsolutePath() + "\\" + md.getDate().get(Calendar.YEAR));
        year.mkdir();
        File month = new File(year.getAbsolutePath() + "\\" + String.valueOf(md.getDate().get(Calendar.MONTH) + 1));
        month.mkdir();
        File day = new File(month.getAbsolutePath() + "\\" + md.getDate().get(Calendar.DAY_OF_MONTH));
        day.mkdir();
        try {
            //Files.move(Paths.get(file.getAbsolutePath()), Paths.get(day.getAbsolutePath() + "\\" + file.getName()));
            Files.copy(Paths.get(file.getAbsolutePath()), Paths.get(day.getAbsolutePath() + "\\" + file.getName()));
            storeMetadata(day.getAbsolutePath() + "\\" + file.getName(), md);
        } catch (FileAlreadyExistsException ex) {
            Counter.incFail();
            System.err.println("File already exists: " + ex.getMessage());
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
}
