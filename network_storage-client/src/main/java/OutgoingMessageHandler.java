import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import java.nio.file.Path;
import java.nio.file.Paths;

public class OutgoingMessageHandler extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        String fileNameString = (String) msg;  //"1client-storage/1.txt"
        Path fileNamePath = Paths.get(fileNameString).getFileName();
        ProtoFileSender pfs = new ProtoFileSender(ctx, "1client-storage/");
        Path fileNewName = Paths.get("5.txt");
//        pfs.sendFile(Comand.DELETE_FILE_FromClient, fileNamePath, null);
//        pfs.sendFile(Comand.RENAME_FILE_FromServer, fileNamePath,  fileNewName, future -> {
                pfs.sendFile(Comand.WRITE_FILE, fileNamePath, future -> {
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
