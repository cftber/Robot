package com.myself.unameproject.net;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
 
public class MyRobot {
     
    private List<String> hrefs   = Collections.synchronizedList(new ArrayList<String>());
    private List<String> visited = Collections.synchronizedList(new ArrayList<String>());
    private List<String> images  = Collections.synchronizedList(new ArrayList<String>());
     
    public MyRobot(String href) {
        hrefs.add(href);
    }
     
    public void run() throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        pool.execute(new SearchHreg(hrefs, visited, images));
        Thread.sleep(5000);
        pool.execute(new SearchHreg(hrefs, visited, images));
        //pool.execute(new DownloadImage(images));
        pool.shutdown();
    }
     
    public static void main(String[] args) throws InterruptedException {
        MyRobot robot = new MyRobot("http://sexy.faceks.com/post/2c9c66_145b18c");
        robot.run();
    }
 
}
