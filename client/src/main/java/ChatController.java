import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.file.*;
import java.util.ResourceBundle;

/**
 * 1.В клиенте разместил 2 listView, добавил в него возможность отображения файлов директории сервера.
 * 2. Добавил вторую кнопку подающую команду на скачивание файлов с сервера.
 * 3. Разделил функции двух кнопок.
 * Пояснение: Задачу считаю выполненной, поскольку передача файла возможна
 * как в направлении клиент-сервер, так и в направлении сервер-клиент.
 */

public class ChatController implements Initializable {

    public Button download_btn;
    public Button upload_btn;
    private String root = "client/clientFiles";
    private String sroot = "server/serverFiles";


    private DataInputStream din;
    private DataOutputStream dout;
    private byte[] buffer;

    public ListView<String> server_view;
    public ListView<String> list_view;
    public TextField status_bar;

    public void send(ActionEvent actionEvent) throws IOException {
        String fileName = list_view.getSelectionModel().getSelectedItem();
        Path filePath = Paths.get(root, fileName);
        if (!Files.exists(filePath)) {
             throw new FileNotFoundException();
        }
        long size = Files.size(filePath);
        dout.writeUTF("upload");
        dout.writeUTF(fileName);
        dout.writeLong(size);
        Files.copy(filePath, dout);

        dout.flush();
        status_bar.setText("File: " + fileName + " successfully sent");

    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        buffer = new byte[8 * 1024];
        try {
            File dir = new File(root);

            File sDir = new File(sroot);
            server_view.getItems().clear();
            server_view.getItems().addAll(sDir.list());


            list_view.getItems().clear();
            list_view.getItems().addAll(dir.list());

            Socket socket = new Socket("localhost", 8188);
            din = new DataInputStream(socket.getInputStream());
            dout = new DataOutputStream(socket.getOutputStream());
            Thread readThread = new Thread(() -> {
                try {
                    while (true) {
                        String status = din.readUTF();
                        Platform.runLater(() -> {
                            status_bar.setText(status);
                        });
                    }
                } catch (Exception e) {
                    System.err.println("Exception while read!");

                }
            });
            readThread.setDaemon(true);
            readThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void download(ActionEvent actionEvent) throws IOException {

        String fileName = server_view.getSelectionModel().getSelectedItem();
        Path filePath = Paths.get(sroot, fileName);
        if (!Files.exists(filePath)) {
             throw new FileNotFoundException();
        }
        long size = Files.size(filePath);
        dout.writeUTF("download");
        dout.writeUTF(fileName);
        dout.writeLong(size);
        Files.copy(filePath, dout);
        dout.flush();
        status_bar.setText("File: " + fileName + " successfully download");
    }
}
