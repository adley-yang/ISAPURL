import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * Project Name:ISAPURL
 * Package Name:PACKAGE_NAME
 * Date:15-1-30 下午4:17
 * Copyright (c) 2015, adleyyang.cn@gmail.com All Rights Reserved.
 */
public class TestMTFile {
    public static void main(String args[]) throws InterruptedException {

        long start = System.currentTimeMillis();

        BufferedReader reader = null;
        ArrayList<Thread> threads = new ArrayList<Thread>();
        try {
            reader = new BufferedReader(new FileReader(
                    "C:\\calc.txt"));
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        for (int i = 0; i <= 100; i++) {
            Runnable task = new ReadFileMT(reader);
            Thread worker = new Thread(task);
            // We can set the name of the thread
            worker.setName(String.valueOf(i));
            // Start the thread, never call method run() direct
            worker.start();
            // Remember the thread for later usage
            threads.add(worker);
        }

        int running = 0;
        int runner1 = 0;
        int runner2 = 0;
        do {
            running = 0;
            for (Thread thread : threads) {
                if (thread.isAlive()) {
                    runner1 = running++;
                }
            }
            if (runner2 != runner1) {
                runner2 = runner1;
                System.out.println("We have " + runner2 + " running threads. ");

            }
        } while (running > 0);

        if (running == 0) {
            System.out.println("Ended");
        }

        /*for (; ; ) {
            for (Thread thread_tmp : threads) {
                thread_tmp.join();
            }
        }*/
        long costtime = System.currentTimeMillis()-start;
        System.out.println("costtime:"+costtime);
    }
}
