package com.xkk.spring.server.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 功能描述：netty 业务处理handler模块
 *
 * @Author: XKK
 * @Date: 2020/11/6 10:10
 */
public class NettyDiscardHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in = (ByteBuf) msg;
        try {
            System.out.println("收到消息，消息如下");
            while(in.isReadable()) {
                System.out.println((char)in.readByte());
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }
}
