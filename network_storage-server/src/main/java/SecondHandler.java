import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.Arrays;

public class SecondHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // точно знаем что предыдущий обработчик отдает нам массив байт
        byte[] arr = (byte[])msg;
        //каждый элемент массива увеличиваем на 1
        for (int i = 0; i < 3; i++) {
            arr[i] ++;
        }
        System.out.println("Второй шагж: " + Arrays.toString(arr));
        //кидаем полученный массив дальше по конвееру
        ctx.fireChannelRead(arr);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
