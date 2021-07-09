

import java.io.*;
import java.net.Socket;

public class ChatHandler implements Runnable {


    private String root = "client/clientFiles";
    private String sroot = "server/serverFiles";
    private Socket socket;
    private byte[] buffer;
    private DataInputStream din;
    private DataOutputStream dout;

    public ChatHandler(Socket socket) {
        this.socket = socket;
        buffer = new byte[8 * 1024];
    }

    @Override
    public void run() {
        try {
            din = new DataInputStream(socket.getInputStream());
            dout = new DataOutputStream(socket.getOutputStream());
            while (true) {
                String command = din.readUTF();
                if ("upload".equals(command)) {
                    uploadFileMessage();
                }
                if ("download".equals(command)) {
                    downloadFileMessage();
                }

            }

        } catch (Exception e) {
            System.err.printf("Client %s disconnected\n", socket.getInetAddress());
        } finally {
            try {
                din.close();
                dout.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    private void downloadFileMessage() throws IOException {
        String fileName = din.readUTF();
        System.out.println("Send out fileName: " + fileName);
        long size = din.readLong();
        System.out.println("Send out fileSize: " + size);
        try (FileOutputStream fos = new FileOutputStream(root + "/" + fileName, true)) {
            for (int i = 0; i < (size + (8 * 1024 - 1)) / buffer.length; i++) {
                int read = din.read(buffer);
                fos.write(buffer, 0, read);
            }
        } catch (Exception e) {
            System.err.println("File write exception!");
            e.printStackTrace();
        }
        dout.writeUTF("File " + fileName + " downloaded!");
    }

    public void uploadFileMessage() throws IOException {
        String fileName = din.readUTF();
        System.out.println("Received fileName: " + fileName);
        long size = din.readLong();
        System.out.println("Received fileSize: " + size);
        try (FileOutputStream fos = new FileOutputStream(sroot + "/" + fileName, true)) {
            for (int i = 0; i < (size + (8 * 1024 - 1)) / buffer.length; i++) {
                int read = din.read(buffer);
                fos.write(buffer, 0, read);
            }
        } catch (Exception e) {
            System.err.println("File write exception!");
            e.printStackTrace();
        }
        dout.writeUTF("File " + fileName + " received!");
    }

}
