package com.example.ant.threadtest;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Timer;
import java.util.TimerTask;


public class MyActivity extends ActionBarActivity {
    String TAG = "ThreadTest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Thread不销毁，导致多个Tread,同时存在
        test1();

        // test handler.postdelay(), 会阻塞UI吗，开新的线程了吗
//        test2();

        //timer test
//        test3();
    }
    Thread thread;
    public void test1(){
        thread = new Thread(new MyThread());
        thread.start();
        //Thread[Thread-146,5,main]
        //不取消，最终导致，多个线程同时在跑，问题在于他们都会更新UI
        //使用stop，异常终止，UnsupportedOperationException
        //interrupt，无效
        //removeCallbacks，无效
    }
    public void test2(){
        log("post before");
        handler.postDelayed(runnable, 2000);//每两秒执行一次runnable.
        log("post start");
        //不錯，不会阻塞，可以通过removeCallbacks随时取消终止，
        //那延时到底是怎么做到的呢，是开了子线程吗
        //通过查看handler源码，所有的sendmessage方法（包括postdelayed），最终都调用sendMessageAtTime，然后。。。
        //http://blog.csdn.net/lilu_leo/article/details/8143205（源码分析示例）
    }
    Timer timer;
    public void test3(){
        timer = new Timer();
        TimerTask task = new MyTimertask();
        timer.schedule(task,2000,2000);
        //Thread[timer-0,5,main]
        //结果，注意如果没有取消，可能会异常终止，task只能付给一个timer一次！！！！！
        //timer,在timer线程执行task
        //结果，取消，又可能会异常终止，timer在cancle后，不能再执行schedule
        //结果又异常终止，timer在cancle后，task还是不能给其他timer，
        //最终，新的timer，新的task终于ok了，每次都会开一个不同的线程，所以cancle以后应该销毁ba??
    }


    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            // 要做的事情
            super.handleMessage(msg);
            log("handler UI");
        }
    };

   class MyTimertask extends TimerTask {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            log(Thread.currentThread()+"run");
            Message message = new Message();
            message.what = 1;
            handler.sendMessage(message);
        }
    };

    Runnable runnable=new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            log(Thread.currentThread()+"run");
            handler.postDelayed(this, 5000);
            handler.postDelayed(this, 2000);
            log("post start");
        }
    };

    public class MyThread implements Runnable {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            while (true) {
                try {
                    log(Thread.currentThread()+"run");
                    Thread.sleep(3000);
                    Message message = new Message();
                    message.what = 1;

                    handler.sendMessage(message);// 发送消息
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    private void log(String s){
        Log.v(TAG,s);
    }

    @Override
    protected void onPause() {
        super.onPause();
        log("onpause");

        //test1
//        handler.removeCallbacks(thread);
//        thread.stop();
//        thread.interrupt();

        //test2
//        handler.removeCallbacks(runnable);

        //test3
//        timer.cancel();
    }

    @Override
    protected void onStop() {
        super.onStop();
        log("onstop");
    }


}
