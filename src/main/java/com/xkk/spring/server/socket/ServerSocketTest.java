package com.xkk.spring.server.socket;

import sun.nio.ch.IOUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 功能描述：
 *
 * @Author: XKK
 * @Date: 2020/11/3 17:59
 */
public class ServerSocketTest {
    private Charset charset = StandardCharsets.UTF_8;

    /**
     * 内部类，服务器端保存的客户端对象，对应一个客户端文件
     */
    static class Client {
        //文件名称
        String fileName;
        //文件长度
        long fileLength;
        //开始传输的时间
        long startTime;
        //客户端地址
        InetSocketAddress remoteAddress;
        //输出的文件通道
        FileChannel outChannel;
    }
    private ByteBuffer buffer = ByteBuffer.allocate(200);
    //保存客户端对应的文件传输通道
    Map<SelectableChannel,Client> clientMap = new HashMap<>();


    public void startServer() throws IOException {
        //开启选择器
        Selector selector = Selector.open();
        //开启服务器监听通道
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        ServerSocket serverSocket = serverChannel.socket();
        //设置为非阻塞
        serverChannel.configureBlocking(false);
        //绑定服务器端口
        serverSocket.bind(new InetSocketAddress(8091));
        //将通道注册到选择器上，并注册的IO新事件为"接受新连接"
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("server is listening......");

        //选择感兴趣的IO就绪事件(选择键集合）
        while (selector.select() > 0) {
            //获取选择键集合
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                //获取当个选择键
                SelectionKey key = iterator.next();
                if(key.isAcceptable()) {
                    //连接就绪事件
                    ServerSocketChannel server = (ServerSocketChannel) key.channel();
                    //获取socket通道
                    SocketChannel socketChannel = server.accept();
                    if(socketChannel == null) {
                        continue;
                    }
                    //设置为非阻塞
                    socketChannel.configureBlocking(false);
                    //将客户端新连接通道注册在选择器上
                    SelectionKey selectionKey = socketChannel.register(selector,SelectionKey.OP_READ);
                    //为每一个传输通道，建立一个client客户端对象
                    Client client = new Client();
                    client.remoteAddress =(InetSocketAddress) socketChannel.getRemoteAddress();
                    clientMap.put(socketChannel,client);
                    System.out.println(socketChannel.getRemoteAddress() + "连接成功。。。");
                } else if(key.isReadable()) {
                    //数据可读事件
                    processData(key);
                }
                iterator.remove();
            }
        }

    }

    /**
     * 处理客户端传过来的数据
     */
    private void processData(SelectionKey key) throws IOException {
        Client client = clientMap.get(key.channel());
        SocketChannel socketChannel =(SocketChannel) key.channel();
        int num = 0;
        try {
            buffer.clear();
            //将客户端socket通道数据写入buffer
            while ((num = socketChannel.read(buffer)) > 0) {
                //翻转buffer，转化为读模式
                buffer.flip();
                if (null == client.fileName) {
                    String fileName = charset.decode(buffer).toString();
                    File dir = new File("C:\\Users\\86156\\Desktop");
                    if (!dir.exists()) {
                        dir.mkdir();
                    }
                    client.fileName = fileName;
                    String fullName = dir.getAbsolutePath() + File.separatorChar + fileName;
                    System.out.println("NIO 传输目标文件" + fullName);
                    File file = new File(fullName);
                    FileChannel fileChannel = new FileOutputStream(file).getChannel();
                    client.outChannel = fileChannel;
                } else if (0 == client.fileLength) {
                    long fileLength = buffer.getLong();
                    client.fileLength = fileLength;
                    client.startTime = System.currentTimeMillis();
                    System.out.println("NIO 传输开始：");
                } else {
                    System.out.println("开始写出");
                    client.outChannel.write(buffer);
                    System.out.println("写出成功");
                }
                buffer.clear();
            }
            key.cancel();
        } catch (IOException e) {
            key.cancel();
            e.printStackTrace();
            return;
        }
        //读取数量-1，表示客户端传输结束标志到了
        if(num == -1) {
            client.outChannel.close();
            System.out.println("传输完毕");
            key.cancel();
            System.out.println("文件接受成功，文件名：" + client.fileName + " 文件长度：" + client.fileLength);
            long endTime = System.currentTimeMillis();
            System.out.println("传输毫秒数：" + (endTime - client.startTime));
        }
    }

    public static void main(String[] args) throws IOException{
        ServerSocketTest serverSocketTest = new ServerSocketTest();
        serverSocketTest.startServer();
    }
}

