package network.storage.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import javafx.application.Platform;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ProtocolFile {

    public enum State {IDLE, NAME_LENGHT, NAME, FILE_LENGHT, FILE}

    public State currentState = State.IDLE;
    private int nameLenght;
    private long fileLenght;
    private long receivedFileLenght;
    private BufferedOutputStream out ;
    private Path newFile;
    private String storage;

    public ProtocolFile(String storage) {
        this.storage = storage;
    }

    public void writeFile(ChannelHandlerContext ctx, ByteBuf buf, String nik, Runnable finishOperation, Callback onReceivedCallback) throws Exception{

        if (currentState == State.NAME_LENGHT) {
            if (buf.readableBytes() >= 4) {  // считывает int
                nameLenght = buf.readInt();
                currentState = State.NAME;
            }
        }

        if (currentState == State.NAME) {
            if (buf.readableBytes() >= nameLenght) {
                byte [] fileName = new byte[nameLenght];
                buf.readBytes(fileName);
                String name = new String(fileName, "UTF-8");//String в виде 1.txt
                Path storagePath = getPathOnStorage(name, nik);  //Path в виде "1server-storage/nick/1.txt"
                if (Files.exists(storagePath)) {
                    Files.delete(storagePath);
                }
                createFile(storagePath, nik);  //создан пустой файл с названием 1.txt
            }
        }

        if (currentState == State.FILE_LENGHT) {
            if (buf.readableBytes() >= 8) {
                fileLenght = buf.readLong();
                currentState = State.FILE;
            }
        }

        if (currentState == State.FILE) {
            while (buf.readableBytes() > 0) {
                out.write(buf.readByte());
                receivedFileLenght++;
                if (fileLenght == receivedFileLenght) {
                    System.out.println("File received");
                    out.close();
                    resetState();
                    finishOperation.run();
                    if (nik == null) {  //файл для записи клиенту
                        Platform.runLater(() -> {
                            onReceivedCallback.callback();
                        });
                    } else {  //файл для записи серверу
                        onReceivedCallback.callback();
                    }
                    return;
                }
            }
            out.flush();
        }
    }

    private void createFile(Path storagePath, String nick) throws Exception {
        if (nick == null) { //для получения файла на клиенте, ник не нужен
            nick = "";
        }
        if (!Files.exists(Paths.get(storage,nick))) {
            Files.createDirectory(Paths.get(storage,nick));
        }
        newFile = Files.createFile(storagePath);
        currentState = State.FILE_LENGHT;
        out = new BufferedOutputStream(new FileOutputStream( newFile.toString()));
    }

    private Path getPathOnStorage(String fn, String nick) {
        if (nick == null) { //для получения файла на клиенте, ник не нужен
            nick = "";
        }
        Path path = Paths.get(fn); // получили Path  в виде network_storage-client/1.txt
        path = path.getFileName(); //получили Path  имя файла 1.txt
        return  Paths.get(storage,nick, path.toString());

//        Path serverPath;
//        return (nick == null)? Paths.get(storage, path.toString()): Paths.get(storage,nick, path.toString());
//        if (nick == null) {
//            serverPath = Paths.get(storage, path.toString());
//        } else {
//            serverPath = Paths.get(storage,nick, path.toString());
//        }
//        return serverPath;
    }

    private void resetState() {
        currentState = State.IDLE;
        nameLenght = 0;
        fileLenght = 0L;
        receivedFileLenght = 0L;
//        FileOutputStream out = null; //
        newFile = null;
    }

}
