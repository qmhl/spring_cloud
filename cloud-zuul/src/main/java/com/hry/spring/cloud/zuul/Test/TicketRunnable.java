package com.hry.spring.cloud.zuul.Test;

        import org.springframework.util.StopWatch;

        import java.util.concurrent.*;

/**
 *   基于线程池实现的多线程卖票demo
 *   joey li
 *   2018-4-12
 *   qq: 1914295136
 */
public class TicketRunnable implements Runnable {
    private CountDownLatch count;
    private CyclicBarrier barrier;

    public TicketRunnable(CountDownLatch count,CyclicBarrier barrier) {
        this.count = count;
        this.barrier = barrier;
    }

    private int num = 10000;  // 总票数
    Object lock = new Object();

    public void sellTicket() {
        synchronized (lock) {
            if (num > 0) {
                System.out.print(Thread.currentThread().getName() + "售出票号" + num);
                num--;
                if(num!=0)
                    System.out.println(",还剩" + num + "张票--" );
                else
                    System.out.println("，票已经票完!--");
            }
        }
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName()+"到达,等待中...");
        try{
            barrier.await();    // 此处阻塞  等所有线程都到位后一起进行抢票
            if(Thread.currentThread().getName().equals("pool-1-thread-1")){
                System.out.println("---------------全部线程准备就绪,开始抢票----------------");
            }else {
                Thread.sleep(10);
            }
        }catch (Exception e){}
        while (num > 0) {
            sellTicket();
        }
        count.countDown();  //当前线程结束后，计数器-1
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        int threadNum = 100;    //模拟多个窗口 进行售票
        final CyclicBarrier barrier = new CyclicBarrier(threadNum);
        final CountDownLatch count = new CountDownLatch(threadNum);  // 用于统计 执行时长
        StopWatch watch = new StopWatch();
        watch.start();
        TicketRunnable tickets = new TicketRunnable(count,barrier);
        //建立100个线程
        ExecutorService executorService = Executors.newFixedThreadPool(threadNum);
        //ExecutorService executorService = Executors.newCachedThreadPool();
        //执行100个线程
        for (int i = 0; i < threadNum; i++) {   //此处 设置数值  受限于 线程池中的数量
            executorService.submit(tickets);
        }
        try {
            //CountDownLatch的await（） 当countDown()数为0  执行下面的代码
            count.await();
            executorService.shutdown();
            watch.stop();
            System.out.println("耗 时:" + watch.getTotalTimeSeconds() + "秒");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

