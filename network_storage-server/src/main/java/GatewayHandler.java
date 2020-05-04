import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.Arrays;

public class GatewayHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        int sum = 0;
        byte[] arr = (byte[])msg;
        for (int i = 0; i < arr.length; i++) {
            sum += arr[i];
        }
        if (sum == 66) {
            ctx.fireChannelRead(arr);
        } else {
            System.out.println("Сообщение сломано " + Arrays.toString(arr));
            //отправляем в обратную сторону сообщение клиенту об ошибке
            ctx.writeAndFlush(" Битое сообщение \n");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
