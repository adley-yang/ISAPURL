import java.io.BufferedReader;
import java.io.IOException;

/**
 * Project Name:ISAPURL
 * Package Name:PACKAGE_NAME
 * Date:15-1-30 下午4:16
 * Copyright (c) 2015, adleyyang.cn@gmail.com All Rights Reserved.
 */
public class ReadFileMT implements Runnable {
    BufferedReader bReader = null;

    ReadFileMT(BufferedReader reader) {
        this.bReader = reader;
    }

    public synchronized void run() {
        String line;
        try {
            while ((line = bReader.readLine()) != null) {

                try {
                    //System.out.println(line);
                    Thread.sleep(1);
                } catch (Exception e) {

                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
