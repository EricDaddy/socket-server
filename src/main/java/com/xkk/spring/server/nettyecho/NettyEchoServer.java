package com.xkk.spring.server.nettyecho;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.boot.autoconfigure.web.ServerProperties;

import java.net.InetSocketAddress;

/**
 * 功能描述：
 *
 * @Author: XKK
 * @Date: 2020/11/19 16:18
 */
public class NettyEchoServer {

    //启动器
    ServerBootstrap bootstrap = new ServerBootstrap();

    public void runServer() {

        //反应器线程组
        //负责连接监听IO事件(线程池中线程数量为1）
        EventLoopGroup boss = new NioEventLoopGroup(1);
        //负责数据IO事件和handler业务处理(默认线程数量为为最大可用cpu处理器的2倍）
        EventLoopGroup work = new NioEventLoopGroup();

        //将组件装配到启动器中

        try {
            //设置反应器
            bootstrap.group(work,boss);
            //设置通道类型
            bootstrap.channel(NioServerSocketChannel.class);
            //设置服务器监听端口
            bootstrap.localAddress(new InetSocketAddress(8091));
            //设置通道的参数
            //设置TCP心跳机制，主动探测空闲连接的有效性
            bootstrap.option(ChannelOption.SO_KEEPALIVE,true);
            //设置通道的分配器，用来创建缓冲区和分配内存空间（池化或非池化）
            bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

            //装配子通道的流水线，每次有连接到达，执行子通道SocketChannel的初始化
            bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                //在初始化方法中将业务处理器，添加到流水线中
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(new EchoServerHandler());
                }
            });
            //服务端启动器start
            //Netty中的IO操作，都会返回一个异步任务实例
            ChannelFuture channelFuture = bootstrap.bind().sync();
            System.out.println("服务器启动成功，监听端口：" + channelFuture.channel().localAddress());
            //自我阻塞，知道通道关闭的异步任务执行完成
            ChannelFuture closeFuture = channelFuture.channel().closeFuture();
            closeFuture.sync();
            //关闭反应器线程组
            boss.shutdownGracefully();
            work.shutdownGracefully();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        NettyEchoServer nettyEchoServer = new NettyEchoServer();
        nettyEchoServer.runServer();
    }
}
