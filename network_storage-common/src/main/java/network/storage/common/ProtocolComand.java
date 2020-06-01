package network.storage.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ProtocolComand {
    public enum State {IDLE, MSG_LENGHT, MSG}

    public State currentState = State.IDLE;
    private int msgLenght;
    private long receivedMsgLenght;
    private byte comand;
    private String storage;
    private StringBuffer msgTextSb;
    private String nick;
    public String serverStorageFiles;
    public String msgString;

    public void setNick(String nick) {
        this.nick = nick;
    }

    public StringBuffer getMsgTextSb() {
        return msgTextSb;
    }
    public void setComand(byte comand) {
        this.comand = comand;
    }
    public ProtocolComand(String storage) {
        this.storage = storage;
    }

    public void executeComand(ChannelHandlerContext ctx, ByteBuf buf, Runnable finishOperation) throws Exception {

        if (currentState == State.IDLE) {
            if (Comand.DELETE_FILE_FromServer == comand              // 2- удаление с сервера
                    || Comand.RENAME_FILE_FromServer == comand       // 3- переименование
                    || Comand.DOWNLOAD_FILE_TO_CLIENT == comand     // 4 - cкачивание файла
                    || Comand.SERVER_STORAGE_LiST == comand) {      // 17 - список файлов с сервера

                currentState = State.MSG_LENGHT;
                receivedMsgLenght = 0L;
                msgTextSb = new StringBuffer();
                System.out.println("STATE: Start comand receiving");
            } else {
                System.out.println("отправьте другую команду");
            }
        }

        if (currentState == State.MSG_LENGHT) {
            if (buf.readableBytes() >= 4) {  // считывает int
                msgLenght = buf.readInt();
                currentState = State.MSG;
            }
        }

        if (currentState == State.MSG) {
            if (buf.readableBytes() >= msgLenght) {
                msgString = getMsgString(buf);
                defineCmd(comand, msgString, ctx.channel());
                resetState();
                    finishOperation.run();
                    return;

//                while (buf.readableBytes() > 0) {
//                msgTextSb.append((char) buf.readByte()); // ?
//                receivedMsgLenght++;
//                if (msgLenght == receivedMsgLenght) {
//                    defineCmd(comand, msgTextSb.toString(), ctx.channel());
//                    resetState();
//                    finishOperation.run();
//                    return;
//                }
            }
        }

    }
    private String getMsgString(ByteBuf buf) throws Exception {
        byte [] fileName = new byte[msgLenght];
        buf.readBytes(fileName);
        String msg = new String(fileName, "UTF-8");
        return msg;
    }
    public void defineCmd (byte comand, String msg, Channel channel) throws IOException {
        if (comand == Comand.SERVER_STORAGE_LiST) {
            return;
        }
        ProtoFileSender protoFileSender = new ProtoFileSender(channel);
 //       String fileName = msg.split(" ")[0];
        Path storagePath = Paths.get(storage,nick, msg);
        switch (comand) {
            case Comand.DELETE_FILE_FromServer:
                if (Files.exists(storagePath)) {
                    Files.delete(storagePath);
                    System.out.println("Файл " + storagePath + " удален");
                } else {
                    System.out.println("Файлa " + storagePath + " нет");
                }
                break;
            case Comand.RENAME_FILE_FromServer:
                String fileNewName = msg.split(" ")[1]; //byte_msgLenght_msg(в виде fileOld.txt_fileNew.txt)
                Path storagePathNew = Paths.get(storage,nick, fileNewName);
                Files.move(storagePath, storagePathNew);
                break;
            case Comand.DOWNLOAD_FILE_TO_CLIENT:
                if (Files.exists(storagePath)) {
                   protoFileSender.sendFile(storagePath, null);
                }
                break;
        }
    }
    private void resetState () {
        currentState = State.IDLE;
        msgLenght = 0;
        //receivedMsgLenght = 0L;
    }
}
