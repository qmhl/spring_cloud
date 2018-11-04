package com.hry.spring.cloud.zuul.Test;

public class SaleTickets implements Runnable
{
    private int ticketCount = 10000;// 总的票数，这个是共享资源，多个线程都会访问
    Object mutex = new Object();// 锁，自己定义的，或者使用实例的锁
    /**
     * 卖票
     */
    public void sellTicket()
    {
        synchronized (mutex)// 当操作的是共享数据时,
        // 用同步代码块进行包围起来,执行里面的代码需要mutex的锁，但是mutex只有一个锁。这样在执行时,只能有一个线程执行同步代码块里面的内容
        {
            if (ticketCount > 9000)
            {
                ticketCount--;
                System.out.println(Thread.currentThread().getName()
                        + "正在卖票,还剩" + ticketCount + "张票");
            }

        }
    }
    public void run()
    {
        while (ticketCount > 0)// 循环是指线程不停的去卖票
        {
            sellTicket();
            /**
             * 在同步代码块里面睡觉,和不睡效果是一样 的,作用只是自已不执行,也不让线程执行。sleep不释放锁，抱着锁睡觉。其他线程拿不到锁，也不能执行同步代码。wait()可以释放锁
             * 所以把睡觉放到同步代码块的外面,这样卖完一张票就睡一会,让其他线程再卖,这样所有的线程都可以卖票
             */
            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }
}
