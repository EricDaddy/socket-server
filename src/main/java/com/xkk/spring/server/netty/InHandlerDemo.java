package com.xkk.spring.server.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 功能描述：
 * 业务处理器的各个方法和生命周期
 * @Author: XKK
 * @Date: 2020/11/19 14:57
 */
public class InHandlerDemo extends ChannelInboundHandlerAdapter {

    /**
     * 当业务处理器被加入到流水线时被调用
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        System.out.println("被调用：handlerAdded()");
        super.handlerAdded(ctx);
    }

    /**
     * 当业务处理器成功绑定一个NioEventLoop线程后被调用
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("被调用：channelRegistered");
        ctx.fireChannelRegistered();
    }


    /**
     * 当通道激活成功（业务处理器添加，注册的异步任务完成，并且反应器线程绑定的异步任务完成
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("被调用：channelActive()");
        ctx.fireChannelActive();
    }

    /**
     *有数据包入站，通道可读
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("被调用：channelRead()");
        ctx.fireChannelRead(msg);
    }


    /**
     * 完成入站处理，数据读取完成
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        System.out.println("被调用：channelReadComplete()");
        ctx.fireChannelReadComplete();
    }



    /**
     * 当通道的底层连接已经不是establish状态或关闭
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("被调用：channelInactive()");
        ctx.fireChannelInactive();
    }


    /**
     * 当通道与反应器线程解除绑定
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("被调用：channelUnregistered9()");
        ctx.fireChannelUnregistered();
    }


    /**
     * 移除通道上所有的业务处理器
     * @param ctx
     * @throws Exception
     */
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        System.out.println("被调用：handlerRemoved");
        super.handlerRemoved(ctx);
    }

}
