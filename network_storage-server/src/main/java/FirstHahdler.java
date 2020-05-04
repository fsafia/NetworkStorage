import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.Arrays;

public class FirstHahdler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // т.к. этот хэндлер стоит 1 от сети, то 100%  получит ByteBuf
        ByteBuf buf = (ByteBuf) msg;
        // ждем получения 3 байт
        if (buf.readableBytes() < 3) {
            return;
        }
        // как только получили 3 байта, готовим массив, чтобы их туда закинуть
        byte[] data = new byte[3];
        // перекидываем байты из буфера в массив
        buf.readBytes(data);
        // освобождаем буфер
        buf.release();
        //распечатываем что за массив у нас получился
        System.out.println(Arrays.toString(data));
        // прокидываем байт массив дальше по конвееру
        ctx.fireChannelRead(data);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
