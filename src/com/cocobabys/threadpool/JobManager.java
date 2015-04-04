package com.cocobabys.threadpool;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import android.util.Log;

public class JobManager{
    private Map<String, MyJob>    map           = new HashMap<String, MyJob>();
    private BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<String>(100);
    private Future<?>             submit;

    public JobManager(){}

    public void start(){
        final ScheduledExecutorService genericService = MyThreadPoolMgr.getGenericService();
        submit = genericService.submit(new Runnable(){

            @Override
            public void run(){
                while(!genericService.isShutdown()){
                    try{
                        String jobid = blockingQueue.take();
                        MyJob myJob = map.get(jobid);

                        if(myJob != null){
                            Future<?> futrue = genericService.submit(myJob);
                            futrue.get();
                        }

                        synchronized(JobManager.this){
                            map.remove(jobid);
                        }
                    } catch(InterruptedException e){
                        e.printStackTrace();
                        Log.d("addJob", "InterruptedException stop loop!");
                        // 捕获中断异常，终止任务
                        break;
                    } catch(ExecutionException e){
                        // 捕获执行异常，继续执行下一个任务
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public synchronized void addJob(String jobID, MyJob job){
        if(map.containsKey(jobID)){
            Log.d("addJob", "addJob already add jobID=" + jobID);
            return;
        }

        map.put(jobID, job);

        blockingQueue.offer(jobID);
    }

    public void stopTask(){
        if(submit != null){
            submit.cancel(true);
        }
    }

}
