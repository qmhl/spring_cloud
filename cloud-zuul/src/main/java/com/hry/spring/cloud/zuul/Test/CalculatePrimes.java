package com.hry.spring.cloud.zuul.Test;

public class CalculatePrimes extends Thread {
            //使用两个线程，一个用于计时(线程会休眠20秒然后设置一个主线程要检查的标志finished)，
            //一个用于执行实际工作。在执行实际工作的线程启动前启动计时线程。

            //达到20秒钟主线程将停止。
            public static final int SECONDS = 10000;

            public volatile boolean finished = false;

            public void run() {
        System.out.println("开始执行实际工作啦");
        for (long i = 0l; i < Long.MAX_VALUE; i++) {
            if(finished) {
                break;

            }
            if(i%100000000==0) {
                System.out.println("i:"+Thread.currentThread().getName()+"----"+i);

            }

        }
        System.out.println("结束执行实际工作啦");

    }

            public static void main(String[] args) {
        //通过实例化CalculatePrimes类型的对象来创建线程。
        CalculatePrimes calculator = new CalculatePrimes();
        calculator.start();
        try {
            System.out.println("执行Thread.sleep前，时间为："+System.currentTimeMillis());
            Thread.sleep(SECONDS);
            System.out.println("执行Thread.sleep后，时间为："+System.currentTimeMillis());

        } catch(InterruptedException e) {


        }
        calculator.finished = true;
        System.out.println(Thread.currentThread().getName()+"---false转为true");
        System.out.println("执行完main()");

    }
}