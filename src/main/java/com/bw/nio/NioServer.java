package com.bw.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class NioServer {
    public static void main(String[] args) throws IOException {
        eventDriver();
    }

    private static void eventDriver() throws IOException {

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        Selector selector = Selector.open();

        serverSocketChannel.socket().bind(new InetSocketAddress(6666));

        serverSocketChannel.configureBlocking(false);

        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT); //channel注册到selector 关注OP_ACCEPT事件

        //等待客户端连接
        while (true) {
            if (selector.select(1000) == 0) { //没有事件发生
                System.out.println("server wait...");
                continue;
            }

            Set<SelectionKey> selectionKeys = selector.selectedKeys();//有关注事件发生
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            if (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                if (selectionKey.isAcceptable()) {//ready to accept a new socket connection.

                    SocketChannel socketChannel = serverSocketChannel.accept();//block method

                    socketChannel.configureBlocking(false);

                    socketChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));//socketChannel注册到selector，并分配buffer

                    System.out.println("客户端连接成功" + socketChannel.hashCode());
                }

                if (selectionKey.isReadable()) { //读事件

                    SocketChannel channel = (SocketChannel) selectionKey.channel();//获取到selectionKey对应的channel

                    ByteBuffer buffer = (ByteBuffer) selectionKey.attachment();

                    int index = channel.read(buffer);

                    System.out.println("from client " + new String(buffer.array(), 0, index));
                }
                iterator.remove();
            }

        }
    }


}
