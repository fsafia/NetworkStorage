package network.storage.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ProtoHandler extends ChannelInboundHandlerAdapter {
    public enum StateHandler {IDLE,COMAND, FILE};

    private StateHandler currentStateHandler = StateHandler.IDLE;
    private ProtoComand protoComand;
    private ProtocolFile protocolFile;
    public String storage;
    public ProtoFileSender protoFileSender;
    private Callback onReceivedCallback;

    public ProtoHandler(String storage/*, ProtoFileSender protoFileSender*/, ProtoComand protoComand) {
        this.storage = storage;
        this.protoFileSender = new ProtoFileSender();
        this.protoComand = protoComand;
        this.protocolFile = new ProtocolFile(storage);
    }

    private Runnable finishOperation = () -> {
        System.out.println("Файл загружен");
        currentStateHandler = StateHandler.IDLE;
    };

    public void setOnReceivedCallback(Callback onReceivedCallback) {
        this.onReceivedCallback = onReceivedCallback;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        protoFileSender.setChannel(ctx.channel());
        while (buf.readableBytes() > 0) {
            if (currentStateHandler == StateHandler.IDLE) {
                byte comand = buf.readByte();
                if (comand == Comand.WRITE_FILE) {
                    currentStateHandler = StateHandler.FILE;
                    protocolFile.currentState = ProtocolFile.State.NAME_LENGHT;

                } else {
                    currentStateHandler = StateHandler.COMAND;
                    protoComand.setComand(comand);
                    protoComand.currentState = ProtoComand.State.MSG_LENGHT;
                }
            }

            if (currentStateHandler == StateHandler.FILE) {
                protocolFile.writeFile(ctx, buf, protoComand.nick , finishOperation, onReceivedCallback);   ///////////////////////////nick??????
            }

            if (currentStateHandler == StateHandler.COMAND) {
                protoComand.receiveMsg(protoFileSender, buf, finishOperation, ctx);
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
