package com.example.asminject;

/**
 * @author AlexisYin
 */
public class InjectTest {
    @ASMTest
    public static void main(String[] args) throws InterruptedException {
        Thread.sleep(1_000);
        /*long start = System.currentTimeMillis();
        Thread.sleep(1_000);
        long end = System.currentTimeMillis();
        System.out.println("execute:" + (end-start)+" ms.");*/
    }

    void b(){}
}
