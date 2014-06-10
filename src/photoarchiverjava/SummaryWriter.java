/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package photoarchiverjava;

import java.io.PrintStream;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author adam
 */
public class SummaryWriter extends Thread {

    private Queue q;
    private PrintStream stream;
    private boolean isArchiving;

    public SummaryWriter(Queue q, PrintStream stream) {
        this.q = q;
        this.stream = stream;
        
        isArchiving = true;
    }

    @Override
    public void run() {
        System.out.println("Summary writer running...");
        while (isArchiving || !q.isEmpty()) {
            if(!q.isEmpty()){
                stream.println(q.poll());
            }
        }
    }

    public void stopWriting() {
        this.isArchiving = false;
    }
    
    
}
