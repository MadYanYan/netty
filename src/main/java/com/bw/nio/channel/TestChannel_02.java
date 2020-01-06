package com.bw.nio.channel;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class TestChannel_02 {

    private static void writeByChannel() throws IOException {
        String str = "channel_test";

        FileOutputStream fileOutputStream = new FileOutputStream("/Users/yan/Documents/channel_test.txt");

        FileChannel fileChannel = fileOutputStream.getChannel();

        //创建缓冲
        ByteBuffer bf = ByteBuffer.allocate(1024);

        ByteBuffer byteBuffer = bf.put(str.getBytes());

        bf.flip();

        fileChannel.write(byteBuffer);

        fileOutputStream.close();
    }

    private static void readByChannel() throws IOException {
        File file = new File("/Users/yan/Documents/channel_test.txt");

        FileInputStream fileInputStream = new FileInputStream(file);

        FileChannel fileChannel = fileInputStream.getChannel();

        ByteBuffer bf = ByteBuffer.allocate((int) file.length());

        fileChannel.read(bf);

        System.out.println(new String(bf.array()));

        fileInputStream.close();
    }

    public static void main(String[] args) throws IOException {
//        writeByChannel();
        readByChannel();
    }


}
