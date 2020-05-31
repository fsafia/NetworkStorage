package network.storage.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import network.storage.common.Comand;
import network.storage.common.ProtocolComand;
import network.storage.common.ProtocolFile;
import network.storage.common.ProtocolLogPass;

public class IncomingMessageHandler extends ChannelInboundHandlerAdapter {
    ProtocolFile protocol = new ProtocolFile("1client-storage");
    ProtocolComand protocolCom = new ProtocolComand("1client-storage");
    ProtocolLogPass protocolLogPass = new ProtocolLogPass();
    Controller c;

    public enum Response {IDLE, COMAND, FILE}

    ;
    private Response currentResponse = Response.IDLE;
    private Runnable finishOperation = () -> {
        System.out.println("Файл загружен");
        currentResponse = Response.IDLE;//////////////////добавить обновление списка файлов?
    };

    public IncomingMessageHandler(Controller c) {
        this.c = c;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!c.getIsAuthorized()) {
            ByteBuf buf = (ByteBuf) msg;
            byte comand = buf.readByte();
            ProtocolLogPass protocolLogPass = new ProtocolLogPass();
            protocolLogPass.executeComand(comand, ctx, buf);
            c.authResponse(comand, protocolLogPass.msgString);

        } else {
            ByteBuf buf = (ByteBuf) msg;
            while (buf.readableBytes() > 0) {
                if (currentResponse == Response.IDLE) {
                    byte comand = buf.readByte();
                    if (comand == Comand.WRITE_FILE) {
                        currentResponse = Response.FILE;
                    }
//                    else {
//                        currentResponse = Response.COMAND;  //возможна только команда о запрсосе несущ файла
//                        protocolCom.setComand(comand);
//                    }
                }

                if (currentResponse == Response.FILE) {
                    protocol.writeFile(ctx, buf, "", finishOperation);
                }

//                if (currentResponse == Response.COMAND) {
//                    protocolCom.executeComand(ctx, buf,finishOperation);
//                    c.authResponse(Comand.AUTH_NOT_OK, protocolCom.getMsgTextSb().toString());
//                }
            }
            if (buf.readableBytes() == 0) {
                buf.release();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

}