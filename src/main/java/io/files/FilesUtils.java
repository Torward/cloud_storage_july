package io.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class FilesUtils {

    public static void main(String[] args) {
        File file = new File("sample 1.png");
        System.out.println(file.exists());
        File copy = new File("copy.png");
        System.out.println(copy.exists());

        byte [] buffer = new byte[8 * 1024];
        try(FileInputStream in = new FileInputStream(file);
            FileOutputStream out = new FileOutputStream(copy)) {
            int read;
            while ((read = in.read(buffer))!=-1){
                out.write(buffer,0,read);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
