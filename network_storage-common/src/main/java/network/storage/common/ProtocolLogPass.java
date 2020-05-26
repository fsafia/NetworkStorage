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
//            if(currentState == State.IDLE) {
//                comand = buf.readByte();
//                if (Comand.TRY_TO_AUTH.getNumberComand() == comand                   // 10-команда для авторизации
//                    || Comand.TRY_TO_SIGNUP.getNumberComand() == comand           // 11-команда для регистрации
//                    || Comand.AUTH_NOT_OK.getNumberComand() == comand            // 13-команда неудачная регистрация
//                    || Comand.AUTH_OK.getNumberComand() == comand) {           // 12-регистрация прошла успешно
//
//                    currentState = State.MSG_LENGHT;
//                    log = null;
//                    pass = null;
//                    System.out.println("STATE: Start log and pass receiving");
//                } else {
//                    System.out.println("неверная команда");
//                }
//            }

            if (currentState == State.MSG_LENGHT) {
                getMsgLenght(buf);
            }

            if (currentState == State.MSG_TEXT) {
                if (buf.readableBytes() >= msgLenght) {
                    msgString = getMsgString(buf); //String в виде "log1 pass1"
                    // ? надо ли менять состояние
                }

//                if (comand == Comand.TRY_TO_AUTH.getNumberComand()) {
//                    log = msgString.split(" ")[0];
//                    pass = msgString.split(" ")[1];
//                    currentState = State.IDLE;
//                }
//                if (comand == Comand.TRY_TO_SIGNUP.getNumberComand()) {
//                    log = msgString.split(" ")[0];
//                    pass = msgString.split(" ")[1];
//                    nick = msgString.split(" ")[2];
//                    currentState = State.IDLE;
//
//                }
//
//                if (comand == Comand.AUTH_NOT_OK.getNumberComand()) {
//                    currentState = State.IDLE;
//                }
//
//                if (comand == Comand.AUTH_OK.getNumberComand()) {
//                    currentState = State.IDLE;
//                }
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

