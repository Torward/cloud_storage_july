import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ChatHandler implements Runnable {

    private String root = "server/serverFiles";
    private Socket socket;
    private byte[] buffer;
    private DataInputStream din;
    private DataOutputStream dout;

    public ChatHandler(Socket socket) {
        this.socket = socket;
        buffer = new byte[256];
    }

    @Override
    public void run() {
        try {
            din = new DataInputStream(socket.getInputStream());
            dout = new DataOutputStream(socket.getOutputStream());
            while (true) {
                processFileMessage();

            }

        } catch (Exception e) {
            System.err.println("Client connection exception");
        } finally {
            try {
                din.close();
                dout.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    public void processFileMessage() throws IOException {
        String fileName = din.readUTF();
        System.out.println("Received fileName: " + fileName);
        long size = din.readLong();
        System.out.println("Received fileSize: " + size);
        try (FileOutputStream fos = new FileOutputStream(root + "/" + fileName, true)) {
            for (int i = 0; i < (size + 255) / 256; i++) {
                int read = din.read(buffer);
                fos.write(buffer, 0, read);
            }
        }catch (Exception e){
            System.err.println("File write exception!");
            e.printStackTrace();
        }
        dout.writeUTF("File " + fileName + " received!");
    }
}
