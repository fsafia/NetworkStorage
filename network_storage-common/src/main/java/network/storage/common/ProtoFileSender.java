package network.storage.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import network.storage.common.Comand;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static network.storage.common.Comand.WRITE_FILE;

public class ProtoFileSender {
    private  Channel channel;

    public Channel getChannel() {
        return channel;
    }
    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public void sendComand(byte comand, String str) throws IOException {
        if (str.equals("")) {
            str = "empty";
        }
        byte[] filenameBytes = str.getBytes(StandardCharsets.UTF_8);
        ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(1 + 4 + filenameBytes.length );
        buf.writeByte(comand);
        buf.writeInt(filenameBytes.length);
        buf.writeBytes(filenameBytes);
        channel.writeAndFlush(buf);
    }
//    public void sendClose(byte comand) {
//        ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(1);
//        buf.writeByte(comand);
//        channel.writeAndFlush(buf);
//    }
    public void sendFile(Path storagePath, ChannelFutureListener finishListener) throws IOException {
        FileRegion region = new DefaultFileRegion(storagePath.toFile(), 0, Files.size(storagePath));
        byte[] fileNameBytes = storagePath.getFileName().toString().getBytes(StandardCharsets.UTF_8);
        ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(1 + 4 + fileNameBytes.length + 8);
        buf.writeByte(WRITE_FILE);
        buf.writeInt(fileNameBytes.length);
        buf.writeBytes(fileNameBytes);
        buf.writeLong(Files.size(storagePath));
        channel.writeAndFlush(buf);
        ChannelFuture transferOperationFuture = channel.writeAndFlush(region);
    }
}