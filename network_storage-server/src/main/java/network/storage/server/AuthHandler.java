package network.storage.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import network.storage.common.Comand;
import network.storage.common.ProtoFileSender;
import network.storage.common.ProtocolLogPass;

public class AuthHandler extends ChannelInboundHandlerAdapter {
    private boolean authOk = false;
    private String nick;
    ProtocolLogPass protocolLogPass = new ProtocolLogPass();


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (authOk) {
            ctx.fireChannelRead(msg);
        } else {
            ByteBuf bufMsg = (ByteBuf) msg;
            ProtoFileSender protoFileSender = new ProtoFileSender(ctx.channel());
            protocolLogPass.executeComand(ctx,bufMsg);
            nick = AuthService.getNickByLoginAndPass(protocolLogPass.log, protocolLogPass.pass);
            if (nick != null) {
                authOk = true;
                protoFileSender.sendAuth(Comand.AUTH_OK, nick, null);//отправить ответ клиенту авторизован

            } else {
                protoFileSender.sendAuth(Comand.AUTH_NOT_OK, "неверный логин пароль", null);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    public boolean getAuthOk() {
        return authOk;
    }

    public void setAuthOk(boolean authOk) {
        this.authOk = authOk;
    }
}
