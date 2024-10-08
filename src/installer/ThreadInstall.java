package installer;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ThreadInstall extends Thread {
	
	private static final int BUFFER_SIZE = 4096;
	ArrayList<String> installFiles;
	String version;
	String dirString;
	
	public ThreadInstall(String version) throws Exception{
		this.version = version;
	}
	
	public void run(){
		try {
			GUI.addText("Installing!");
			File dir = getAppDir();
			dirString = dir.getPath().replace("\\", "/");
			installFiles = new ArrayList<String>();
			if(Files.exists(Paths.get(dirString + "/NFCinstaller/Extract")))
			listEverything(new File(dirString + "/NFCinstaller/Extract"));
			deleteOldExtract();
			downloadNFC();
			unZip(dirString + "/NFCinstaller/NFC " + version + ".zip/", dirString +"/NFCinstaller/Extract");
			installFiles = new ArrayList<String>();
			listFilesForFolder(new File(dirString + "/NFCinstaller/Extract/For Beta 1.7.3 Jar"), dirString + "/NFCinstaller/Extract/For Beta 1.7.3 Jar", true);
			moveAndRenameJar(dirString);
			deleteMETAINF(dirString + "/versions/NFC " + version + "/NFC " + version + ".jar");
			inputToBetaJar(dirString + "/versions/NFC " + version + "/NFC " + version + ".jar");
			installFiles = new ArrayList<String>();
			listFilesForFolder(new File(dirString + "/NFCinstaller/Extract/For Game Dir"), dirString + "/NFCinstaller/Extract/For Game Dir", true);
			inputToGameDir(dirString);
			GUI.addText("Install successful! You can now close out of the installer.");
		} catch (Exception e) {
			GUI.addText(e.toString());
			GUI.addText("Install failed.");
		}
	}
	
	public void deleteOldExtract() throws Exception{
		GUI.addText("Deleting any old extract files.");
		try {
			for(int i = 0; i < installFiles.size(); i++){
				Files.delete(Paths.get(installFiles.get(i)));
			}
		} catch (Exception e) {
			GUI.addText(e.toString());
			GUI.addText("ERROR: Extract files detected, but could not delete!");
			throw new Exception("Failed to install.");
		}
	}
	
	public void downloadNFC() throws Exception{
		GUI.addText("Downloading New Frontier Craft. This may take a minute or two.");
		try{
		URL url = new URL(GUI.NFCTXTDownload);
		try (InputStream stream = url.openStream()) {
		    Files.copy(stream, Paths.get(dirString + "/NFCinstaller/nfc.txt"), StandardCopyOption.REPLACE_EXISTING);
		    stream.close();
		}
		FileInputStream downloc = new FileInputStream(dirString + "/NFCinstaller/nfc.txt");
		Scanner scnr = new Scanner(downloc);
		url = new URL(scnr.nextLine());
		try (InputStream stream = url.openStream()) {
		    Files.copy(stream, Paths.get(dirString + "/NFCinstaller/NFC " + version + ".zip"), StandardCopyOption.REPLACE_EXISTING);
		    stream.close();
		}
		scnr.close();
		downloc.close();
		} catch (Exception e) {
			GUI.addText(e.toString());
			GUI.addText("ERROR: Could not download New Frontier Craft!");
			throw new Exception("Failed to install.");
		}
	}
	
	public void deleteMETAINF(String path) throws Exception{ 
		GUI.addText("Deleting META-INF from Beta jar.");
		try{
        Map<String, String> zip_properties = new HashMap<>(); 
        zip_properties.put("create", "false"); 
        URI zip_disk = URI.create("jar:" + Paths.get(path).toUri().toString());
        try (FileSystem zipfs = FileSystems.newFileSystem(zip_disk, zip_properties)) {
            Path pathInZipfile = zipfs.getPath("META-INF/MANIFEST.MF");
            Files.deleteIfExists(pathInZipfile);
            
            pathInZipfile = zipfs.getPath("META-INF/MOJANG_C.DSA");
            Files.deleteIfExists(pathInZipfile);
            
            pathInZipfile = zipfs.getPath("META-INF/MOJANG_C.SF");
            Files.deleteIfExists(pathInZipfile);
            
            pathInZipfile = zipfs.getPath("META-INF/");
            Files.deleteIfExists(pathInZipfile);
        } 
		} catch (Exception e) {
			e.printStackTrace();
			GUI.addText(e.toString());
			throw new Exception("Failed to install.");
		}
    }
	
	public void listFilesForFolder(final File folder, String originalPath, boolean addSubFile) {
	    for (final File fileEntry : folder.listFiles()) {
	        if (fileEntry.isDirectory()) {
	            listFilesForFolder(fileEntry, originalPath, addSubFile);
	        } else {
	        	if(addSubFile)installFiles.add(fileEntry.getPath().substring(originalPath.length()));
	            installFiles.add(fileEntry.getPath());
	        }
	    }
	}
	
	public void listEverything(final File folder){
		File[] list = folder.listFiles();
		if(list != null && list.length > 0){
			for(int i = 0; i < list.length; i++){
				listEverything(list[i]);
			}
		}
		else
		installFiles.add(folder.getPath());
	}
	
	public void inputToBetaJar(String path)throws Exception{
		GUI.addText("Installing NFC into Beta Jar");
		try {
		/* Define ZIP File System Properies in HashMap */    
        Map<String, String> zip_properties = new HashMap<>();
        /* We want to read an existing ZIP File, so we set this to False */
        zip_properties.put("create", "false");
        /* Specify the encoding as UTF -8 */
        zip_properties.put("encoding", "UTF-8");
        /* Specify the path to the ZIP File that you want to read as a File System */
        URI zip_disk = URI.create("jar:" + Paths.get(path).toUri().toString());
        /* Create ZIP file System */
        try (FileSystem zipfs = FileSystems.newFileSystem(zip_disk, zip_properties)) {
			for(int i = 0; i < installFiles.size(); i+= 2){
             /* Create a Path in ZIP File */
            Path ZipFilePath = zipfs.getPath(installFiles.get(i));
            /* Path where the file to be added resides */
            Path addNewFile = Paths.get(installFiles.get(i+1));  
            /* Append file to ZIP File */
            Files.createDirectories(ZipFilePath.getParent());
            Files.copy(addNewFile,ZipFilePath, StandardCopyOption.REPLACE_EXISTING); 
			}
        } 
		} catch (Exception e) {
			GUI.addText(e.toString());
			GUI.addText("ERROR: Could not install NFC into Beta jar.");
			throw new Exception("Failed to install.");
		}
	}
	
	public void inputToGameDir(String path)throws Exception{
		GUI.addText("Copying files to game directory.");
		try {
		for(int i = 0; i < installFiles.size(); i+= 2){
        Path gameDirPath = Paths.get(path + installFiles.get(i));
        Path fileBeingCopied = Paths.get(installFiles.get(i+1));  
        Files.createDirectories(gameDirPath.getParent());
        Files.copy(fileBeingCopied,gameDirPath, StandardCopyOption.REPLACE_EXISTING); 
		}
		} catch(Exception e) {
			GUI.addText(e.toString());
			GUI.addText("ERROR: Could not copy files to directory!");
			throw new Exception("Failed to install.");
		}
	}
	
	public void unZip(String zipFilePath, String destDirectory)throws Exception{
		GUI.addText("Extracting New Frontier Craft.");
		try {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry entry = zipIn.getNextEntry();
        // iterates over entries in the zip file
        while (entry != null) {
            String filePath = destDirectory + File.separator + entry.getName();
            if (!entry.isDirectory()) {
                // if the entry is a file, extracts it
                extractFile(zipIn, filePath);
            } else {
                // if the entry is a directory, make the directory
                File dir = new File(filePath);
                dir.mkdir();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
		}
		catch (Exception e) {
			GUI.addText(e.toString());
			GUI.addText("ERROR: Could extract New Frontier Craft!");
			throw new Exception("Failed to install.");
		}
    }
	
	 private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
	        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
	        byte[] bytesIn = new byte[BUFFER_SIZE];
	        int read = 0;
	        while ((read = zipIn.read(bytesIn)) != -1) {
	            bos.write(bytesIn, 0, read);
	        }
	        bos.close();
	    }
	 
	 private void moveAndRenameJar(String path)throws Exception{
		 GUI.addText("Moving and renaming jar.");
		 try {
		 Path betaJar = Paths.get(path + "/versions/b1.7.3/b1.7.3.jar");
		 new File(path + "/versions/NFC " + version).mkdir();
		 Path NFCplace = Paths.get(path + "/versions/NFC " + version + "/NFC " + version + ".jar");
		 Files.copy(betaJar, NFCplace, StandardCopyOption.REPLACE_EXISTING);
		 } catch(Exception e) {
			 GUI.addText(e.toString());
			 GUI.addText("ERROR: Could not find the jar! Please run Beta 1.7.3 on the most recent launcher at least once before installing, and close it.");
			 throw new Exception("Failed to install.");
		 }
	 }

	 
	 public static File getAppDir() {
		 	String s = "minecraft";
			String s1 = System.getProperty("user.home", ".");
			File file;
			switch (EnumOSMappingHelper.enumOSMappingArray[getOs().ordinal()]) {
			case 1: // '\001'
			case 2: // '\002'
				file = new File(s1, (new StringBuilder()).append('.').append(s)
						.append('/').toString());
				break;

			case 3: // '\003'
				String s2 = System.getenv("APPDATA");
				if (s2 != null) {
					file = new File(s2, (new StringBuilder()).append(".").append(s)
							.append('/').toString());
				} else {
					file = new File(s1, (new StringBuilder()).append('.').append(s)
							.append('/').toString());
				}
				break;

			case 4: // '\004'
				file = new File(s1, (new StringBuilder())
						.append("Library/Application Support/").append(s)
						.toString());
				break;

			default:
				file = new File(s1, (new StringBuilder()).append(s).append('/')
						.toString());
				break;
			}
			if (!file.exists() && !file.mkdirs()) {
				throw new RuntimeException((new StringBuilder())
						.append("The working directory could not be created: ")
						.append(file).toString());
			} else {
				return file;
			}
		}

		private static EnumOS2 getOs() {
			String s = System.getProperty("os.name").toLowerCase();
			if (s.contains("win")) {
				return EnumOS2.windows;
			}
			if (s.contains("mac")) {
				return EnumOS2.macos;
			}
			if (s.contains("solaris")) {
				return EnumOS2.solaris;
			}
			if (s.contains("sunos")) {
				return EnumOS2.solaris;
			}
			if (s.contains("linux")) {
				return EnumOS2.linux;
			}
			if (s.contains("unix")) {
				return EnumOS2.linux;
			} else {
				return EnumOS2.unknown;
			}
		}


}
