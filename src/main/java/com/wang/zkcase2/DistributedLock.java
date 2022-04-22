package com.wang.zkcase2;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class DistributedLock {

    private final ZooKeeper zk;
    private final String connectString = "hadoop102:2181,hadoop103:2181,hadoop104:2181";
    private final int sessionTimeout = 2000;
    private String waitPath;
    private String currentNode;
    //zookeeper连接
    private CountDownLatch connectLatch = new CountDownLatch(1);
    //zookeeper节点等待
    private CountDownLatch waitLatch = new CountDownLatch(1);

    //获取连接
    public DistributedLock() throws IOException, InterruptedException, KeeperException {
        zk = new ZooKeeper(connectString, sessionTimeout, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
                    connectLatch.countDown();
                }
                if (watchedEvent.getType() == Event.EventType.NodeDeleted && watchedEvent.getPath().equals(waitPath)) {
                    waitLatch.countDown();
                }
            }
        });

        //等待zk正常连接后往下走程序
        connectLatch.await();

        //判断根节点/locks是否存在
        Stat stat = zk.exists("/locks", false);
        if (stat == null) {
            //创建根节点
            zk.create("/locks", "locks".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
    }

    //对zk加锁
    public void zkLock(){
        try {
            //创建对应的临时带序号节点
            currentNode = zk.create("/locks" + "seq-", null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

            //判断创建的节点是否是最小的序号节点，若是获取到锁否则监听其序号前的一个节点
            List<String> children = zk.getChildren("/locks", false);

            //如果children只有一个值，直接获取锁，若多个节点，需要判断谁最小
            if (children.size() == 1) {
                return;
            } else {
                Collections.sort(children);
                //获取节点名称 seq-000000000
                String thisNode = currentNode.substring("/locks/".length());
                //获得该节点在children集合中的位置
                int index = children.indexOf(thisNode);
                if (index == -1) {
                    System.out.println("数据异常");
                } else if (index == 0) {
                    // index == 0, 说明 thisNode 在列表中最小, 当前client 获得锁
                    return;
                } else {
                    // 获得排名比 currentNode 前 1 位的节点
                    this.waitPath = "/locks/" + children.get(index - 1);
                    // 在 waitPath 上注册监听器, 当 waitPath 被删除时,zookeeper 会回调监听器的 process 方法
                    zk.getData(waitPath, true, new Stat());
                    //进入等待锁的状态
                    waitLatch.await();
                    return;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }

    }

    //解锁方法
    public void zkUnlock() {
        try {
            zk.delete(this.currentNode,-1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }
}
