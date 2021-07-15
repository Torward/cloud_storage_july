package nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import static com.sun.activation.registries.LogSupport.log;

public class Server {

    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private String name = "user";
    private static int cnt = 1;
    private Path root = Paths.get(".");

    public Server() throws IOException {

        serverSocketChannel = ServerSocketChannel.open();
        selector = Selector.open();
        serverSocketChannel.bind(new InetSocketAddress(8187));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT, "Ivan");
        //  sc.register(selector,SelectionKey.OP_WRITE,"Hallo World!");
        System.out.println("Server connected...");
        while (serverSocketChannel.isOpen()) {
            selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (key.isAcceptable()) {
                    handleAccept(key);
                }
                if (key.isReadable()) {
                    handleRead(key);
                }
                iterator.remove();
            }
        }

    }

    private void handleRead(SelectionKey key) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(8 * 1024);
        SocketChannel channel = (SocketChannel) key.channel();
        String name = (String) key.attachment();
        int readKey;


        StringBuilder sb = new StringBuilder();


        while (true) {

            readKey = channel.read(buffer);
            buffer.flip();

            if (readKey == -1) {
                channel.close();
                break;
            }
            if (readKey > 0) {
                while (buffer.hasRemaining()) {

                    if (sb.toString().equals("ls")) {
                        Path root = Paths.get(".");
                        Files.walkFileTree(root, new HashSet<>(), 1, new SimpleFileVisitor<Path>() {

                            @Override
                            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                System.out.println(file);
                                channel.write(ByteBuffer.wrap((file + " ").getBytes(StandardCharsets.UTF_8)));
                                return super.visitFile(file, attrs);
                            }

                        });
                    }
                    sb.append((char) buffer.get());
                }
                buffer.clear();
            } else {
                break;
            }
        }

        if (sb.toString().startsWith("cat")) {
                System.out.println("Зашли в функцию" + sb);
                Path clientFileName = Paths.get(String.valueOf(sb.substring(4)).trim());
                Path root = Paths.get("./", String.valueOf(clientFileName));
                byte[] bytes = Files.readAllBytes(root);
                String text = new String(bytes, StandardCharsets.UTF_8);
                channel.write(ByteBuffer.wrap((text + " ").getBytes(StandardCharsets.UTF_8)));
        }
        System.out.println("received: " + sb);
        for (SelectionKey selectionKey : selector.keys()) {
            if (selectionKey.isValid() && selectionKey.channel() instanceof SocketChannel) {
                SocketChannel ch = (SocketChannel) selectionKey.channel();
                ch.write(ByteBuffer.wrap((name + ": " + sb).getBytes(StandardCharsets.UTF_8)));
            }
        }
    }

    private void handleAccept(SelectionKey key) throws IOException {
        SocketChannel channel = serverSocketChannel.accept(); //точка соединения клиента и сервера
        channel.configureBlocking(false);

        channel.register(selector, SelectionKey.OP_READ, name + cnt);
        cnt++;
    }

    public static void main(String[] args) throws IOException {
        new Server();
    }
}
