package Editor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class Editor extends JFrame{

    private final ETextArea area;
    private final int startWindowSize = 600;

    private Editor() throws HeadlessException {
        super("Editor");
        setFocusTraversalKeysEnabled(false);
        setLayout(new BorderLayout());

        JScrollBar scrollBar = new JScrollBar(Adjustable.VERTICAL);
        add(scrollBar, BorderLayout.EAST);

        area = new ETextArea(scrollBar);
        scrollBar.addAdjustmentListener(area.getAdjustmentListener());

        add(area);

        JMenuBar menu = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem openItem = new JMenuItem("Open");
        fileMenu.add(openItem);
        openItem.addActionListener(new openFile());
        JMenuItem saveItem = new JMenuItem("Save as");
        saveItem.addActionListener(new saveFile());
        fileMenu.add(saveItem);

        menu.add(fileMenu);
        setJMenuBar(menu);
        
        setSize(startWindowSize, startWindowSize);
        setLocationRelativeTo(null);

        addKeyListener(area.getKeyListener());

        setVisible(true);
    }

    public static void main(String[] args) {
        new Editor();
    }

    private class openFile implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            int ret = fileChooser.showDialog(null, "Open file");

            if (ret == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try {
                    area.setNewDocument(Files.readAllLines(file.toPath()));
                    area.setFileName(file.getName(), true);
                } catch (IOException exception) {
                    System.out.println("Problem with opening file");
                    exception.printStackTrace();
                }
            }
        }
    }

    private class saveFile implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            int ret = fileChooser.showDialog(null, "Save file as");

            if (ret == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try {
                    if (!file.exists()) {
                        file.createNewFile();
                    }

                    Files.write(file.toPath(), area.getLines(), StandardOpenOption.WRITE);
                    area.setFileName(file.getName(), false);
                } catch (IOException exception) {
                    System.out.println("Problem with saving file");
                    exception.printStackTrace();
                }
            }
        }
    }
}