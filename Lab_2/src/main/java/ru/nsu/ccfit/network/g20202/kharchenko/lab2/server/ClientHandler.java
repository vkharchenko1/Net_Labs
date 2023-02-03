package ru.nsu.ccfit.network.g20202.kharchenko.lab2.server;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This thread exchanges info with the client accordingly:
 * <p>1. Receives the name of the file to be uploaded through readUTF()
 * <p>2. Receives the size of the file to be uploaded through readInt()
 * <p>3. Receives byte data up to fileSize bytes, which is defined in step 2, or until it encounters EOF
 * <p>4. It compares the received number of bytes to the fileSize parameter and sends
 *         the success state to client through writeBoolean()
 */

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final String dirPath = "C:\\Users\\vladi\\Documents\\Net_labs\\lab2\\uploads\\";
    public ClientHandler(Socket cS) {
        clientSocket = cS;
    }

    @Override
    public void run() {
        try {
            //Open socket streams
            DataOutputStream socketWriter = new DataOutputStream(clientSocket.getOutputStream());
            DataInputStream socketReader = new DataInputStream(clientSocket.getInputStream());

            //Accept name, size
            String fileName = socketReader.readUTF();
            long fileSize = socketReader.readLong();

            //Create file and open stream
            String filePath = dirPath + fileName;
            File file = new File(filePath);
            while (!file.createNewFile()) {
                filePath = filePath.substring(0,(filePath.lastIndexOf('.'))) + "_new" + filePath.substring((filePath.lastIndexOf('.')));
                file = new File(filePath);
            }
            FileOutputStream fileWriter = new FileOutputStream(file);

            //Read/writing utilities
            byte[] buffer = new byte[4096];
            int len;
            long newFileSize = 0;

            //Start speed measuring
            SpeedMeter speedMeter = new SpeedMeter();
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(speedMeter, 3000L, 3000L);

            //Keep read/writing until read == 0
            while (newFileSize < fileSize && (len = socketReader.read(buffer, 0, buffer.length)) != -1) {
                fileWriter.write(buffer, 0, len);
                newFileSize += len;
                speedMeter.addReadCount(len);
            }
            timer.cancel();
            speedMeter.run();

            //Send success state
            boolean copySuccess = (newFileSize == fileSize);
            socketWriter.writeBoolean(copySuccess);

            //Close file stream and socket
            clientSocket.close();
            fileWriter.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
