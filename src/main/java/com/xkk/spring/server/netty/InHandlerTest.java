package com.xkk.spring.server.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.embedded.EmbeddedChannel;

/**
 * 功能描述：
 *
 * @Author: XKK
 * @Date: 2020/11/19 15:20
 */
public class InHandlerTest {

    public static void main(String[] args) {
        final InHandlerDemo inHandler = new InHandlerDemo();

        //初始化处理器
        ChannelInitializer initializer = new ChannelInitializer<EmbeddedChannel>() {
            @Override
            protected void initChannel(EmbeddedChannel channel) throws Exception {
                channel.pipeline().addLast(inHandler);
            }
        };

        //创建嵌入式通道
        EmbeddedChannel channel = new EmbeddedChannel(initializer);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeInt(1);

        //模拟入站1
        channel.writeInbound(byteBuf);
        channel.flush();

        //模拟入站2
        channel.writeInbound(byteBuf);
        channel.flush();

        channel.close();

        try {
            Thread.sleep(Integer.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
