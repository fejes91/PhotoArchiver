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
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class PAFrame extends JFrame {

    private JPanel buttonPanel, inboxPanel, imagesPanel, editorPanel; 
    //private JPanel vaultPanel, containerPanel;
    private JScrollPane vaultInfoScroll;
    private JScrollPane imageGridScroll;
    private JFileChooser dirChooser;
    private JButton setVault, setInbox, archive;
    private JLabel vaultPath, inboxPath, progressLabel;
    private JTextArea vaultInfo, inboxInfo;

    
    public PAFrame(final PALogic logic) {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("PhotoArchiver 0.2");
        this.setSize(1800, 950);
        this.setLocationRelativeTo(null);

        dirChooser = new JFileChooser(new File("."));
        dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        inboxPanel = new JPanel(new BorderLayout());
        //inboxPanel.setPreferredSize(new Dimension(450, 450));
        inboxPanel.setBackground(Color.yellow);
        
        buttonPanel = new JPanel();
        //buttonPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        buttonPanel.setBackground(Color.MAGENTA);
//        containerPanel = new JPanel(new BorderLayout());
//        containerPanel.setBackground(Color.green);
        
        editorPanel = new JPanel();
        editorPanel.setPreferredSize(new Dimension((int)(this.getWidth() * 0.2), inboxPanel.getHeight()));
        editorPanel.setBackground(Color.ORANGE);

        
//        vaultPanel = new JPanel();
//        vaultPanel.setPreferredSize(new Dimension(450, 450));
//        vaultPanel.setBackground(Color.CYAN);

        imagesPanel = new JPanel(new WrapLayout());
        imagesPanel.setBackground(Color.red);
        //imagesPanel.setPreferredSize(new Dimension(450, 415));

        inboxInfo = new JTextArea();
        inboxInfo.setPreferredSize(new Dimension(450, 20));
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
                if (logic.getInbox() != null) {
                    inboxPath.setText("Inbox dir: " + logic.getInbox().getAbsolutePath());
                    inboxInfo.setText("Files in inbox: " + logic.getInboxFileCount());
                    loadPreviews(logic, imagesPanel);
                }
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
                if (logic.getVault() != null) {
                    vaultPath.setText("Vault dir: " + logic.getVault().getAbsolutePath());
                }
            }
        });

        archive = new JButton("Archive!");
        archive.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (logic.getVault() != null && logic.getInbox() != null) {
                    logic.doArchive();
                } else {
                    //TODO error message
                }
            }
        });

        buttonPanel.add(setInbox);
        buttonPanel.add(archive);
        buttonPanel.add(setVault);

        if (logic.getVault() != null) {
            vaultPath.setText("Vault dir: " + logic.getVault().getAbsolutePath());
        }
        if (logic.getInbox() != null) {
            inboxPath.setText("Inbox dir: " + logic.getInbox().getAbsolutePath());
        }
        //inboxPanel.add(inboxPath, BorderLayout.NORTH);
        inboxPanel.add(inboxInfo, BorderLayout.NORTH);
        inboxPanel.add(editorPanel, BorderLayout.EAST);
        imageGridScroll = new JScrollPane(imagesPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        imageGridScroll.setPreferredSize(new Dimension((int)(this.getWidth() * 0.8), inboxPanel.getHeight()));
        imageGridScroll.getVerticalScrollBar().setUnitIncrement(16);
        inboxPanel.add(imageGridScroll, BorderLayout.WEST);
        inboxPanel.add(progressLabel, BorderLayout.SOUTH);
        //vaultPanel.add(vaultPath);

        vaultInfoScroll = new JScrollPane(vaultInfo, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        vaultInfoScroll.setPreferredSize(new Dimension(400, 450));
        //vaultPanel.add(vaultInfoScroll);

        //containerPanel.add(inboxPanel, BorderLayout.NORTH);
        //statContainerPanel.add(vaultPanel, BorderLayout.EAST);
        
        
        
        this.add(buttonPanel, BorderLayout.NORTH);
        this.add(inboxPanel, BorderLayout.CENTER);
        this.setVisible(true);

    }

    private void loadPreviews(PALogic logic, JPanel panel) {
        File[] files = logic.getInbox().listFiles(new ImageFilter());
        panel.removeAll();
        
        int numberOfThreads = 3;
        System.out.println("Number of threads: " + numberOfThreads);
        for (int i = 0;  i < numberOfThreads; ++i) {
            ThumbnailGeneratorThread generator = new ThumbnailGeneratorThread(logic, files, i + 1, panel);
            generator.start();
        }
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
