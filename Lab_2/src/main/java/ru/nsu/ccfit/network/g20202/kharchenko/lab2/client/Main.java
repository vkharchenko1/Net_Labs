package ru.nsu.ccfit.network.g20202.kharchenko.lab2.client;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class Main {

    private final static String filePath = "C:\\Users\\vladi\\Documents\\Net_labs\\lab2\\client_files\\cyberpunk.mp4";
    private final static String fileName = "cyberpunk.mp4";
    private final static String serverIP = "127.0.0.1";
    private final static int serverPort = 63000;

    public static void main(String[] args) {
        try {
            //Open socket
            Socket socket = new Socket(serverIP, serverPort);

            //Open socket streams
            DataOutputStream socketWriter = new DataOutputStream(socket.getOutputStream());
            DataInputStream socketReader = new DataInputStream(socket.getInputStream());

            //Open file
            FileInputStream fileReader = new FileInputStream(filePath);

            //Send name, size
            socketWriter.writeUTF(fileName);
            long fileSize = Files.size(Path.of(filePath));
            socketWriter.writeLong(fileSize);

            //Keep read/writing while len > 0
            byte[] buffer = new byte[4096];
            int len;
            while (fileReader.available() > 0) {
                len = fileReader.read(buffer, 0, buffer.length);
                socketWriter.write(buffer, 0, len);
            }

            //Get success of copy procedure
            boolean copySuccess = socketReader.readBoolean();
            if (copySuccess) {
                System.out.println("Copy was successful");
            } else {
                System.out.println("Copy failed");
            }

            //Close socket and file stream
            socket.close();
            fileReader.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}