package com.xkk.spring.server.mutireactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 功能描述：
 *
 * @Author: XKK
 * @Date: 2020/11/5 13:59
 */
public class MutiReactorTest {
    ServerSocketChannel serverChannel;
    AtomicInteger next = new AtomicInteger(0);
    //引入多个选择器
    Selector[] selectors = new Selector[2];
    //引入多个子反应器
    SubReactor[] subReactors = null;

    MutiReactorTest() throws IOException {
        selectors[0] = Selector.open();
        selectors[1] = Selector.open();

        //初始化服务器监听通道
        serverChannel = ServerSocketChannel.open();
        serverChannel.socket().bind(new InetSocketAddress(8091));
        serverChannel.configureBlocking(false);
        //第一个选择器，负责监听新连接事件
        SelectionKey sk = serverChannel.register(selectors[0],SelectionKey.OP_ACCEPT);
        sk.attach(new AcceptHandler());

        //一个子反应器对应一个选择器
        SubReactor subReactor1 = new SubReactor(selectors[0]);
        SubReactor subReactor2 = new SubReactor(selectors[1]);
        subReactors = new SubReactor[] {subReactor1,subReactor2};
    }

    private void startService() {
        new Thread(subReactors[0]).start();
        new Thread(subReactors[1]).start();
    }


    //子反应器,负责监听对应的选择器，并分发至相应的处理器
    class SubReactor implements Runnable {

        final Selector selector;

        SubReactor(Selector selector) {
            this.selector = selector;
        }
        @Override
        public void run() {
            try {
                while (!Thread.interrupted()) {
                    selector.select();
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        //反应器负责分发接受到的事件
                        SelectionKey sk = iterator.next();
                        dispatch(sk);
                    }
                    //分发之后清空事件集合
                    selector.selectedKeys().clear();;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void dispatch(SelectionKey sk) {
        System.out.println("开始分发接收到的事件"+sk.interestOps());
        Runnable runnable =(Runnable) sk.attachment();
        //调用之前绑定到选择器上的处理器
        if(runnable != null) {
            runnable.run();
        }
    }

    class AcceptHandler implements Runnable {

        @Override
        public void run() {
            try{
                SocketChannel channel =serverChannel.accept();
                if(channel != null) {
                    new MutiThreadIOHandler(selectors[next.get()],channel);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(next.incrementAndGet() == selectors.length) {
                next.set(0);
            }
        }
    }

    public static void main(String[] args) throws IOException{
        MutiReactorTest mutiReactorTest = new MutiReactorTest();
        mutiReactorTest.startService();
    }
}
