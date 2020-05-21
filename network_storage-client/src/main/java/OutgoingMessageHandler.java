import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import java.nio.file.Path;
import java.nio.file.Paths;

public class OutgoingMessageHandler extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        String storageString = (String) msg;  //"1client-storage/1.txt"
        Path storagePath = Paths.get(storageString);
        ProtoFileSender pfs = new ProtoFileSender(ctx, "1client-storage/");

        pfs.sendFile(Comand.DOWNLOAD_FILE_ToClient, storagePath, future -> {
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
       // pfs.renaneFile(Comand.RENAME_FILE_ToClient, storagePath, Paths.get("5.txt"), null);

    }


}
