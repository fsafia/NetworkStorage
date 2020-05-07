import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;

public class FirstHahdler extends ChannelInboundHandlerAdapter {
    public State currentState = State.IDLE;
    private int nextLenght;
    private long fileLenght;
    private long receivedFileLenght;
    private FileOutputStream out ;
    private File newFile;
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        while (buf.readableBytes() > 0) {
            System.out.println("пришло байт -" + buf.readableBytes());
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
                if (buf.readableBytes() >= 4) {  // ?????????????почему цыфра 4?
                    System.out.println("STATE: GET filename lenght");
                    nextLenght = buf.readInt();
                    System.out.println(nextLenght);
                    currentState = State.NAME;
                }
            }

            if (currentState == State.NAME) {
                if (buf.readableBytes() >=  nextLenght) {
                    System.out.println("-------------------------");
                    byte [] fileName = new byte[nextLenght];
                    buf.readBytes(fileName);
                    System.out.println("STATE Filename received - _" + new String(fileName, "UTF-8"));
                    currentState = State.FILE_LENGHT;
                    //сама писала//////////////////////////////////
                    String pathName = "repository/" + new String(fileName, "UTF-8");
                    newFile = new File(pathName);
                    newFile.mkdirs();
                    out = new FileOutputStream(pathName, true);

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
                        break;
                    }
                }
            }
        }

        if (buf.readableBytes() == 0) {
            buf.release();
        }
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
