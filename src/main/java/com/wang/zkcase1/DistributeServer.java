package com.wang.zkcase1;

import org.apache.zookeeper.*;

import java.io.IOException;

public class DistributeServer {

    private String connectString = "hadoop102:2181,hadoop103:2181,hadoop104:2181";
    private int sessionTimeout = 2000;
    private ZooKeeper zk = null;
    private String parentNode = "/servers";

    public static void main(String[] args) throws Exception {
        DistributeServer server = new DistributeServer();
        //1.获取zk连接
        server.getConnect();
        //2.利用zk连接注册服务器信息
        server.registServer(args[0]);
        //3.启动业务功能
        server.business(args[0]);

    }

    private void business(String hostname) throws InterruptedException {
        System.out.println(hostname + " is working");
        Thread.sleep(Long.MAX_VALUE);
    }

    private void registServer(String hostname) throws InterruptedException, KeeperException {
        String create = zk.create(parentNode + "/server", hostname.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        System.out.println(hostname + " is online" + create);
    }

    private void getConnect() throws IOException {
        zk = new ZooKeeper(connectString, sessionTimeout, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {

            }
        });
    }
}
