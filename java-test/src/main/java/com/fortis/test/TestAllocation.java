package com.fortis.test;

public class TestAllocation {

    public interface A{
        public void a();
    }
    public interface B{
        public void b();
    }
    public interface C extends A,B{
        public void c();
    }

    public class D implements C{

        @Override
        public void a() {

        }

        @Override
        public void b() {

        }

        @Override
        public void c() {

        }
    }

    private static final int _1MB = 1024 * 1024;
    //-verbose:gc -Xms20M -Xmx20M -Xmn10M -XX:+PrintGCDetails -XX:SurvivorRatio=8
    public static void main(String[] args) {
        byte[] bytes1 = new byte[2 * _1MB];
        byte[] bytes2 = new byte[2 * _1MB];
        byte[] bytes3 = new byte[2 * _1MB];
        byte[] bytes4 = new byte[4 * _1MB];
        System.out.println(bytes1.length+bytes2.length+bytes3.length+bytes4.length);
    }
}
