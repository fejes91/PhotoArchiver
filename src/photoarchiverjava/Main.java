package photoarchiverjava;

import java.io.File;
import com.drew.metadata.Metadata;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Tag;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
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
import javax.imageio.ImageIO;

public class Main {

    private static final String NO_DATA = "FILE HAS NOT METADATA!";
    private static File vault;
    private static File inbox;

    public static void main(String[] args) {

        //TODO thumbnail generálás
        //TODO GUI
        //TODO kulcsszó karbantartó
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
            /*File keywordDir = new File(metaDir.getAbsolutePath() + "\\" + keyword.charAt(0));
             keywordDir.mkdir();

             File keywordFile = new File(keywordDir.getAbsolutePath() + "\\" + keyword);*/
            File keywordFile = new File(metaDir.getAbsolutePath() + "\\" + keyword + ".js");
            try {
                if (keywordFile.createNewFile()) {
                    File listDir = new File(metaDir.getAbsolutePath() + "\\list");
                    listDir.mkdir();
                    File keywordList = new File(metaDir.getAbsolutePath() + "\\" + listDir.getName() + "\\keywordlist.js");
                    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(keywordList, true)));
                    out.println("keywordlist.push(\"" + keyword + "\");");
                    out.flush();
                }
            } catch (IOException ex) {
                System.err.println(ex.getMessage());
            }
            //System.out.println("insert meta for file " + path + " to keyword file " + keywordFile.getName());
            try {
                PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(keywordFile, true)));
                out.println("addPicture(\"" + path + "\", \"" + keyword + "\");");
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
                            if (temp != null) {
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
        System.out.println("Handling file: " + file.getName());
        vault.mkdir();
        File year = new File(vault.getAbsolutePath() + "\\" + md.getDate().get(Calendar.YEAR));
        year.mkdir();
        File month = new File(year.getAbsolutePath() + "\\" + String.valueOf(md.getDate().get(Calendar.MONTH) + 1));
        month.mkdir();
        File day = new File(month.getAbsolutePath() + "\\" + md.getDate().get(Calendar.DAY_OF_MONTH));
        day.mkdir();

        File thumbnails = new File(vault.getAbsolutePath() + "\\thumbnails");
        thumbnails.mkdir();
        File tYear = new File(thumbnails.getAbsolutePath() + "\\" + md.getDate().get(Calendar.YEAR));
        tYear.mkdir();
        File tMonth = new File(tYear.getAbsolutePath() + "\\" + String.valueOf(md.getDate().get(Calendar.MONTH) + 1));
        tMonth.mkdir();
        File tDay = new File(tMonth.getAbsolutePath() + "\\" + md.getDate().get(Calendar.DAY_OF_MONTH));
        tDay.mkdir();

        try {
            //Files.move(Paths.get(file.getAbsolutePath()), Paths.get(day.getAbsolutePath() + "\\" + file.getName()));
            Files.copy(Paths.get(file.getAbsolutePath()), Paths.get(day.getAbsolutePath() + "\\" + file.getName()));

            BufferedImage thumbnail = generateThumbnail(file);
            ImageIO.write(thumbnail, "jpg", new File(tDay.getAbsolutePath() + "\\" + file.getName()));

            storeMetadata(year.getName() + "/" + month.getName() + "/" + day.getName() + "/" + file.getName(), md);
        } catch (FileAlreadyExistsException ex) {
            Counter.incFail();
            System.err.println("File already exists: " + ex.getMessage());
        } catch (IOException ex) {
            Counter.incFail();
            System.err.println("File moving error: " + ex.getMessage());
        }
    }

    private static BufferedImage generateThumbnail(File image) {
        BufferedImage originalImage;
        try {
            originalImage = ImageIO.read(image);
            double originalWidth = originalImage.getWidth();
            double originalHeight = originalImage.getHeight();
            boolean isLandscape = originalHeight < originalWidth;

            Double resizedWidth, resizedHeight;
            if (isLandscape) {
                resizedWidth = 600.0;
                resizedHeight = originalHeight / (originalWidth / resizedWidth);
            } else {
                resizedHeight = 400.0;
                resizedWidth = originalWidth / (originalHeight / resizedHeight);
            }

            BufferedImage resizedImage = new BufferedImage(resizedWidth.intValue(), resizedHeight.intValue(), originalImage.getType());
            Graphics2D g = resizedImage.createGraphics();           
            
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            
            g.drawImage(originalImage, 0, 0, resizedWidth.intValue(), resizedHeight.intValue(), null);
            g.dispose();

            return resizedImage;
        } catch (IOException ex) {
            return null;
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
