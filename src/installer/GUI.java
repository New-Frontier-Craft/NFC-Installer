package installer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class GUI {
	
	public static String version;
	public JButton install;
	public static JTextArea text;
	private String installerVersion = "1.5";
	public static JFrame frame;
	public static final String NFCTXTDownload = "http://newfrontiercraft.altervista.org/wp-content/uploads/nfc.txt";
	public static final String versionDownload = "http://newfrontiercraft.altervista.org/wp-content/uploads/version.txt";
	
	public GUI() throws IOException{
		frame = new JFrame("New Frontier Craft Installer");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(640,420);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		
        int s = 64;
        final int[] sizes = new int[s];

        for (int ii=0; ii<sizes.length; ii++) {
            sizes[ii] = 16+(ii*2);
        }

        ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
        Vector<ImageIcon> icons = new Vector<ImageIcon>();
        BufferedImage image = null;

        URL resource = frame.getClass().getResource("/logo.png");
        if (resource != null) {
            try {
                image = ImageIO.read(resource);
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            for (int ii=0; ii< sizes.length; ii++) {
                BufferedImage bi = FrameIconList.getImage(
                        sizes[ii], image);
                images.add(bi);
                ImageIcon imi = new ImageIcon(bi);
                icons.add(imi);
            }
            
            frame.setIconImages(images);
        } else {
        	System.err.println("Couldn't find 'logo.png', falling back to default Java logo");
        }
		
		JPanel p = new JPanel();
		p.setBackground(Color.BLACK);
		frame.getContentPane().add(p);
		ImageIcon banner = new ImageIcon(this.getClass().getClassLoader().getResource("banner.png"));
		JLabel label = new JLabel("", banner, JLabel.CENTER);
		p.add(label);
		text = new JTextArea();
		text.setEditable(false);
		text.setLineWrap(true);
		text.setWrapStyleWord(true);
		text.setPreferredSize(new Dimension(600, 250));
		JScrollPane scroll = new JScrollPane(text);
		p.add(scroll);
		install = new JButton(" Install ");
		install.setEnabled(false);
		p.add(install);
		frame.setVisible(true);
		addText("New Frontier Craft Universal Installer " + installerVersion);
		getVersion();
	}
	
	public static void addText(String string){
		text.setText(text.getText() + "\n " + string);
	}
	
	public static void attemptInstall(){
		try {
			new ThreadInstall(version).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void getVersion(){
		try{
		Files.createDirectories(Paths.get(ThreadInstall.getAppDir().toPath() + "/NFCinstaller"));
		URL url = new URL(versionDownload);
		try (InputStream stream = url.openStream()) {
		    Files.copy(stream, Paths.get(ThreadInstall.getAppDir().toPath() + "/NFCinstaller/version.txt"), StandardCopyOption.REPLACE_EXISTING);
		    stream.close();
		}
		FileInputStream downloc = new FileInputStream(ThreadInstall.getAppDir().toPath() + "/NFCinstaller/version.txt");
		Scanner scnr = new Scanner(downloc);
		version = scnr.nextLine();
		scnr.close();
		addText("Version Detected: " + version);
		addText("Would you like to install?");
		install.setEnabled(true);
		install.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				install.setEnabled(false);
				attemptInstall();
			}
			
		});
		}
		catch (IOException e) {
			addText(e.toString());
			addText("Could not detect version!");
		}
	}
	
}
