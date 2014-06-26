/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package photoarchiverjava;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URLEncoder;
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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.codec.binary.Base64;
/**
 *
 * @author adam
 */
public class PALogic {

    private final String NO_DATA = "FILE HAS NOT METADATA!";
    private File vault;
    private File inbox;
    private final PAFrame frame;
    private SummaryWriter writer;
    private Archiver archiver;
    private Queue<String> summary;
    private PrintStream printStream;


    public PALogic() {
        vault = new File("/home/adam/git/PhotoArchiver/Vault");
        inbox = new File("/home/adam/Desktop/Inbox");
        //inbox = new File("C:\\Users\\fejes_000\\Desktop\\PhotoArchiver Inbox");
        //vault = new File("G:\\Projektek\\PhotoArchiver\\Vault");
        summary = new LinkedList<>();
        frame = new PAFrame(this);

    }

    public void doArchive() {
        archiver = new Archiver(this);
        
        printStream = new PrintStream(new CustomOutputStream(frame.getVaultInfo()));
        writer = new SummaryWriter(summary, printStream);

        writer.start();
        archiver.start();
    }

    private void storeMetadata(String path, String fileName, MyMetaData md) {
        File metaDir = new File(vault.getAbsolutePath() + "/meta");
        metaDir.mkdir();

        String allKeyword = "";
        for (String keyword : md.getKeywords()) {
            allKeyword += keyword + " ";
        }
        allKeyword = allKeyword.trim();
        for (String keyword : md.getKeywords()) {
            try {
                File keywordFile = new File(metaDir.getAbsolutePath() + "/" + keyword.hashCode() + ".js");
                if (keywordFile.createNewFile()) {//ha nem létezett, akkor fel kell venni a listába is
                    File listDir = new File(metaDir.getAbsolutePath() + "/list");
                    listDir.mkdir();
                    File keywordList = new File(metaDir.getAbsolutePath() + "/" + listDir.getName() + "/keywordlist.js");
                    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(keywordList, true)));
                    out.println("keywordlist.push(\"" + keyword + "\");");
                    out.flush();
                    
                    File dictDir = new File(metaDir.getAbsolutePath() + "/list");
                    dictDir.mkdir();
                    File dictFile = new File(metaDir.getAbsolutePath() + "/" + dictDir.getName() + "/" + getDictionaryfileNameForKeyword(keyword) + ".js");
                    out = new PrintWriter(new BufferedWriter(new FileWriter(dictFile, true)));
                    out.println("addToDictionary(\"" + keyword + "\",\"" + keyword.hashCode() + "\");");
                    out.flush();
                }
                PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(keywordFile, true)));
                out.println("addPicture(\"" + path + "/" + fileName + "\", \"" + allKeyword + "\");");
                out.flush();
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
            }
            //System.out.println("insert meta for file " + path + " to keyword file " + keywordFile.getName());

        }

        try {
            File folderDescriptorFile = new File(vault.getAbsolutePath() + "/" + path + "/desc.js");
            folderDescriptorFile.createNewFile();
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(folderDescriptorFile, true)));
            out.println("addPicture(\"" + path + "/" + fileName + "\", \"" + allKeyword + "\");");
            out.flush();
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }


        Counter.incMoved();
                
    }

    public MyMetaData getMetadata(File f) {
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
            printToSummary("\tFile attribute reading error: " + ex.getMessage());
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
        if (keywords.size() == 0) {
            keywords.add("$");
        }

        return new MyMetaData(date, keywords, name, desc);
    }

    public void handleFile(File file, MyMetaData md) {
        //System.out.println("MyMeta: " + myMeta);
        printToSummary("Handling file: " + file.getName());
        vault.mkdir();
        File year = new File(vault.getAbsolutePath() + "/" + md.getDate().get(Calendar.YEAR));
        year.mkdir();
        File month = new File(year.getAbsolutePath() + "/" + String.valueOf(md.getDate().get(Calendar.MONTH) + 1));
        month.mkdir();
        File day = new File(month.getAbsolutePath() + "/" + md.getDate().get(Calendar.DAY_OF_MONTH));
        if (!day.exists()) {
            PrintWriter out = null;
            try {
                File metaDir = new File(vault.getAbsolutePath() + "/meta");
                metaDir.mkdir();
                File dateDir = new File(metaDir.getAbsolutePath() + "/dates");
                dateDir.mkdir();
                File dateList = new File(metaDir.getAbsolutePath() + "/" + dateDir.getName() + "/dateList.js");
                out = new PrintWriter(new BufferedWriter(new FileWriter(dateList, true)));
                out.println("addDate(\"" + md.getDate().get(Calendar.YEAR) + "-" + (md.getDate().get(Calendar.MONTH) + 1) + "-" + md.getDate().get(Calendar.DAY_OF_MONTH) + "\");");
                out.flush();
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                out.close();
            }
        }
        day.mkdir();


        File thumbnails = new File(vault.getAbsolutePath() + "/thumbnails");
        thumbnails.mkdir();
        File tYear = new File(thumbnails.getAbsolutePath() + "/" + md.getDate().get(Calendar.YEAR));
        tYear.mkdir();
        File tMonth = new File(tYear.getAbsolutePath() + "/" + String.valueOf(md.getDate().get(Calendar.MONTH) + 1));
        tMonth.mkdir();
        File tDay = new File(tMonth.getAbsolutePath() + "/" + md.getDate().get(Calendar.DAY_OF_MONTH));
        tDay.mkdir();

        try {
            //Files.move(Paths.get(file.getAbsolutePath()), Paths.get(day.getAbsolutePath() + "/" + file.getName()));
            Files.copy(Paths.get(file.getAbsolutePath()), Paths.get(day.getAbsolutePath() + "/" + file.getName()));

            BufferedImage thumbnail = generateThumbnail(file);
            ImageIO.write(thumbnail, "jpg", new File(tDay.getAbsolutePath() + "/" + file.getName()));

            storeMetadata(year.getName() + "/" + month.getName() + "/" + day.getName(), file.getName(), md);
        } catch (FileAlreadyExistsException ex) {
            Counter.incFail();
            printToSummary("\tFile already exists: " + ex.getMessage());
        } catch (IOException ex) {
            Counter.incFail();
            System.err.println("File moving error: " + ex.getMessage());
        }
    }

    private BufferedImage generateThumbnail(File image) {
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

    private String getAllMetadataAsString(File f) {
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
    
    private String getDictionaryfileNameForKeyword(String keyword){
        String first = String.valueOf(keyword.charAt(0)).toLowerCase();
        if(first.equals("ö")){
            return "o";
        }
        else if(first.equals("ü")){
            return "u";
        }
        else if(first.equals("ó")){
            return "o";
        }
        else if(first.equals("ő")){
            return "o";
        }
        else if(first.equals("ú")){
            return "u";
        }
        else if(first.equals("é")){
            return "e";
        }
        else if(first.equals("á")){
            return "a";
        }
        else if(first.equals("ű")){
            return "u";
        }
        else if(first.equals("í")){
            return "i";
        }

        return first;
    }
    
    public int getInboxFileCount(){
        if(inbox != null && inbox.listFiles() != null){
            return inbox.listFiles().length;
        }
        
        return 0;
    }
    
    public void printToSummary(String s){
        summary.add(s);
    }

    public File getVault() {
        return vault;
    }

    public void setVault(File vault) {
        this.vault = vault;
    }

    public File getInbox() {
        return inbox;
    }

    public void setInbox(File inbox) {
        this.inbox = inbox;
    }

    public SummaryWriter getWriter() {
        return writer;
    }

    public void setWriter(SummaryWriter writer) {
        this.writer = writer;
    }

    public PAFrame getFrame() {
        return frame;
    }
        
}
