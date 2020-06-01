package network.storage.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import network.storage.common.Comand;
import network.storage.common.ProtoFileSender;
import network.storage.common.ProtocolComand;
import network.storage.common.ProtocolFile;

public class ProtoHandler extends ChannelInboundHandlerAdapter {
    ProtocolFile protocol;
    ProtocolComand protocolCom;
    private String nickName;
    private ProtoFileSender protoFileSender;
    private AuthHandler authHandler;
    private Runnable finishOperation = () -> {
        System.out.println("Файл загружен");
        protoFileSender.sendServerStorageList(Comand.SERVER_STORAGE_LiST, authHandler.getServerStorageList());
        currentRequest = Request.IDLE;
    };
    ProtoHandler() {
        protocol = new ProtocolFile("1server-storage");
        protocolCom = new ProtocolComand("1server-storage");

    }
    public enum Request {IDLE,COMAND, FILE};
    private Request currentRequest = Request.IDLE;


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (nickName == null) {  //клиент только что авторизовался
            authHandler = ctx.pipeline().get(AuthHandler.class);
            nickName = authHandler.getNick();
            protocolCom.setNick(nickName);
            protoFileSender = new ProtoFileSender(ctx.channel());
        }

        ByteBuf buf = (ByteBuf) msg;

        while (buf.readableBytes() > 0) {
            if (currentRequest == Request.IDLE) {
                byte comand = buf.readByte();
                if (comand == Comand.CLIENT_CLOSE) { //если user  авторизовался и закрыл соединение
                    ctx.close();
                    AuthService.getUserList().remove(nickName);
                } else if (comand == Comand.WRITE_FILE) {
                    currentRequest = Request.FILE;
                } else {
                    currentRequest = Request.COMAND;
                    protocolCom.setComand(comand);
                }
            }

            if (currentRequest == Request.FILE) {
                protocol.writeFile(ctx, buf, nickName, finishOperation);
//                ProtoFileSender protoFileSender = new ProtoFileSender(ctx.channel());
//                protoFileSender.sendServerStorageList(Comand.SERVER_STORAGE_LiST, authHandler.getServerStorageList());
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
