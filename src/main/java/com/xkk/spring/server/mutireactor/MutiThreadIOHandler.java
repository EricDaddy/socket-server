package com.xkk.spring.server.mutireactor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 功能描述：
 *
 * @Author: XKK
 * @Date: 2020/11/5 14:53
 */
public class MutiThreadIOHandler implements Runnable {
    final SocketChannel channel;
    final SelectionKey sk;
    final ByteBuffer buffer = ByteBuffer.allocate(1024);
    static final int RECIEVING = 0,SENDING = 1;
    int state = RECIEVING;
    static ExecutorService pool = Executors.newFixedThreadPool(4);

    MutiThreadIOHandler(Selector selector, SocketChannel c) throws IOException {
        channel = c;
        c.configureBlocking(false);
        sk = channel.register(selector,0);
        sk.attach(this);
        sk.interestOps(SelectionKey.OP_READ);
        selector.wakeup();
    }

    @Override
    public void run() {
        //异步任务，在独立的线程池中执行
        pool.execute(new AsyncTask());
    }


    class AsyncTask implements Runnable {

        @Override
        public void run() {
            MutiThreadIOHandler.this.asyncRun();
        }
    }

    //业务处理，不在反应器线程中执行
    private synchronized void asyncRun() {
        System.out.println("事件IO处理开始执行");
        try {
            if (state == SENDING) {
                channel.write(buffer);
                buffer.clear();
                sk.interestOps(SelectionKey.OP_READ);
                state = RECIEVING;
            } else if (state == RECIEVING) {
                int length = 0;
                while ((length = channel.read(buffer)) > 0) {
                    System.out.println(Thread.currentThread().toString() +"读取数据：" + new String(buffer.array(),0,length));
                }
                buffer.flip();
                sk.interestOps(SelectionKey.OP_WRITE);
                state = SENDING;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
