package network.storage.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class ProtocolComand {
    public enum State {IDLE, MSG_LENGHT, MSG}

    public State currentState = State.IDLE;
    private int msgLenght;
    private long receivedMsgLenght;
    private byte comand;
    private String storage;
    private StringBuffer sb;
    private String nick;

    public void setNick(String nick) {
        this.nick = nick;
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
                    || Comand.DOWNLOAD_FILE_TO_CLIENT == comand) {  // 4 - cкачивание файла

                currentState = State.MSG_LENGHT;
                receivedMsgLenght = 0L;
                sb = new StringBuffer();
                System.out.println("STATE: Start file receiving");
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
            while (buf.readableBytes() > 0) {
                sb.append((char) buf.readByte()); // ?
                receivedMsgLenght++;
                if (msgLenght == receivedMsgLenght) {
                    defineCmd(comand, sb.toString(), ctx.channel());
                    resetState();
                    finishOperation.run();
                    return;
                }
            }
        }

//        if (currentState == State.NAME) {
//            if (buf.readableBytes() >= ) {
//                String name = getName(buf); //String в виде 1.txt
//
//                Path storagePath = getPathOnStorage(name);  //Path в виде "1server-storage/1.txt"
//
//                if (Comand.DELETE_FILE_FromServer == comand) {
//                    if (Files.exists(storagePath)) { //--------------добавить проверку на существование файла и директории
//                        Files.delete(storagePath);
//                        System.out.println("Файл " + storagePath + " удален");
//                    } else {
//                        System.out.println("Файлa " + storagePath + " нет");
//                    }
//                    resetState();
//                }
//
//                if (Comand.RENAME_FILE_FromServer == comand ) {  // 351.txt55.txt -(3команда-5длина старИмени-1.тхт-5длинаНовИмени-5т.хт
//                    renameFile.add(storagePath);                                     //byteIntNameOldIntNameNew
//                    if (renameFile.size() == 1) {  //получили Path , renameFile(0)- это старое имя файла
//                         = 0;
//                        currentState = State.NAME_LENGHT;
//                    }
//                    if (renameFile.size() == 2 && Files.exists(renameFile.get(0)) && !Files.exists(renameFile.get(1))) { //получили новый Path , renameFile(1)- это новое имя файла
//                        Files.move(renameFile.get(0), renameFile.get(1));       //если старый файл существует, а файла с новым именем не существует -- переименовываем
//                        resetState();
//                    }
//                }
//                if (Comand.DOWNLOAD_FILE_TO_CLIENT == comand) {
//                    ctx.write(storagePath);
//                    resetState();
//                }
//            }
//        }
//
//        if (currentState == State.FILE_LENGHT) {
//            getFileLenght(buf);
//        }
//
//        if (currentState == State.FILE) {
//            writeFile(buf);
//        }
//    }

    }
    public void defineCmd (byte comand, String sb, Channel channel) throws IOException {
        ProtoFileSender protoFileSender = new ProtoFileSender(channel);
        String fileName = sb.split(" ")[0];
        Path storagePath = Paths.get(storage + nick + fileName);
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
                String fileNewName = sb.split(" ")[1]; //byte_msgLenght_msg(в виде fileOld.txt_fileNew.txt)
                Path storagePathNew = Paths.get(storage + nick + fileNewName);
                Files.move(storagePath, storagePathNew);
                /////////////////////////////если файлы с такими именами не существуют???
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
        receivedMsgLenght = 0L;
        //comand = (byte) 0;
    }
}
