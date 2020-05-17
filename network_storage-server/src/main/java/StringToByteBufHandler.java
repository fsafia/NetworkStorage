import io.netty.buffer.ByteBuf;
import io.netty.channel.*;

import java.nio.file.Files;
import java.nio.file.Path;

public class StringToByteBufHandler extends ChannelOutboundHandlerAdapter {
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        Path serverPath = (Path) msg;

        FileRegion region = new DefaultFileRegion(serverPath.toFile(), 0, Files.size(serverPath));
        ChannelFuture transferOperationFuture = ctx.writeAndFlush(region);
    }
}
