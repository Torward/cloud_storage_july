import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class ChatController implements Initializable {
    private String root = "client/clientFiles";

    private DataInputStream din;
    private DataOutputStream dout;
    private byte[] buffer;

    public ListView<String> list_view;
    public TextField status_bar;

    public void send(ActionEvent actionEvent) throws IOException {
      String fileName = list_view.getSelectionModel().getSelectedItem();
      Path filePath = Paths.get(root,fileName);
       long size =  Files.size(filePath);
        dout.writeUTF(fileName);
        dout.writeLong(size);
        Files.copy(filePath, dout);
        dout.flush();
        status_bar.setText("File: " + fileName + "successfully sent");

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        buffer = new byte[256];
        try {
            File dir = new File(root);
            list_view.getItems().clear();
            list_view.getItems().addAll(dir.list());

            Socket socket = new Socket("localhost", 8188);
            din = new DataInputStream( socket.getInputStream());
            dout = new DataOutputStream(socket.getOutputStream());
            Thread readThread = new Thread(() -> {
                try {
                    while (true) {
                       String status = din.readUTF();
                       Platform.runLater(()->{
                           status_bar.setText(status);
                       });
                    }
                }catch (Exception e){
                    System.err.println("Exception while read!");;
                }
            });
            readThread.setDaemon(true);
            readThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
