package com.hry.spring.cloud.zuul.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CyclicBarrier;

public class ThreadTest {
    //需要处理的订单数量,
    private static int MAX_ORDERS=10000;
    //最大线程数
    private  static  int MAX_THREADS =100;
    //每个线程睡眠时间(订单处理时间)
    private static  int SLEEP =10;
    //写复制list,支持并发修改和遍历功能,需要注意内存耗费的问题，可以采用ConcurrentHashMap等代替
    private static  List<Order> list=new CopyOnWriteArrayList<>();//new CopyOnWriteArrayList<>();//new ArrayList<>();
    static {//初始化测试数据
        for(int i=0;i<MAX_ORDERS;i++)
            list.add(new Order("HHHH"+i));
    }
    public  static  void main(String ss[]){
        /**
         * 记录开始时间，理论上耗时略大于MAX_ORDERS×SLEEP/MAX_THREADS
         */
        Long t=System.currentTimeMillis();
        //同步屏障,以是住线程能等待所有线程结束，并记录下耗时
        final CyclicBarrier cyclicBarrier=new CyclicBarrier(MAX_THREADS+1);
        for(int i = 0; i< MAX_THREADS; i++){
            //创建并启动订单处理线程
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!list.isEmpty()) {
                        Order order = null;
                        //生成唯一加锁令牌，保证唯一性即可
                        String uuid = UUID.randomUUID().toString();
                        int i = 0;//记录查找到未处理的订单位置
                        for (Order item : list) {
                            i++;
                            if (item.getLock(uuid)) {//找到并锁定订单
                                order = item;
                                break;
                            }
                        }
                        if (order != null) {//获取到可处理的订单
                            System.out.println(i + ":" + Thread.currentThread().getId() + "正在处理订单：" + order.getApplyNo());
                            try {
                                Thread.sleep(SLEEP);//模拟订单处理功能
                                list.remove(order);//处理成功后将订单重待处理列表中移除
                            } catch (Exception e) {
                                e.printStackTrace();
                            }finally {
                                /**
                                 * 无论成功还是失败，都需要解锁订单
                                 * 失败情况下订单依旧留在待处理列表中,供其他线程处理
                                 * 可以根据实际情况将处理失败的订单转移到别的地方进行处理
                                 * 否则会导致坏订单影响后面正常订单的处理
                                 */
                                order.unLock(uuid);//解锁订单
                            }

                        }

                    }
                    try {
                        cyclicBarrier.await();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

            }).start();
        }
        try{
            //等待所有线程执行完成
            cyclicBarrier.await();
        }catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("*******订单处理完成，耗时："+(System.currentTimeMillis()-t)+"ms");

    }

    static  class  Order{
        private String applyNo;

        public  Order(String applyNo){
            this.applyNo=applyNo;
        }
        public String getApplyNo() {
            return applyNo;
        }
        private    boolean  lock=true;
        //用来检验加锁和解锁的是否为同一线程
        private  String token="";

        /**
         * 加锁，先判断是否已被锁住，未被锁住则获取该对象锁
         * @param token
         * @return
         */
        public boolean getLock(String token){
            if(lock){
                synchronized (this){
                    if(lock){//双重检测
                        lock=false;
                        this.token=token;
                        return true;
                    }else{
                        System.out.println("被别人抢走了！！！！！！！！！！");
                    }

                }

            }
            return lock;
        }

        /**
         * 解锁,通过token限定只有获取到锁的线程才能解锁,否则为非法调用
         * @param newToken
         */
        public void unLock(String newToken){
            if(!newToken.equals(this.token)||lock){
                System.out.println("出错了！！！！！！！！！！");
            }else{
                lock=true;
            }
        }

    }
}
