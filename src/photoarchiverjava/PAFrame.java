/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package photoarchiverjava;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author adam
 */
public class PAFrame extends JFrame {

    public PAFrame() {
        JComboBox hGap;
        JComboBox vGap;
        
        //make sure the program exits when the frame closes
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("FlowLayout Example");
        this.setSize(700,300);
      
        //This will center the JFrame in the middle of the screen
        this.setLocationRelativeTo(null);
        
        JPanel optionPanel = new JPanel();
        
        JButton changeLayout = new JButton("Change Layout");
        changeLayout.setActionCommand("Change Layout");
        
        JFrame frame = this;
        changeLayout.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent event)
            {
               //When the "Change Layout" button is pressed
               //add a JPanel with the chosen BorderLayout settings
               AddJPanel(frame,(Integer)hGap.getSelectedItem(),
                    (Integer)vGap.getSelectedItem());

            }
        });
        
        Integer[] options = {0,5,10,15,20,30};
        hGap = new JComboBox(options);
        hGap.setSelectedIndex(0);
        
        vGap = new JComboBox(options);
        vGap.setSelectedIndex(0);
        
        JLabel horizontalGap = new JLabel("Horizontal Gap:");
        JLabel verticalGap = new JLabel("Vertical Gap:");
        
        optionPanel.add(horizontalGap);
        optionPanel.add(hGap);
        optionPanel.add(verticalGap);
        optionPanel.add(vGap);
        optionPanel.add(changeLayout);
        this.add(optionPanel, BorderLayout.NORTH);
 
        this.setVisible(true);
    }
}
