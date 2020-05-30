package com.bw.nio.channel;

import java.nio.ByteBuffer;

public class TestChannel_03 {
    public static void main(String[] args) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(32);

        byteBuffer.putInt(10);
        byteBuffer.putChar('z');

        byteBuffer.flip();

        System.out.println(byteBuffer.getInt());
        //put放入的数据类型和get的数据类型要一致
        System.out.println(byteBuffer.getChar());
//        System.out.println(byteBuffer.getLong());
    }
}
