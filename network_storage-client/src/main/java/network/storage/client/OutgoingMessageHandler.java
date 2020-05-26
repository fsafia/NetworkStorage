package network.storage.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import network.storage.common.Comand;
import network.storage.common.ProtoFileSender;

import java.nio.file.Path;
import java.nio.file.Paths;

//public class OutgoingMessageHandler extends ChannelOutboundHandlerAdapter {
//
//    @Override
//    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
//        String loginAndPassString = "log1 pass1";
//        byte [] loginAndPassByte = loginAndPassString.getBytes();
//        ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(loginAndPassByte.length );
//        buf.writeBytes(loginAndPassByte);
//        ctx.write(buf);
//        String storageString = (String) msg;  //"1client-storage/1.txt"
//        Path storagePath = Paths.get(storageString);
//        ProtoFileSender pfs = new ProtoFileSender(ctx, "1client-storage/");
//
//        pfs.sendMsg(Comand.DOWNLOAD_FILE_ToClient, storagePath, future -> {
//            if (!future.isSuccess()) {
//                future.cause().printStackTrace();
//                System.out.println("Файл не передан");
////                Network.getInstance().stop();
//            }
//            if (future.isSuccess()) {
//                System.out.println("Файл успешно передан");
////                Network.getInstance().stop();
//            }
//        });
//    }
//
//
//}
