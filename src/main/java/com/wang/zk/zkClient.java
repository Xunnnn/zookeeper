package com.wang.zk;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class zkClient {
    private String connectString = "hadoop102:2181,hadoop103:2181,hadoop104:2181";
    private int sessionTimeout = 2000;
    private ZooKeeper zkClient = null;

    @Test
    public void init() throws IOException {
        zkClient = new ZooKeeper(connectString, sessionTimeout, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                // 收到事件通知后的回调函数（用户的业务逻辑）
//                System.out.println(watchedEvent.getType() + "--" + watchedEvent.getPath());
//                try {
//                    List<String> children = zkClient.getChildren("/", true);
//                    for (String child : children) {
//                        System.out.println(child);
//                    }
//                } catch (KeeperException e) {
//                    e.printStackTrace();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            }
        });
    }

    /**
     * 创建子节点
     * PERSISTENT永久的
     *
     * @throws InterruptedException
     * @throws KeeperException
     */
    @Test
    public void create() throws InterruptedException, KeeperException {
        String nodeCreated = zkClient.create("/wang", "wang".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    }

    /**
     * 获取子节点
     *
     * @throws InterruptedException
     * @throws KeeperException
     */
    @Test
    public void getChildren() throws InterruptedException, KeeperException {
        List<String> children = zkClient.getChildren("/", true);
        for (String child : children) {
            System.out.println(child);
        }
        //延时阻塞，为了初始化方法不结束，监听器一直生效
        Thread.sleep(Long.MAX_VALUE);
    }

    /**
     * 判断znode是否存在
     *
     * @throws InterruptedException
     * @throws KeeperException
     */
    @Test
    public void exist() throws InterruptedException, KeeperException {
        Stat stat = zkClient.exists("/wang", false);
        System.out.println(stat == null ? "not exist" : "exist");
    }
}
