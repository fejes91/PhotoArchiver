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
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

public class PAFrame extends JFrame {

    private JPanel buttonPanel, statContainerPanel, inboxPanel, vaultPanel, imagesPanel;
    private JScrollPane vaultInfoScroll;
    private JFileChooser dirChooser;
    private JButton setVault, setInbox, archive;
    private JLabel vaultPath, inboxPath, progressLabel;
    private JTextArea vaultInfo, inboxInfo;

    
    public PAFrame(final PALogic logic) {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("PhotoArchiver 0.2");
        this.setSize(900, 600);
        this.setLocationRelativeTo(null);

        dirChooser = new JFileChooser(new File("."));
        dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        buttonPanel = new JPanel();
        buttonPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        statContainerPanel = new JPanel(new BorderLayout());

        inboxPanel = new JPanel();
        inboxPanel.setPreferredSize(new Dimension(450, 450));
        vaultPanel = new JPanel();
        vaultPanel.setPreferredSize(new Dimension(450, 450));

        imagesPanel = new JPanel();
        imagesPanel.setBackground(Color.red);
        //imagesPanel.setPreferredSize(new Dimension(450, 415));

        inboxInfo = new JTextArea();
        inboxInfo.setPreferredSize(new Dimension(450, 30));
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
                    //loadPreviews(logic, imagesPanel);
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
        inboxPanel.add(inboxPath);
        inboxPanel.add(inboxInfo);
        inboxPanel.add(imagesPanel);
        vaultPanel.add(vaultPath);

        vaultInfoScroll = new JScrollPane(vaultInfo, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        vaultInfoScroll.setPreferredSize(new Dimension(400, 450));
        vaultPanel.add(vaultInfoScroll);

        statContainerPanel.add(inboxPanel, BorderLayout.WEST);
        statContainerPanel.add(vaultPanel, BorderLayout.EAST);
        statContainerPanel.add(progressLabel, BorderLayout.SOUTH);
        this.add(buttonPanel, BorderLayout.NORTH);
        this.add(statContainerPanel, BorderLayout.CENTER);
        this.setVisible(true);

    }

    private void loadPreviews(PALogic logic, JPanel panel) {
        File[] files = logic.getInbox().listFiles(new ImageFilter());
        for (File f : files) {
            System.out.println("image: " + f.getName());

            BufferedImage img = logic.generateThumbnail(f, 240.0, 160.0);
            panel.add(new JLabel(new ImageIcon(img)));
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
