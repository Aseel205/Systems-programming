package bgu.spl.net.impl.tftp;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class FilesStock {
    // Directory where files will be stored
    private static String DIRECTORY_PATH;


    public FilesStock() {
        File tempFile = new File("Skeleton/client");
        DIRECTORY_PATH =  tempFile.getAbsolutePath();
        deleteFile(tempFile.getName());
    }

    // Create a new file with the specified name
    public void createFile(String fileName) {
        File directory = new File(DIRECTORY_PATH);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File newFile = new File(directory, fileName);
        try {
            newFile.createNewFile() ;
            }
         catch (IOException e) {
        }
    }

    // Check if a file with the specified name exists
    public boolean contains(String fileName) {
        File file = new File(DIRECTORY_PATH, fileName);
        return file.exists();
    }

    // Add data to an existing file
    public void addDataToFile(String fileName, byte[] data) {
        File file = new File(DIRECTORY_PATH, fileName);
        try (FileOutputStream fos = new FileOutputStream(file, true)) {
            fos.write(data);
        } catch (IOException e) {
            System.err.println("Error adding data to file: " + e.getMessage());
        }
    }

    // Delete a file with the specified name
    public void deleteFile(String fileName) {
        File file = new File(DIRECTORY_PATH, fileName);
        file.delete();

    }

    // Get the data from a file as byte array
    public byte[] getFileData(String fileName) {
        byte[] data = null;
        try {
            Path filePath = Paths.get(DIRECTORY_PATH, fileName);
            data = Files.readAllBytes(filePath);
        } catch (IOException e) {
            System.err.println("Error reading file data: " + e.getMessage());
        }
        return data;
    }

    public  byte[] getFilesNames() {
        File directory = new File(DIRECTORY_PATH);
        File[] files = directory.listFiles();
        List<Byte> namesBytes = new ArrayList<>();
        for (File file : files) {
            byte[] fileNameBytes = new byte[0];
            try {
                fileNameBytes = file.getName().getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            for (byte b : fileNameBytes) {
                namesBytes.add(b);
            }
            // Add delimiter byte (0) between each name
            namesBytes.add((byte) 0);
        }
        byte[] result = new byte[namesBytes.size()];
        for (int i = 0; i < namesBytes.size(); i++) {
            result[i] = namesBytes.get(i);
        }
        return result;
    }
}