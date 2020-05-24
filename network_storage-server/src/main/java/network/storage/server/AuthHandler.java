package network.storage.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class AuthHandler extends ChannelInboundHandlerAdapter {
    private boolean authOk = false;


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (authOk) {
            ctx.fireChannelRead(msg);
        } else {
            ByteBuf logAndPassBuf = (ByteBuf) msg;
            byte[] logAndPassByte = new byte[10];
            logAndPassBuf.readBytes(logAndPassByte); //считали из буфера в массив

            String logAndPassString = new String(logAndPassByte, "UTF-8");
            String login = logAndPassString.split(" ")[0];
            String password = logAndPassString.split(" ")[1];
            System.out.println(AuthService.getNickByLoginAndPass(login, password));
            authOk = true;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    public boolean isAuthOk() {
        return authOk;
    }

    public void setAuthOk(boolean authOk) {
        this.authOk = authOk;
    }
}
