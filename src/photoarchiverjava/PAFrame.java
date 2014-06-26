/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package photoarchiverjava;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;


public class PAFrame extends JFrame {

    private JPanel buttonPanel, statContainerPanel, inboxPanel, vaultPanel;
    private JScrollPane vaultInfoScroll;
    private JFileChooser dirChooser;
    private JButton setVault, setInbox, archive;
    private JLabel vaultPath, inboxPath, progressLabel;
    private JTextArea vaultInfo, inboxInfo;

    public PAFrame(final PALogic logic) {
        //make sure the program exits when the frame closes
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("PhotoArchiver 0.1");
        this.setSize(900, 600);
        this.setLocationRelativeTo(null);
        
        dirChooser = new JFileChooser(new File("."));
        dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.green);
        buttonPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        statContainerPanel = new JPanel(new BorderLayout());
        statContainerPanel.setBackground(Color.red);
        //statContainerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        inboxPanel = new JPanel();
        inboxPanel.setBackground(Color.yellow);
        inboxPanel.setPreferredSize(new Dimension(450, 450));
        vaultPanel = new JPanel();
        vaultPanel.setBackground(Color.yellow);
        vaultPanel.setPreferredSize(new Dimension(450, 450)); 
        
        inboxInfo = new JTextArea();
        inboxInfo.setPreferredSize(new Dimension(400, 450));
        inboxInfo.setText("Files in inbox: " + logic.getInboxFileCount());
        vaultInfo = new JTextArea();
        vaultInfo.setSize(400, 400);
        vaultInfo.setLineWrap(true);     
        vaultInfo.setFont(new Font("Arial", Font.PLAIN, 10));
        
        progressLabel = new JLabel("0%");
        
        setInbox = new JButton("Set Inbox");
        inboxPath = new JLabel();
        final JFrame parent = this;
        setInbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                dirChooser.setDialogTitle("Choose inbox directory");
                dirChooser.showOpenDialog(parent);
                logic.setInbox(dirChooser.getSelectedFile());
                inboxPath.setText("Inbox dir: " + logic.getInbox().getAbsolutePath());
                
                inboxInfo.setText("Files in inbox: " + logic.getInbox().listFiles().length);
            }
        });
        setVault = new JButton("Set Vault");
        vaultPath = new JLabel();
        setVault.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                dirChooser.setDialogTitle("Choose vault directory");
                dirChooser.showOpenDialog(parent);
                logic.setVault(dirChooser.getSelectedFile());
                vaultPath.setText("Vault dir: " + logic.getVault().getAbsolutePath());
            }
        });
        
        archive = new JButton("Archive!");
        archive.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                logic.doArchive();
            }
        });
        
        buttonPanel.add(setInbox);
        buttonPanel.add(archive);
        buttonPanel.add(setVault);
        
        vaultPath.setText("Vault dir: " + logic.getVault().getAbsolutePath());
        inboxPath.setText("Inbox dir: " + logic.getInbox().getAbsolutePath());
        inboxPanel.add(inboxPath);
        inboxPanel.add(inboxInfo);
        vaultPanel.add(vaultPath);
        
        vaultInfoScroll = new JScrollPane (vaultInfo, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        vaultInfoScroll.setPreferredSize(new Dimension(400, 450));
        vaultPanel.add(vaultInfoScroll);
        
        statContainerPanel.add(inboxPanel, BorderLayout.WEST);
        statContainerPanel.add(vaultPanel, BorderLayout.EAST);
        statContainerPanel.add(progressLabel, BorderLayout.SOUTH);
        this.add(buttonPanel, BorderLayout.NORTH);
        this.add(statContainerPanel, BorderLayout.CENTER);
        this.setVisible(true);

    }

    public JTextArea getVaultInfo() {
        return vaultInfo;
    }

    public JButton getArchive() {
        return archive;
    }

    public JLabel getProgressLabel() {
        return progressLabel;
    }

    
}
