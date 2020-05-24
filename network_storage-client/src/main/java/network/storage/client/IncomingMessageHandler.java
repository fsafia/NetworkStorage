package network.storage.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import network.storage.common.Protocol;

public class IncomingMessageHandler extends ChannelInboundHandlerAdapter {
    Protocol protocol = new Protocol("1client-storage/");
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        while (buf.readableBytes() > 0) {
            protocol.executeComand(ctx, buf);
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("client connect");
        ctx.write("1client-storage/1.odt");
        ctx.write("1client-storage/2.txt");
        ctx.flush();
    }
}
