package network.storage.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import network.storage.common.Protocol;
import network.storage.common.ProtocolLogPass;

public class IncomingMessageHandler extends ChannelInboundHandlerAdapter {
    Protocol protocol = new Protocol("1client-storage/");
    ProtocolLogPass protocolLogPass = new ProtocolLogPass();
    Controller c;

    public IncomingMessageHandler(Controller c) {
        this.c = c;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        if (!c.getIsAuthorized()) {
            protocolLogPass.executeComand(ctx, buf);
            c.authResponse(protocolLogPass.comand, protocolLogPass.msgString);

        } else {
            while (buf.readableBytes() > 0) {
                protocol.executeComand(ctx, buf);
            }
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
