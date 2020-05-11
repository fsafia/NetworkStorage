import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class FirstHahdler extends ChannelInboundHandlerAdapter {
    public State currentState = State.IDLE;
    private int nextLenght;
    private long fileLenght;
    private long receivedFileLenght;
    private FileOutputStream out ;
    private Path newFile;
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        while (buf.readableBytes() > 0) {
            if(currentState == State.IDLE) {
                byte readed = buf.readByte();
                if (readed == (byte) 1) {   // 1-команда для записи файла на сервер
                    currentState = State.NAME_LENGHT;
                    receivedFileLenght = 0L;
                    System.out.println("STATE: Start file receiving");
                } else {
                    System.out.println("другую команду"); /////////////////////////////////////////////
                }
            }

            if (currentState == State.NAME_LENGHT) {
                getFileNameLenght(buf);
//                if (buf.readableBytes() >= 4) {  // ?????????????почему цыфра 4?
//                    System.out.println("STATE: GET filename lenght");
//                    nextLenght = buf.readInt();
//                    currentState = State.NAME;
//                }
            }

            if (currentState == State.NAME) {

                if (buf.readableBytes() >=  nextLenght) {
//                    byte [] fileName = new byte[nextLenght];
//                    buf.readBytes(fileName);
                    String fn = getFileName(buf);
                    createFile(fn);
//
//                    Path path = Paths.get(fn); // получили Path  в виде network_storage-client/1.txt
//                    path = path.getFileName(); //получили Path  имя файла 1.txt
//                    Path s = Paths.get("network_storage-server/repository/" + path.toString());
//                    if (Files.exists(s)) { //--------------добавить проверку на существование файла и директории
//                        Files.delete(s);
//                    }
//
//                    newFile = Files.createFile(s);
//
//                    System.out.println("STATE Filename received - " + path);
//                    currentState = State.FILE_LENGHT;
//
//                    out = new FileOutputStream(newFile.toString(), true);
//
                }
            }

            if (currentState == State.FILE_LENGHT) {
                getFileLenght(buf);
//                if (buf.readableBytes() >= 8) {
//                    fileLenght = buf.readLong();
//                    System.out.println("STATE: File lenght received - " + fileLenght);
//                    currentState = State.FILE;
//                }
            }

            if (currentState == State.FILE) {
                writeFile(buf);
//                while (buf.readableBytes() > 0) {
//                    out.write(buf.readByte());
//                    //Files.write(newFile, buf.readByte());
//                    receivedFileLenght++;
//                    if (fileLenght == receivedFileLenght) {
//                        System.out.println("File received");
//                        out.close();
//                        break;
//                    }
//                }
            }
        }

        if (buf.readableBytes() == 0) {
            buf.release();
        }
    }

    private void getFileNameLenght(ByteBuf buf){
        if (buf.readableBytes() >= 4) {  // ?????????????почему цыфра 4?
            System.out.println("STATE: GET filename lenght");
            nextLenght = buf.readInt();
            currentState = State.NAME;
        }
    }

    private String getFileName(ByteBuf buf) throws Exception {
        byte [] fileName = new byte[nextLenght];
        buf.readBytes(fileName);
        String fn = new String(fileName, "UTF-8");
        return fn;
    }

    private void createFile(String fn) throws Exception {
        Path path = Paths.get(fn); // получили Path  в виде network_storage-client/1.txt
        path = path.getFileName(); //получили Path  имя файла 1.txt
        Path s = Paths.get("network_storage-server/repository/" + path.toString());
        if (Files.exists(s)) { //--------------добавить проверку на существование файла и директории
            Files.delete(s);
        }

        newFile = Files.createFile(s);
        System.out.println("STATE Filename received - " + path);
        currentState = State.FILE_LENGHT;
        out = new FileOutputStream(newFile.toString(), true);
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
            //Files.write(newFile, buf.readByte());
            receivedFileLenght++;
            if (fileLenght == receivedFileLenght) {
                System.out.println("File received");
                out.close();
                reverseState();
                break;
            }
        }
    }

    private void reverseState() {
        currentState = State.IDLE;
        nextLenght = 0;
        fileLenght = 0L;
        receivedFileLenght = 0L;
        FileOutputStream out = null;
        newFile = null;
    }
    //    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        // т.к. этот хэндлер стоит 1 от сети, то 100%  получит ByteBuf
//        ByteBuf buf = (ByteBuf) msg;
//        // ждем получения 3 байn
//        if ( buf.readableBytes() <3) {
//            return;
//        }
//        // как только получили 3 байта, готовим массив, чтобы их туда закинуть
//        byte[] data = new byte[3];
//        // перекидываем байты из буфера в массив
//        buf.readBytes(data);
//        // освобождаем буфер
//        buf.release();
//        //распечатываем что за массив у нас получился
//        System.out.println(Arrays.toString(data));
//        // прокидываем байт массив дальше по конвееру
//        ctx.fireChannelRead(data);
//    }
    public enum State {IDLE, NAME_LENGHT, NAME, FILE_LENGHT, FILE}

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
