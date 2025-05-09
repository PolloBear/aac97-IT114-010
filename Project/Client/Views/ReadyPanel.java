package Project.Client.Views;

import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JPanel;

import Project.Client.Client;

public class ReadyPanel extends JPanel {
    public ReadyPanel() {
        JButton readyButton = new JButton("Ready");
        readyButton.addActionListener(event -> {
            try {
                Client.INSTANCE.sendReady();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        
        JButton toggleExtendedModeButton = new JButton("Toggle Extended Mode");
        toggleExtendedModeButton.addActionListener(event -> {
            try {
                Client.INSTANCE.sendMessage("/toggle-extended");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        this.add(readyButton);
        this.add(toggleExtendedModeButton);
    
        JButton cooldownToggle = new JButton("Enable Cooldown");
            cooldownToggle.addItemListener(e -> {
            boolean isCooldownEnabled = cooldownToggle.isSelected();
            try {
            Client.INSTANCE.sendMessage("/cooldown " + isCooldownEnabled);
            } catch (IOException ex) {
                ex.printStackTrace();
             }
        });
    this.add(cooldownToggle);
    
    
    }

    


    
}
