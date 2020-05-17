import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class OutgoingMessageHandler extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

        String pathString = (String) msg;
        Path pathPath = Paths.get(pathString);

        ByteBuf buf = null;
        buf = ByteBufAllocator.DEFAULT.directBuffer(1);
        buf.writeByte((byte) 2); //команда 2 на удаление файла с сервера
        ctx.writeAndFlush(buf);

        byte[] filenameBytes = pathPath.getFileName().toString().getBytes(StandardCharsets.UTF_8);
        //byte[] filenameBytes = pathString.getBytes(StandardCharsets.UTF_8);
        buf = ByteBufAllocator.DEFAULT.directBuffer(4);
        buf.writeInt(filenameBytes.length);
        ctx.writeAndFlush(buf);

        buf = ByteBufAllocator.DEFAULT.directBuffer(filenameBytes.length);
        buf.writeBytes(filenameBytes);
        ctx.writeAndFlush(buf);
    }


}
