/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package photoarchiverjava;

/**
 *
 * @author adam_fejes_dell
 */
public class Counter {

    private static int moved = 0;
    private static int missingData = 0;
    private static int fail = 0;
    private static int all = 0;
    
    public static void incMoved(){
        ++moved;
    }

    public static void incFail(){
        ++fail;
    }
    
    public static void incMissingData(){
        ++missingData;
    }
    
    public static int getMoved() {
        return moved;
    }

    public static int getFail() {
        return fail;
    }
    
    public static int getMissingData(){
        return missingData;
    }

    public static int getAll() {
        return all;
    }

    public static void setAll(int all) {
        Counter.all = all;
    }
    
    
    
}
