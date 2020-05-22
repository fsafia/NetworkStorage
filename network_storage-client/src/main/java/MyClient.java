import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class MyClient {
    public Channel currentChannel;

    public void start() {
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(new OutgoingMessageHandler(), new IncomingMessageHandler());
                    currentChannel = socketChannel;
                }
            });

            // Start the client.
            ChannelFuture f = b.connect("localhost", 8189).sync();   // (4)

            f.channel().closeFuture().sync();

        } catch ( InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
        }

    }

    public static void main(String[] args) {
        MyClient myClient = new MyClient();
        myClient.start();
    }
}
