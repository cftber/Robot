package com.myself.unameproject.net;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
 
/**
 * 寻找href链接
 * @author hongframe
 *
 */
public class SearchHreg implements Runnable {
     
    //当前解析的url
    private URL url = null;
    private String href = null;
    //当前url解析的页面内容
    private StringBuffer pageContent = null;
    //读取页面内容
    private BufferedReader reader    = null;
    //当前url的Connection
    private HttpURLConnection httpURLConnection = null;
    //未解析url队列
    private List<String> hrefs     = null;
    //已解析url队列
    private List<String> visited = null;
    //图片链接队列
    private List<String> images  = null;
    //已解析链接数
    private int analyze = 0;
    //
    private int count   = 0;
    public static final String HREF_REGEX  = "\\s*(?i)href\\s*=\"\\s*(\"([^\"]*\")|'[^']*'|([^'\">\\s]+))\"";
    public static final String IMAGE_REGEX = "<img[^<>]*?\\ssrc=['\"]?(.*?)['\"].*?>";
     
    public SearchHreg(String href) {
        this.href = href;
    }
     
    public SearchHreg(List<String> hrefs, List<String> visited, List<String> images) {
        this.hrefs = hrefs;
        this.visited = visited;
        this.images = images;
    }
 
    @Override
    public void run() {
        String content = null;
        //未解析队列非空时，继续解析
        while(!hrefs.isEmpty()) {
            try {
                //把当前要解析的url字符串从hrefs移到visited
                visited.add(hrefs.remove(0));
                //访问visited最后一个元素
                url = new URL(visited.get(visited.size()-1));
                System.out.println("已解析第 " + ++analyze + " 个连接。。。");
                httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setConnectTimeout(1500);
                httpURLConnection.setReadTimeout(3000);
                //打开字符输入流
                reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                //每行字符串
                content = null;
                pageContent = new StringBuffer();
                //开始读取页面内容
                while((content = reader.readLine()) != null) {
                    pageContent.append(content);
                }
                //关闭字符输入流
                reader.close();
                //arr1为该页面所有href，arr2为该页面的所有<img>的src
                String[] arr1 = getlinks(pageContent.toString(), HREF_REGEX);
                String[] arr2 = getlinks(pageContent.toString(), IMAGE_REGEX);
                for(String str : arr1) {
                    //获取http协议与https协议的url，则存放不存在的url
                    if((str.startsWith("http") || str.startsWith("https")) && (visited.indexOf(str) == -1) && (hrefs.indexOf(str) == -1)) {
                        hrefs.add(str);
                        System.out.println(++count + "  >>> " + str);
                    }
                }
                for(String str : arr2) {
                    if(images.indexOf(str) == -1) {
                        images.add(str);
                        //System.out.println(count + " IMAGE >>> " + str);
                    }
                }
                new Thread(new DownloadImage(images)).start();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                 
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                next();
            }
        }
    }
     
    public void next() { 
        try {
            pageContent = null;
            url = null;
            httpURLConnection = null;
            if(reader != null) {
                reader.close();
            }
            reader = null;
            System.gc();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
     
    public String[] getlinks(String pageContent, String regex) {
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(pageContent);
        List<String> strs = new ArrayList<String>();
        while(matcher.find()) {
            strs.add(matcher.group(1));
        }
        String[] strings = new String[strs.size()];
        return strs.toArray(strings);
    }
 
}