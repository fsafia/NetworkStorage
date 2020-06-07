package network.storage.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;

public class Network {
    public IncomingMessageHandler incomingMessageHandler;
    private Channel currentChannel;

    public Network(Controller c) {
        incomingMessageHandler = new IncomingMessageHandler(c);
    }

    public Channel getCurrentChannel() {
        return currentChannel;
    }

    public  void start(CountDownLatch countDownLatch) {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.remoteAddress(new InetSocketAddress("localhost", 8189));
            b.handler(new ChannelInitializer<SocketChannel>() {
                public void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(incomingMessageHandler);
                    currentChannel = socketChannel;
                }
            });

            // Start the client.
            ChannelFuture f = b.connect().sync();
            countDownLatch.countDown();
            f.channel().closeFuture().sync();

        } catch ( InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}
