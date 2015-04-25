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
 * Ѱ��href����
 * @author hongframe
 *
 */
public class SearchHreg implements Runnable {
     
    //��ǰ������url
    private URL url = null;
    private String href = null;
    //��ǰurl������ҳ������
    private StringBuffer pageContent = null;
    //��ȡҳ������
    private BufferedReader reader    = null;
    //��ǰurl��Connection
    private HttpURLConnection httpURLConnection = null;
    //δ����url����
    private List<String> hrefs     = null;
    //�ѽ���url����
    private List<String> visited = null;
    //ͼƬ���Ӷ���
    private List<String> images  = null;
    //�ѽ���������
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
        //δ�������зǿ�ʱ����������
        while(!hrefs.isEmpty()) {
            try {
                //�ѵ�ǰҪ������url�ַ�����hrefs�Ƶ�visited
                visited.add(hrefs.remove(0));
                //����visited���һ��Ԫ��
                url = new URL(visited.get(visited.size()-1));
                System.out.println("�ѽ����� " + ++analyze + " �����ӡ�����");
                httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setConnectTimeout(1500);
                httpURLConnection.setReadTimeout(3000);
                //���ַ�������
                reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                //ÿ���ַ���
                content = null;
                pageContent = new StringBuffer();
                //��ʼ��ȡҳ������
                while((content = reader.readLine()) != null) {
                    pageContent.append(content);
                }
                //�ر��ַ�������
                reader.close();
                //arr1Ϊ��ҳ������href��arr2Ϊ��ҳ�������<img>��src
                String[] arr1 = getlinks(pageContent.toString(), HREF_REGEX);
                String[] arr2 = getlinks(pageContent.toString(), IMAGE_REGEX);
                for(String str : arr1) {
                    //��ȡhttpЭ����httpsЭ���url�����Ų����ڵ�url
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