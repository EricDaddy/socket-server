package com.xkk.spring.server.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * 功能描述：
 *
 * @Author: XKK
 * @Date: 2020/11/6 9:47
 */
public class NettyDiscardServer {

    ServerBootstrap bootstrap = new ServerBootstrap();

    public void runServer() {
        //创建反应器线程组
        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup work = new NioEventLoopGroup();

        try {
            //设置反应器组
            bootstrap.group(boss,work);
            //设置nio类型的通道
            bootstrap.channel(NioServerSocketChannel.class);
            //设置监听端口
            bootstrap.localAddress(8091);
            //设置通道的参数
            bootstrap.option(ChannelOption.SO_KEEPALIVE,true);
            bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            //装配子通道流水线
            bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    //向子通道流水线添加一个handler处理器
                    socketChannel.pipeline().addLast(new NettyDiscardHandler());
                }
            });
            //绑定服务器，调用sync同步方法阻塞直到绑定成功
            ChannelFuture channelFuture = bootstrap.bind().sync();
            System.out.println("服务器启动成功，监听端口：" + channelFuture.channel().localAddress());
            //等待通道关闭的异步任务结束
            ChannelFuture closeFuture = channelFuture.channel().closeFuture();
            closeFuture.sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            boss.shutdownGracefully();
            work.shutdownGracefully();
        }
    }


    public static void main(String[] args) {
        NettyDiscardServer server = new NettyDiscardServer();
        server.runServer();
    }
}
