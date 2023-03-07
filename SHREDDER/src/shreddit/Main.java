package shreddit;

import java.io.File;
import java.io.FileOutputStream;
import java.security.SecureRandom;
import java.util.*;

/**
 * Program takes a user supplied directory and overwrites and shreds, the files within
 * @author saucecan
 * Not for misuse.
 */
public class Main {

    /**
     * Main method for shredding files within a directory
     * @param args given from the command line, thrown
     */
    public static void main(String[] args) {

        try (Scanner reader = new Scanner(System.in)) {
            while (true) {
                // Get directory from user
                System.out.print("\nEnter a directory path: ");
                String usrDirectory = reader.nextLine();

                if (usrDirectory.startsWith("/")) {
                    // Creating a file object for the directory path
                    File directory = new File(usrDirectory);
                    if (!directory.exists()) {
                        System.err.println("\nGiven directory was not found.");
                        continue;
                    } else {
                        // Getting a list of all files within the directory
                        File[] files = directory.listFiles();
                        // Print the name of each file in the directory
                        System.out.println("\nThese files will be shredded:");
                        for (File file: files) {
                            System.out.println(file.getName());
                        }
                        // Get user confirmation
                        boolean ans = confirm(reader, usrDirectory);
                        if (ans == true) {
                            // User has confirmed continuation
                            System.out.println();
                            int cout = 0;
                            for (File file: files) {
                                // Skipping directories, shredding files
                                if (file.isDirectory()) {
                                    cout++;
                                    System.out.println(String.format("%d/%d \t Omitting directory %s", cout, files.length, file));
                                } else {
                                    cout++;
                                    System.out.println(String.format("%d/%d \t Shredding %s", cout, files.length, file));
                                    shreddit(file);
                                }
                            }
                            System.out.println("\nShredding complete.");
                            break;
                        } else if (ans == false) {
                            // User has denied continuation
                            continue;
                        }
                    }
                } else {
                    System.err.println("\nDirectory was not specified in the correct format\nOnly absolute paths are permitted.");
                }
            }
        }
    }

    /**
     * Method makes a user choose "yes" or "no" to determine the course of the program
     * @param reader is a Scanner object used to read user input
     * @param usrDirectory is the initial directory chosen by the user 
     * @return true if the user confirmed
     * @return false if the user denied
     */
    public static boolean confirm(Scanner reader, String usrDirectory) {
        while (true) {
            // Confirm that the chosen path should be used
            System.out.print(String.format("\nAre you sure you like to use the path \"%s\"?: ", usrDirectory));
            String confirm = reader.nextLine();
            // If yes, do this
            if (confirm.toLowerCase().equals("y") || confirm.toLowerCase().equals("yes")) {
                // Return true to start shredding
                return true;
                // If no, break the confirm loop
            } else if (confirm.toLowerCase().equals("n") || confirm.toLowerCase().equals("no")) {
                return false;
                // Else the user did not supply valid input
            } else {
                System.err.println("Please answer \"yes\" or \"no\"");
                continue;
            }
        }
    }

    /**
     * Method takes file, renames it to a random name, then rewrites all data with random data
     * @param file is the file we are shredding
     */
    public static void shreddit(File file) {
        // Rename file
        File newFile = new File(file.getParent(), "tmp_" + UUID.randomUUID().toString());
        if (file.renameTo(newFile)) {
            file = newFile;
        } else {
            System.err.println("The file could not be renamed");
        }
        // Get length of contents
        long fileLen = file.length();
        // Overwrite file 3 times
        for (int i = 0; i < 3; i++) {
            // Make random byte stream
            SecureRandom random = new SecureRandom();
            byte[] data = new byte[(int) fileLen];
            random.nextBytes(data);
            try {
                java.nio.file.Files.write(file.toPath(), data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        zeroit(file);
        // Delete the file or give error
        if (!file.delete()) {
            System.err.println("Failed to delete file");
            return;
        }
    }

    /**
     * Method Zeros the file, the true shred method
     * @param file is the file to be shredded
     */
    public static void zeroit(File file) {

        try (FileOutputStream out = new FileOutputStream(file);) {
            // Get length of file data
            long length = file.length();
            // Create a new byte buffer thats all zeros
            byte[] buffer = new byte[1024];
            Arrays.fill(buffer, (byte) 0);
            // Start overwriting
            while (length > 0) {
                int bytesToWrite = (int) Math.min(length, buffer.length);
                out.write(buffer, 0, bytesToWrite);
                length -= bytesToWrite;
            }
        } catch (Exception e) {
            System.out.println("Unable to open file for zeroing.");
            e.printStackTrace();
        }
    }
}
