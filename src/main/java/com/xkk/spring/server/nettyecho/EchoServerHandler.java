package com.xkk.spring.server.nettyecho;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 功能描述：
 *入站业务处理器
 * @Author: XKK
 * @Date: 2020/11/20 9:49
 */
public class EchoServerHandler extends ChannelInboundHandlerAdapter {

    /**
     *有数据包入站，通道可读
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //将msg消息转化成ByteBuf
        ByteBuf buf = (ByteBuf) msg;
        System.out.println("msg类型：" + (buf.hasArray() ? "堆内存":"直接内存"));
        //获取缓冲区可读byte的长度
        int len = buf.readableBytes();
        //将数据读到字节数组中
        byte[] arr = new byte[len];
        buf.getBytes(0,arr);
        System.out.println("服务器接收到消息：" + new String(arr,"UTF-8"));

        //记录写回前的引用计数
        System.out.println("写回前，msg的引用计数：" + buf.refCnt());
        //写回数据，异步任务
        ChannelFuture channelFuture = ctx.writeAndFlush(msg);
        //监听写回后的引用计数
        channelFuture.addListener((ChannelFuture future) -> {
            System.out.println("写回后，msg的引用计数：" + buf.refCnt());
        });
    }
}
