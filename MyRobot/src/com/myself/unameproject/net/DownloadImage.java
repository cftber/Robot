package com.myself.unameproject.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
 
public class DownloadImage implements Runnable {
     
    public static String IMAGE_PATH = "D:\\IMAGE";
     
    private int  imageCount     = 0;
    private File image          = null;
    private URL  imageUrl       = null;
    private List<String> images = null;
    private BufferedInputStream  inputStream  = null;
    private BufferedOutputStream outputStream = null;
     
    public DownloadImage(List<String> image) {
        this.images = image;
    }
     
 
    @Override
    public void run() {
        // TODO Auto-generated method stub
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HHmmssSSS");
        try {
             
            while(!images.isEmpty()) {
                imageUrl = new URL(images.remove(0));
                imageUrl.openConnection().setConnectTimeout(1500);
                imageUrl.openConnection().setReadTimeout(10000);
                inputStream = new BufferedInputStream(imageUrl.openStream());
                image = new File(IMAGE_PATH + "\\" + dateFormat.format(new Date()) + "." + imageUrl.toString().substring(imageUrl.toString().lastIndexOf(".") + 1));
                outputStream = new BufferedOutputStream(new FileOutputStream(image));
                byte[] buf = new byte[2048];  
                int length = inputStream.read(buf);  
                while (length != -1) {  
                    outputStream.write(buf, 0, length);  
                    length = inputStream.read(buf);  
                }
                next();
            }
            //wait();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                next();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
     
    public void next() throws IOException {
        if (inputStream != null) {
            inputStream.close();
        }
        if (outputStream != null) {
            outputStream.close();
        }
        image  = null;
        //images = null;
        imageUrl     = null;
        inputStream  = null;
        outputStream = null;
        System.gc();
        System.out.println("DownloadImage >>> " + ++imageCount);
    }
 
}