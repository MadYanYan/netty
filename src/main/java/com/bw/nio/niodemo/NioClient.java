package com.bw.nio.niodemo;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class NioClient {
    public static void main(String[] args) throws IOException {

        SocketChannel socketChannel = SocketChannel.open();

        socketChannel.configureBlocking(false);

        //提供ip和端口
        InetSocketAddress inetSocketAddress = new InetSocketAddress("127.0.0.1", 6666);

        if (!socketChannel.connect(inetSocketAddress)) {
            while (!socketChannel.finishConnect()) {
                System.out.println("");
            }
        }
        String str = "hello world!";
        ByteBuffer buffer = ByteBuffer.wrap(str.getBytes());
//发送数据 将buffer中数据写入channel
        socketChannel.write(buffer);

        System.in.read();
    }
}
