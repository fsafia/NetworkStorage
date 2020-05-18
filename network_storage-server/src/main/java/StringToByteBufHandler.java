import io.netty.channel.*;

import java.nio.file.Path;

public class StringToByteBufHandler extends ChannelOutboundHandlerAdapter {
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        Path serverPath = (Path) msg; // 1client-storage/1.txt
        ProtoFileSender pfs = new ProtoFileSender(ctx, "1client-storage/");
        pfs.sendFile(Comand.WRITE_FILE, serverPath, future -> {
            if (!future.isSuccess()) {
                future.cause().printStackTrace();
                System.out.println("Файл не передан");
//                Network.getInstance().stop();
            }
            if (future.isSuccess()) {
                System.out.println("Файл успешно передан");
//                Network.getInstance().stop();
            }
        });
    }
}
