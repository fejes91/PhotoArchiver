/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package photoarchiverjava;

import java.io.File;
import java.io.FileFilter;

/**
 *
 * @author adam
 */
public class ImageFilter implements FileFilter{

    @Override
    public boolean accept(File file) {
        return file.getName().toLowerCase().endsWith("jpg");
    }
    
}
