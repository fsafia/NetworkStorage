package network.storage.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class ProtocolLogPass {

    public enum State {IDLE, MSG_LENGHT, MSG_TEXT, NAME, FILE_LENGHT, FILE}

//    public State currentState = State.IDLE;
    public State currentState = State.MSG_LENGHT;
    private int msgLenght;
    public byte comand;
    public String msgString;
    public String log;
    public String pass;
    public String nick;

    public void executeComand(byte cmd, ChannelHandlerContext ctx, ByteBuf buf) throws Exception{

        while (buf.isReadable()) {
            if (currentState == State.MSG_LENGHT) {
                getMsgLenght(buf);
            }

            if (currentState == State.MSG_TEXT) {
                if (buf.readableBytes() >= msgLenght) {
                    msgString = getMsgString(buf); //String в виде "log1 pass1"
                    // ? надо ли менять состояние
                }
            }
        }
        if (buf.readableBytes() == 0) {
            buf.release();
        }
    }

    private void getMsgLenght(ByteBuf buf){
        if (buf.readableBytes() >= 4) {  // считывает int
            System.out.println("STATE: GET str lenght");
            msgLenght = buf.readInt();
            currentState = State.MSG_TEXT;
        }
    }

    private String getMsgString(ByteBuf buf) throws Exception {
        byte [] fileName = new byte[msgLenght];
        buf.readBytes(fileName);
        String msg = new String(fileName, "UTF-8");
        currentState = State.MSG_LENGHT;
        return msg;
    }
}

