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

    public static void main(String[] args) throws ImageProcessingException, IOException {

            File jpegFile = new File("C:\\Users\\adam_fejes_dell\\Desktop\\Archiver Inbox\\asd.jpg");
            //File jpegFile = new File("D:\\Fotók\\Dettivel\\detti_morci.jpg");
            //File jpegFile = new File("D:\\Fotók\\Éjszakai\\Wekerle\\Cserkészbál - február 26, 2014.dng");
            Metadata metadata = ImageMetadataReader.readMetadata(jpegFile);
            System.out.println(metadata.getDirectoryCount());
            
            for (Directory directory : metadata.getDirectories()) {
            for (Tag tag : directory.getTags()) {
                System.out.println(tag);
            }
        }

    }

}