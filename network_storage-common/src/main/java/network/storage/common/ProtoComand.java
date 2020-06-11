package network.storage.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;

import java.nio.charset.StandardCharsets;

public abstract class ProtoComand {
    public enum State {IDLE, MSG_LENGHT, MSG_TEXT}

    public State currentState = State.IDLE;
    private int msgLenght;
    private byte comand;
    public String msgString;
    private ByteBuf buffer;
    private int receivedLength;
    public String nick;

    public void setCurrentState(State currentState) {
        this.currentState = currentState;
    }
    public void setComand(byte comand) {
        this.comand = comand;
    }

    public void receiveMsg(ProtoFileSender protoFileSender, ByteBuf buf, Runnable finishOperation, ChannelHandlerContext ctx) throws Exception {

        if (currentState == State.MSG_LENGHT) {
            if (buf.readableBytes() >= 4) {  // считывает int
                msgLenght = buf.readInt();
                currentState = State.MSG_TEXT;
                buffer = ByteBufAllocator.DEFAULT.directBuffer(msgLenght);
            }
        }

        if (currentState == State.MSG_TEXT) {
            while (buf.readableBytes() > 0) {
                buffer.writeByte(buf.readByte());
                receivedLength++;
                if (receivedLength == msgLenght) {
                    msgString = buffer.toString(StandardCharsets.UTF_8);
                    executСomand(protoFileSender, comand, msgString, ctx);
                    finishOperation.run();
                    currentState = State.IDLE;
                    buffer.clear();
                    receivedLength = 0;
                    return;
                }
            }
        }
    }

    public abstract void executСomand(ProtoFileSender protoFileSender, byte cmd, String msgString, ChannelHandlerContext ctx) ;

}
