package network.storage.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import javafx.application.Platform;
import network.storage.common.Comand;
import network.storage.common.ProtocolComand;
import network.storage.common.ProtocolFile;
import network.storage.common.ProtocolLogPass;

public class IncomingMessageHandler extends ChannelInboundHandlerAdapter {
    ProtocolFile protocolFile = new ProtocolFile("1client-storage");
    ProtocolComand protocolCom = new ProtocolComand("1client-storage");
    ProtocolLogPass protocolLogPass = new ProtocolLogPass();
    Controller c;

    public enum Response {IDLE, COMAND, FILE}

    ;
    private Response currentResponse = Response.IDLE;
    private Runnable finishOperation = () -> {
        System.out.println("Файл загружен");
        Platform.runLater(() -> {c.updateLocalStorage();});
        currentResponse = Response.IDLE;
    };

    public IncomingMessageHandler(Controller c) {
        this.c = c;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        while (buf.readableBytes() > 0) {
            if (currentResponse == Response.IDLE) {
                byte comand = buf.readByte();
                if (comand == Comand.AUTH_NOT_OK || comand == Comand.AUTH_OK) {
                    ProtocolLogPass protocolLogPass = new ProtocolLogPass();
                    protocolLogPass.executeComand(buf);
                    c.authResponse(comand, protocolLogPass.msgString);
                    currentResponse = Response.IDLE;
                } else if (comand == Comand.WRITE_FILE) {
                    currentResponse = Response.FILE;
                } else {
                    currentResponse = Response.COMAND;
                    protocolCom.setComand(comand);
                }
            }

            if (currentResponse == Response.FILE) {
                protocolFile.writeFile(ctx, buf, "", finishOperation);
            }

            if (currentResponse == Response.COMAND) {
                protocolCom.executeComand(ctx, buf,finishOperation);
                if (protocolCom.msgString != null ) {
                    c.updateServerStorage(protocolCom.msgString);
                }
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