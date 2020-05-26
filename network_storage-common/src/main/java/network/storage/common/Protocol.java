package network.storage.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import network.storage.common.Comand;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Protocol {

    public enum State {IDLE, NAME_LENGHT, NAME, FILE_LENGHT, FILE}

    public State currentState = State.IDLE;
    private int nextLenght;
    private long fileLenght;
    private long receivedFileLenght;
    private BufferedOutputStream out ;
    private Path newFile;
    private byte comand;
    ArrayList<Path> renameFile;
    private String storage;

    public Protocol(String storage) {
        this.storage = storage;
    }

    public void executeComand(ChannelHandlerContext ctx, ByteBuf buf, String nik) throws Exception{
        while (buf.isReadable()) {
            if(currentState == State.IDLE) {
                comand = buf.readByte();
                if (Comand.CLIENT_CLOSE == comand) { // 15- клиент отключился

                }
                if (Comand.WRITE_FILE == comand                   // 1-команда для записи файла на сервер
                    || Comand.DELETE_FILE_FromServer == comand    // 2- удаление с сервера
                    || Comand.RENAME_FILE_FromServer == comand    // 3- переименование
                    || Comand.DOWNLOAD_FILE_TO_CLIENT == comand ) {   // 4 - cкачивание файла

                    currentState = State.NAME_LENGHT;
                    receivedFileLenght = 0L;
                    renameFile = new ArrayList<>(2);
                    System.out.println("STATE: Start file receiving");
                } else {
                    System.out.println("другую команду");
                }
            }

            if (currentState == State.NAME_LENGHT) {
                getFileNameLenght(buf);
            }

            if (currentState == State.NAME) {
                if (buf.readableBytes() >= nextLenght) {
                    String name = getName(buf); //String в виде 1.txt

                    Path storagePath = getPathOnStorage(name);  //Path в виде "1server-storage/1.txt"

                    if (Comand.WRITE_FILE == comand ) {
                        if (Files.exists(storagePath)) { //--------------добавить проверку на существование файла и директории
                            Files.delete(storagePath);
                        }
                        createFile(storagePath);  //создан пустой файл с названием 1.txt
                    }

                    if (Comand.DELETE_FILE_FromServer == comand) {
                        if (Files.exists(storagePath)) { //--------------добавить проверку на существование файла и директории
                            Files.delete(storagePath);
                            System.out.println("Файл " + storagePath + " удален");
                        } else {
                            System.out.println("Файлa " + storagePath + " нет");
                        }
                        resetState();
                    }

                    if (Comand.RENAME_FILE_FromServer == comand ) {  // 351.txt55.txt -(3команда-5длина старИмени-1.тхт-5длинаНовИмени-5т.хт
                        renameFile.add(storagePath);                                     //byteIntNameOldIntNameNew
                        if (renameFile.size() == 1) {  //получили Path , renameFile(0)- это старое имя файла
                            nextLenght = 0;
                            currentState = State.NAME_LENGHT;
                        }
                        if (renameFile.size() == 2 && Files.exists(renameFile.get(0)) && !Files.exists(renameFile.get(1))) { //получили новый Path , renameFile(1)- это новое имя файла
                            Files.move(renameFile.get(0), renameFile.get(1));       //если старый файл существует, а файла с новым именем не существует -- переименовываем
                            resetState();
                        }
                    }
                    if (Comand.DOWNLOAD_FILE_TO_CLIENT == comand) {
                        ctx.write(storagePath);
                        resetState();
                    }
                }
            }

            if (currentState == State.FILE_LENGHT) {
                getFileLenght(buf);
            }

            if (currentState == State.FILE) {
                writeFile(buf);
            }
        }
        if (buf.readableBytes() == 0) {
            buf.release();
        }
    }

    private void getFileNameLenght(ByteBuf buf){
        if (buf.readableBytes() >= 4) {  // считывает long
            System.out.println("STATE: GET filename lenght");
            nextLenght = buf.readInt();
            currentState = State.NAME;
        }
    }

    private String getName(ByteBuf buf) throws Exception {
        byte [] fileName = new byte[nextLenght];
        buf.readBytes(fileName);
        String fn = new String(fileName, "UTF-8");
        return fn;
    }

    private void createFile(Path s) throws Exception {
        newFile = Files.createFile(s);
        System.out.println("STATE Filename received - " + s.getFileName());
        currentState = State.FILE_LENGHT;
        out = new BufferedOutputStream(new FileOutputStream( newFile.toString()));
    }

    private Path getPathOnStorage(String fn) {
        Path path = Paths.get(fn); // получили Path  в виде network_storage-client/1.txt
        path = path.getFileName(); //получили Path  имя файла 1.txt
        Path serverPath = Paths.get(storage + path.toString());
        return serverPath;
    }

    private void getFileLenght(ByteBuf buf) {
        if (buf.readableBytes() >= 8) {
            fileLenght = buf.readLong();
            System.out.println("STATE: File lenght received - " + fileLenght);
            currentState = State.FILE;
        }
    }

    private void writeFile(ByteBuf buf) throws Exception {
        while (buf.readableBytes() > 0) {
            out.write(buf.readByte());
            receivedFileLenght++;
            if (fileLenght == receivedFileLenght) {
                System.out.println("File received");
                out.close();
                resetState();
                break; //return;
            }
        }
        out.flush();
    }

    private void resetState() {
        currentState = State.IDLE;
        nextLenght = 0;
        fileLenght = 0L;
        receivedFileLenght = 0L;
        FileOutputStream out = null;
        newFile = null;
        comand = (byte) 0;
        renameFile = null;
    }

}

