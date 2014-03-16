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

    private static int success = 0;
    private static int fail = 0;
    
    public static void incSuccess(){
        ++success;
    }

    public static void incFail(){
        ++fail;
    }
    
    public static int getAll(){
        return fail + success;
    }
    
    public static int getSuccess() {
        return success;
    }

    public static int getFail() {
        return fail;
    }
    
}
