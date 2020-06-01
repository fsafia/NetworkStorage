package network.storage.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.FutureListener;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class ProtocolFile {

    public enum State {IDLE, NAME_LENGHT, NAME, FILE_LENGHT, FILE}

    private State currentState = State.NAME_LENGHT;
    private int nameLenght;
    private long fileLenght;
    private long receivedFileLenght;
    private BufferedOutputStream out ;
    private Path newFile;
    private String storage;

    public ProtocolFile(String storage) {
        this.storage = storage;
    }

    public void writeFile(ChannelHandlerContext ctx, ByteBuf buf, String nik, Runnable finishOperation) throws Exception{

            if (currentState == State.NAME_LENGHT) {
                if (buf.readableBytes() >= 4) {  // считывает int
                    System.out.println("STATE: GET filename lenght");
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
                    System.out.println("STATE: File lenght received - " + fileLenght);
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
                        return;
                    }
                }
                out.flush();
            }
    }

    private void createFile(Path storagePath, String nick) throws Exception {
        if (!Files.exists(Paths.get(storage,nick))) {
            Files.createDirectory(Paths.get(storage,nick));
        }
        newFile = Files.createFile(storagePath);
        System.out.println("STATE Filename received - " + storagePath.getFileName());
        currentState = State.FILE_LENGHT;
        out = new BufferedOutputStream(new FileOutputStream( newFile.toString()));
    }

    private Path getPathOnStorage(String fn, String nick) {
        Path path = Paths.get(fn); // получили Path  в виде network_storage-client/1.txt
        path = path.getFileName(); //получили Path  имя файла 1.txt
        Path serverPath = Paths.get(storage,nick, path.toString());
        return serverPath;
    }

//    private void writeFile(ByteBuf buf) throws Exception {
//        while (buf.readableBytes() > 0) {
//            out.write(buf.readByte());
//            receivedFileLenght++;
//            if (fileLenght == receivedFileLenght) {
//                System.out.println("File received");
//                out.close();
//                resetState();
//                break; //return;
//            }
//        }
//        out.flush();
//    }

    private void resetState() {
        currentState = State.NAME_LENGHT;
        nameLenght = 0;
        fileLenght = 0L;
        receivedFileLenght = 0L;
        FileOutputStream out = null;
        newFile = null;
    }

}

