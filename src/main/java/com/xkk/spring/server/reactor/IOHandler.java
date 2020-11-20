package com.xkk.spring.server.reactor;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

/**
 * 功能描述：
 *
 * @Author: XKK
 * @Date: 2020/11/5 9:54
 */
public class IOHandler implements Runnable {
    private Charset charset = Charset.forName("UTF-8");
    final SocketChannel channel;
    final SelectionKey selectionKey;
    final ByteBuffer buffer = ByteBuffer.allocate(200);
    static final int RECIEVING = 0, SENDING = 1;
    int state = RECIEVING;

    IOHandler(Selector selector,SocketChannel c) throws IOException {
        channel = c;
        c.configureBlocking(false);
        //
        selectionKey = channel.register(selector,0);
        selectionKey.attach(this);
        selectionKey.interestOps(SelectionKey.OP_READ);
        selector.wakeup();
    }

    @Override
    public void run() {
        System.out.println("事件IO处理开始执行");
        try {
            if (state == SENDING) {
              channel.write(buffer);
              buffer.clear();
              selectionKey.interestOps(SelectionKey.OP_READ);
              state = RECIEVING;
            } else if (state == RECIEVING) {
                int length = 0;
                while ((length = channel.read(buffer)) > 0) {
                    System.out.println("读取数据：" + new String(buffer.array(),0,length));
                }
                buffer.flip();
                selectionKey.interestOps(SelectionKey.OP_WRITE);
                state = SENDING;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
