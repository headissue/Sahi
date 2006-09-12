package net.sf.sahi.command;

import net.sf.sahi.request.HttpRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Debug {
    public void toOut(HttpRequest request) {
        String msg = request.getParameter("msg");
        System.out.println(msg);
    }

    public void toErr(HttpRequest request) {
        String msg = request.getParameter("msg");
        System.err.println(msg);
    }

    public void toFile(HttpRequest request) {
        String msg = request.getParameter("msg");
        try {
            File file = new File(request.getParameter("file"));
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream out;
            out = new FileOutputStream(file, true);
            out.write((msg + "\n").getBytes());
            out.close();
        } catch (IOException e) {
            System.out.println(msg);
        }
    }
}
