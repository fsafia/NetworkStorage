import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ProtoFileSender {
    private  String pathStringToFile;
    private  ChannelHandlerContext ctx;
    ProtoFileSender(ChannelHandlerContext ctx, String pathStringToFile) {
        this.pathStringToFile = pathStringToFile;
        this.ctx = ctx;
    }

    public void sendFile(Comand comand, Path storagePath, ChannelFutureListener finishListener) throws IOException {
        Path p = storagePath.getFileName();//было 1srver-storage/1.txt стало 1.txt
//        String pathString = pathStringToFile + storagePath.toString(); // путь в client-storage/
//        Path pathFile = Paths.get(pathString);
//        System.out.println(pathFile);

        if (comand == Comand.DELETE_FILE_FromClient && Files.exists(pathFile)) {
            Files.delete(pathFile);
            return;
        }
        byte[] filenameBytes = storagePath.getFileName().toString().getBytes(StandardCharsets.UTF_8);
        ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(1 + 4 + filenameBytes.length );
        buf.writeByte(comand.getNumberComand());
        buf.writeInt(filenameBytes.length);
        buf.writeBytes(filenameBytes);

        switch (comand) {
            case WRITE_FILE:
                ctx.write(buf);
                buf = ByteBufAllocator.DEFAULT.directBuffer(8);
                buf.writeLong(Files.size(pathFile));
                ctx.writeAndFlush(buf);
                FileRegion region = new DefaultFileRegion(pathFile.toFile(), 0, Files.size(pathFile));
                ChannelFuture transferOperationFuture = ctx.writeAndFlush(region);
                if (finishListener != null) {
                    transferOperationFuture.addListener(finishListener);
                }
                break;
            case DELETE_FILE_FromServer:
                ctx.writeAndFlush(buf);
                break;
            case DELETE_FILE_FromClient:

        }
    }

    public void sendFile(Comand comand, Path pathOld, Path pathNew, ChannelFutureListener finishListener) throws IOException {
        switch (comand) {
            case RENAME_FILE_ToClient:
                String pathOldString = pathStringToFile + pathOld.toString(); // путь в client-storage/1.txt
                Path pathOldhFile = Paths.get(pathOldString);

                String pathNewString = pathStringToFile + pathNew.toString(); // путь в client-storage/1.txt
                Path pathNewhFile = Paths.get(pathNewString);
                if (Files.exists(pathOldhFile)) {
                    if (!Files.exists(pathNewhFile)) {
                        Files.move(pathOldhFile, pathNewhFile);
                    } else {
                        System.out.println("Файл не переименован, файл с  новым названием уже существует. ");
                    }
                } else {
                    System.out.println("Файл не переименован, файла для переименования не существует. ");
                }
                break;
            case RENAME_FILE_FromServer:
                pathOld = pathOld.getFileName();
                pathNew = pathNew.getFileName();
                byte[] oldFileNameBytes = pathOld.toString().getBytes(StandardCharsets.UTF_8);
                byte[] newFileNameBytes = pathNew.toString().getBytes(StandardCharsets.UTF_8);
                ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(1 + 4 + oldFileNameBytes.length + 4 +  newFileNameBytes.length);
                buf.writeByte(comand.getNumberComand());
                buf.writeInt(oldFileNameBytes.length);
                buf.writeBytes(oldFileNameBytes);
                buf.writeInt(newFileNameBytes.length);
                buf.writeBytes(newFileNameBytes);
                ctx.writeAndFlush(buf);
        }
    }
}

//не сделала папку client-storage