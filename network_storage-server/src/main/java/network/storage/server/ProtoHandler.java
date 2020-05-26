package network.storage.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import network.storage.common.Protocol;

public class ProtoHandler extends ChannelInboundHandlerAdapter {
    Protocol protocol;
    ProtoHandler() {
        protocol = new Protocol("1server-storage/");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        AuthHandler authHandler = ctx.pipeline().get(AuthHandler.class);

        ByteBuf buf = (ByteBuf) msg;
        while (buf.readableBytes() > 0) {
            protocol.executeComand(ctx, buf, authHandler.getNick());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
