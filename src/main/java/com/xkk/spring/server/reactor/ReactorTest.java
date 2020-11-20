package com.xkk.spring.server.reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * 功能描述：
 *
 * @Author: XKK
 * @Date: 2020/11/5 9:26
 */
public class ReactorTest implements Runnable {
    Selector selector;
    ServerSocketChannel serverChannel;

    ReactorTest() throws IOException {
        //获取选择器
        selector = Selector.open();
        //开启通道
        serverChannel = ServerSocketChannel.open();
        ServerSocket serverSocket = serverChannel.socket();
        //设置为非阻塞
        serverChannel.configureBlocking(false);
        serverSocket.bind(new InetSocketAddress(8091));

        SelectionKey selectionKey = serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        selectionKey.attach(new AcceptorHandler());
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

    void dispatch(SelectionKey sk) {
        System.out.println("开始分发接收到的新连接事件");
        Runnable runnable =(Runnable) sk.attachment();
        //调用之前绑定到选择器上的处理器
        if(runnable != null) {
            runnable.run();
        }
    }

    //新连接处理器
    class AcceptorHandler implements Runnable {

        @Override
        public void run() {
            System.out.println("新连接事件处理器开始执行");
            try {
                //接受新连接，并创建数据处理处理器
                SocketChannel socketChannel = serverChannel.accept();
                if(socketChannel != null) {
                    new IOHandler(selector,socketChannel);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException{
        new Thread(new ReactorTest()).start();
    }
}
