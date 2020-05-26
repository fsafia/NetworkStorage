package network.storage.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import network.storage.common.Comand;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ProtoFileSender {
//    private  String pathStringToFile;
//    private  ChannelHandlerContext ctx;
    private  Channel channel;
    public ProtoFileSender(Channel channel /*String pathStringToFile*/) {
//    public ProtoFileSender(ChannelHandlerContext ctx /*String pathStringToFile*/) {
//        this.pathStringToFile = pathStringToFile;
        this.channel = channel;
    }

//    public void sendMsg(Comand comand, Path storagePath, ChannelFutureListener finishListener) throws IOException {
//
//        if (comand == Comand.DELETE_FILE_FromClient && Files.exists(storagePath)) {
//            Files.delete(storagePath);
//            return;
//        }
//
//        byte[] filenameBytes = storagePath.getFileName().toString().getBytes(StandardCharsets.UTF_8);
//        ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(1 + 4 + filenameBytes.length );
//        buf.writeByte(comand.getNumberComand());
//        buf.writeInt(filenameBytes.length);
//        buf.writeBytes(filenameBytes);
//
//        switch (comand) {
//            case WRITE_FILE:
//                ctx.write(buf);
//                buf = ByteBufAllocator.DEFAULT.directBuffer(8);
//                buf.writeLong(Files.size(storagePath));
//                ctx.writeAndFlush(buf);
//                FileRegion region = new DefaultFileRegion(storagePath.toFile(), 0, Files.size(storagePath));
//                ChannelFuture transferOperationFuture = ctx.writeAndFlush(region);
//                if (finishListener != null) {
//                    transferOperationFuture.addListener(finishListener);
//                }
//                break;
//            case DELETE_FILE_FromServer:
//                ctx.writeAndFlush(buf);
//                break;
//            case DOWNLOAD_FILE_ToClient:
//                ctx.writeAndFlush(buf);
//                break;
//        }
//    }


    public void renaneFile(Comand comand, Path pathOld, Path pathNew, ChannelFutureListener finishListener) throws IOException {
        switch (comand) {
            case RENAME_FILE_ToClient:
//                String pathOldString = pathStringToFile + pathOld.toString(); // путь в client-storage/1.txt
//                Path pathOldhFile = Paths.get(pathOldString);
//                String pathNewString = pathStringToFile + pathNew.toString(); // путь в client-storage/1.txt
//                pathNew = Paths.get(pathStringToFile + pathNew.toString());
                if (Files.exists(pathOld)) {
                    if (!Files.exists(pathNew)) {
                        Files.move(pathOld, pathNew);
                        System.out.println("Файл  переименован");
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
 //               ctx.writeAndFlush(buf);
        }
    }

    ////////////////////////
    public void sendAuth(Comand comand, String str, ChannelFutureListener finishListener) throws IOException {
        byte[] filenameBytes = str.getBytes(StandardCharsets.UTF_8);
        ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(1 + 4 + filenameBytes.length );
        buf.writeByte(comand.getNumberComand());
        buf.writeInt(filenameBytes.length);
        buf.writeBytes(filenameBytes);
        channel.writeAndFlush(buf);
    }

//        if (comand == Comand.DELETE_FILE_FromClient && Files.exists(storagePath)) {
//            Files.delete(storagePath);
//            return;
//        }
//
//        byte[] filenameBytes = storagePath.getFileName().toString().getBytes(StandardCharsets.UTF_8);
//        ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(1 + 4 + filenameBytes.length );
//        buf.writeByte(comand.getNumberComand());
//        buf.writeInt(filenameBytes.length);
//        buf.writeBytes(filenameBytes);
//
//        switch (comand) {
//            case WRITE_FILE:
//                ctx.write(buf);
//                buf = ByteBufAllocator.DEFAULT.directBuffer(8);
//                buf.writeLong(Files.size(storagePath));
//                ctx.writeAndFlush(buf);
//                FileRegion region = new DefaultFileRegion(storagePath.toFile(), 0, Files.size(storagePath));
//                ChannelFuture transferOperationFuture = ctx.writeAndFlush(region);
//                if (finishListener != null) {
//                    transferOperationFuture.addListener(finishListener);
//                }
//                break;
//            case DELETE_FILE_FromServer:
//                ctx.writeAndFlush(buf);
//                break;
//            case DOWNLOAD_FILE_ToClient:
//                ctx.writeAndFlush(buf);
//                break;
//        }
//    }
}