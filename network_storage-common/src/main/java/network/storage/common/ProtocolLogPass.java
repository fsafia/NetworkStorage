package network.storage.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ProtocolLogPass {

    public enum State {IDLE, MSG_LENGHT, MSG, NAME, FILE_LENGHT, FILE}

    public State currentState = State.IDLE;
    private int msgLenght;
    public byte comand;
    public String msgString;
    public String log;
    public String pass;
    public String nick;

    public void executeComand(ChannelHandlerContext ctx, ByteBuf buf) throws Exception{

        while (buf.isReadable()) {
            if(currentState == State.IDLE) {
                comand = buf.readByte();
                if (Comand.TRY_TO_AUTH.getNumberComand() == comand                   // 10-команда для авторизации
                    || Comand.TRY_TO_SIGNUP.getNumberComand() == comand           // 11-команда для регистрации
                    || Comand.AUTH_NOT_OK.getNumberComand() == comand            // 13-команда неудачная регистрация
                    || Comand.AUTH_OK.getNumberComand() == comand) {           // 12-регистрация прошла успешно

                    currentState = State.MSG_LENGHT;
                    log = null;
                    pass = null;
                    System.out.println("STATE: Start log and pass receiving");
                } else {
                    System.out.println("неверная команда");
                }
            }

            if (currentState == State.MSG_LENGHT) {
                getMsgLenght(buf);
            }

            if (currentState == State.MSG) {
                if (buf.readableBytes() >= msgLenght) {
                    msgString = getMsgString(buf); //String в виде "log1 pass1"
                }

                if (comand == Comand.TRY_TO_AUTH.getNumberComand()) {
                    log = msgString.split(" ")[0];
                    pass = msgString.split(" ")[1];
                    currentState = State.IDLE;
                }
                if (comand == Comand.TRY_TO_SIGNUP.getNumberComand()) {
                    log = msgString.split(" ")[0];
                    pass = msgString.split(" ")[1];
                    nick = msgString.split(" ")[2];
                    currentState = State.IDLE;

                }

                if (comand == Comand.AUTH_NOT_OK.getNumberComand()) {
                    currentState = State.IDLE;
                }

                if (comand == Comand.AUTH_OK.getNumberComand()) {
                    currentState = State.IDLE;
                }

//                    if (Comand.WRITE_FILE.getNumberComand() == comand ) {
//                        if (Files.exists(storagePath)) { //--------------добавить проверку на существование файла и директории
//                            Files.delete(storagePath);
//                        }
//                        createFile(storagePath);  //создан пустой файл с названием 1.txt
//                    }
//
//                    if (Comand.DELETE_FILE_FromServer.getNumberComand() == comand) {
//                        if (Files.exists(storagePath)) { //--------------добавить проверку на существование файла и директории
//                            Files.delete(storagePath);
//                            System.out.println("Файл " + storagePath + " удален");
//                        } else {
//                            System.out.println("Файлa " + storagePath + " нет");
//                        }
//                        resetState();
//                    }
//
//                    if (Comand.RENAME_FILE_FromServer.getNumberComand() == comand ) {  // 351.txt55.txt -(3команда-5длина старИмени-1.тхт-5длинаНовИмени-5т.хт
//                        renameFile.add(storagePath);                                     //byteIntNameOldIntNameNew
//                        if (renameFile.size() == 1) {  //получили Path , renameFile(0)- это старое имя файла
//                            nextLenght = 0;
//                            currentState = State.MSG_LENGHT;
//                        }
//                        if (renameFile.size() == 2 && Files.exists(renameFile.get(0)) && !Files.exists(renameFile.get(1))) { //получили новый Path , renameFile(1)- это новое имя файла
//                            Files.move(renameFile.get(0), renameFile.get(1));       //если старый файл существует, а файла с новым именем не существует -- переименовываем
//                            resetState();
//                        }
//                    }
//                    if (Comand.DOWNLOAD_FILE_ToClient.getNumberComand() == comand) {
//                        ctx.write(storagePath);
//                        resetState();
//                    }
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
            currentState = State.MSG;
        }
    }

    private String getMsgString(ByteBuf buf) throws Exception {
        byte [] fileName = new byte[msgLenght];
        buf.readBytes(fileName);
        String msg = new String(fileName, "UTF-8");
        return msg;
    }

    private void resetState() {
        currentState = State.IDLE;
        comand = (byte) 0;
    }

}

