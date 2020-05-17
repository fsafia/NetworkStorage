import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class FirstHahdler extends ChannelInboundHandlerAdapter {
    public State currentState = State.IDLE;
    private int nextLenght;
    private long fileLenght;
    private long receivedFileLenght;
    private FileOutputStream out ;
    private Path newFile;
    private byte readed;
    ArrayList<Path> renameFile;
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        while (buf.readableBytes() > 0) {
            if(currentState == State.IDLE) {
                readed = buf.readByte();
                if (readed == (byte)1 ||readed == (byte)2 ||readed == (byte)3)  {   // 1-команда для записи файла на сервер
                    currentState = State.NAME_LENGHT;                               // 2- удаление с сервера
                    receivedFileLenght = 0L;                                        // 3- переименование
                    renameFile = new ArrayList<>(2);                   // 4 - cкачивание файла
                    System.out.println("STATE: Start file receiving");
                } else {
                    System.out.println("другую команду"); /////////////////////////////////////////////
                }
            }

            if (currentState == State.NAME_LENGHT) {
                getFileNameLenght(buf);
//                if (buf.readableBytes() >= 4) {  // считывает long
//                    System.out.println("STATE: GET filename lenght");
//                    nextLenght = buf.readInt();
//                    currentState = State.NAME;
//                }
            }

            if (currentState == State.NAME) {
                if (buf.readableBytes() >= nextLenght) {
//                    byte [] fileName = new byte[nextLenght];
//                    buf.readBytes(fileName);
                    String fn = getName(buf);

                    Path serverPath = getPathOnServer(fn);
//                    Path path = Paths.get(fn); // получили Path  в виде network_storage-client/1.txt
//                    path = path.getFileName(); //получили Path  имя файла 1.txt
//                    Path serverPath = Paths.get("network_storage-server/repository/" + path.toString());

                    if (readed == 1 ) {
                        if (Files.exists(serverPath)) { //--------------добавить проверку на существование файла и директории
                            Files.delete(serverPath);
                        }
                        createFile(serverPath);
                    }

                    if (readed == 2 ) {
                        Files.delete(serverPath);
                        resetState();
                    }

                    if (readed == 3) {  //переименование файла ожидаем команду 3 в виде 3(команда)число(длина стар имиени)1.тхтчисло(длина нового имени)3.тхт(1.тхт в 3.тхт)
                        renameFile.add(serverPath);                                     //byteIntNameOldIntNameNew
                        if (renameFile.size() == 1) {  //получили Path , renameFile(0)- это старое имя файла
                            nextLenght = 0;
                            currentState = State.NAME_LENGHT;
                        }
                        if (renameFile.size() == 2 && Files.exists(renameFile.get(0)) && !Files.exists(renameFile.get(1))) { //получили новый Path , renameFile(1)- это новое имя файла
                            Files.copy(renameFile.get(0), renameFile.get(1));       //если старый файл существует, а файла с новым именем не существует -- переименовываем
                            // заменить на Files.move(renameFile.get(0), renameFile.get(1));  - это перемещение с заменой имени
                            Files.delete(renameFile.get(0));
                            resetState();
                        }
//                        if (!Files.exists(renameFile.get(0))){
//                            System.out.println("file " + renameFile.get(0).toString() + " not exist ");
//                        } else if (Files.exists(renameFile.get(1))) {
//                            System.out.println(renameFile.get(1).toString() + " file already exists " );
//                        }
                    }
                    if (readed == 4) {
                        ctx.write(serverPath);
                    }
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
        out = new FileOutputStream(newFile.toString(), true);
    }

    private Path getPathOnServer(String fn) {
        Path path = Paths.get(fn); // получили Path  в виде network_storage-client/1.txt
        path = path.getFileName(); //получили Path  имя файла 1.txt
        Path s = Paths.get("network_storage-server/repository/" + path.toString());
        return s;
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
                resetState();
                break;
            }
        }
    }

    private void resetState() {
        currentState = State.IDLE;
        nextLenght = 0;
        fileLenght = 0L;
        receivedFileLenght = 0L;
        FileOutputStream out = null;
        newFile = null;
        readed = (byte) 0;
        renameFile = null;
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
