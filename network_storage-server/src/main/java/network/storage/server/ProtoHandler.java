package network.storage.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import network.storage.common.Comand;
import network.storage.common.ProtocolComand;
import network.storage.common.ProtocolFile;

public class ProtoHandler extends ChannelInboundHandlerAdapter {
    ProtocolFile protocol;
    ProtocolComand protocolCom;
    private Runnable finishOperation = () -> {
        System.out.println("Файл загружен");
        currentRequest = Request.IDLE;//////////////////добавить обновление списка файлов?
    };

    ProtoHandler() {
        protocol = new ProtocolFile("1server-storage");
        protocolCom = new ProtocolComand("1server-storage");

    }
    public enum Request {IDLE,COMAND, FILE};
    private Request currentRequest = Request.IDLE;


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        AuthHandler authHandler = ctx.pipeline().get(AuthHandler.class);
        protocolCom.setNick(authHandler.getNick());//передает ник каждый раз

        while (buf.readableBytes() > 0) {
            if (currentRequest == Request.IDLE) {
                byte comand = buf.readByte();
                if (comand == Comand.CLIENT_CLOSE) { //если user  авторизовался и закрыл соединение
                    ctx.close();
                    AuthService.getUserList().remove(authHandler.getNick());
                } else if (comand == Comand.WRITE_FILE) {
                    currentRequest = Request.FILE;
                } else {
                    currentRequest = Request.COMAND;
                    protocolCom.setComand(comand);
                }
            }

            if (currentRequest == Request.FILE) {
                protocol.writeFile(ctx, buf, authHandler.getNick(), finishOperation);
            }

            if (currentRequest == Request.COMAND) {
                protocolCom.executeComand(ctx, buf,finishOperation);
            }
        }
        if (buf.readableBytes() == 0) {
            buf.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
