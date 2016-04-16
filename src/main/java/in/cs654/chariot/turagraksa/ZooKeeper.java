/*
 * Copyright (c) 2016 Abhilash Kumar and Saurav Kumar.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package in.cs654.chariot.turagraksa;

import in.cs654.chariot.avro.BasicRequest;
import in.cs654.chariot.avro.BasicResponse;
import in.cs654.chariot.utils.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * Zookeeper class has functions for maintaining quality attribute in the system.
 * It has functions to sync database states with other zookeeper, to check availability of other zookeeper using
 * ping-echo tactic, reset zookeeper client when other is down etc
 */
public class ZooKeeper {
    public static final Long HB_TIME_THRESHOLD = 30000L; // milliseconds
    public static final Long HB_TIME_ASHVA = 15000L;
    private static final Long PING_ECHO_DURATION = 10000L;
    private static final Long ZK_PING_TIMEOUT = 40000L;
    private static ZooKeeperClient otherZooKeeperClient = null;
    private final static Logger LOGGER = Logger.getLogger("ZooKeeper");

    /**
     * This function is used to sync heartbeat with other zookeeper
     * @param heartbeat to be synced
     */
    static void notifyOtherZooKeeperServer(Heartbeat heartbeat) {
        if (otherZooKeeperClient != null) {
            final BasicRequest request = RequestFactory.getHeartbeatSyncRequest(heartbeat);
            otherZooKeeperClient.call(request);
        }
    }

    /**
     * This function runs in a separate thread, in an infinite loop. It checks availability of other zookeeper.
     * In case the zookeeper goes down (by extension the prashti too), it notifies the D2Client about this event
     * by updating the prashti list on D2 server (setting single prashti i.e itself). Then it initiates the function
     * to select a new prashti (and zookeeper) if available.
     */
    static void startPingEcho() {
        final Thread thread = new Thread() {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(PING_ECHO_DURATION);
                        if (otherZooKeeperClient != null) {
                            if (checkOtherZooKeeper()) {
                                LOGGER.info("Other Zookeeper is alive");
                            } else {
                                otherZooKeeperClient = null;
                                LOGGER.warning("Other Zookeeper is down");
                                // inform D2 that I am the alone Prashti/Zookeeper server
                                final List<Prashti> prashtiList = new ArrayList<Prashti>();
                                prashtiList.add(new Prashti(CommonUtils.getIPAddress()));
                                D2Client.setPrashtiServers(prashtiList);
                                selectANewPrashti();
                            }
                        }
                    } catch (InterruptedException ignore) {
                        LOGGER.severe("Error in Ping-Echo. Retrying..");
                        run();
                    }
                }
            }
        };
        thread.start();
    }

    private static boolean checkOtherZooKeeper() {
        final ExecutorService executorService = Executors.newCachedThreadPool();
        final Callable<BasicResponse> task = new Callable<BasicResponse>() {
            @Override
            public BasicResponse call() throws Exception {
                return otherZooKeeperClient.call(RequestFactory.getPingRequest());
            }
        };
        final Future<BasicResponse> future = executorService.submit(task);
        try {
            final BasicResponse ignore = future.get(ZK_PING_TIMEOUT, TimeUnit.MILLISECONDS);
            return true;
        } catch (Exception ignore) {
            return false;
        }
    }

    /**
     * This function is called when signalled by D2 client that there is some change in D2 server state.
     * It sets the ZKClient corresponding to the updated zookeeper.
     */
    static void resetOtherZooKeeperClient() {
        List<Prashti> prashtiList = D2Client.getOnlinePrashtiServers();
        if (prashtiList.size() == 2) {
            if (!prashtiList.get(0).getIpAddr().equals(CommonUtils.getIPAddress())) {
                otherZooKeeperClient = new ZooKeeperClient(prashtiList.get(0).getIpAddr());
            } else {
                otherZooKeeperClient = new ZooKeeperClient(prashtiList.get(1).getIpAddr());
            }
            LOGGER.info("Resetting otherZooKeeperClient");
        } else {
            LOGGER.warning("Number of prashtis : " + prashtiList.size());
        }
    }

    /**
     * This function finds a possible candidate to become new prashti (and zookeeper).
     * This is done by obtaining the latest list of online prashti (hopefully, exactly one will be there)
     * Next, a list of online ashvas is queried and out of them, one is selected (one on top in query)
     * and the ashva is signalled to become the new prashti (second one)
     */
    private static void selectANewPrashti() {
        final List<Prashti> onlinePrashtiList = D2Client.getOnlinePrashtiServers();
        final List<Ashva> onlineAshvaList = Mongo.getAliveAshvaList();
        final List<String> candidateIPAddresses = new ArrayList<String>();
        for (Ashva ashva : onlineAshvaList) {
            candidateIPAddresses.add(ashva.getIpAddr());
        }
        for (Prashti prashti : onlinePrashtiList) {
            candidateIPAddresses.remove(prashti.getIpAddr());
        }
        if (candidateIPAddresses.size() > 0) {
            final AshvaClient client = new AshvaClient(candidateIPAddresses.get(0));
            final BasicRequest req = RequestFactory.getBecomePrashti2Request();
            client.call(req);
            LOGGER.info("Ashva (" + candidateIPAddresses.get(0) + ") requested to be another prashti");
        } else {
            LOGGER.warning("No possible candidate ashva can start prashti");
        }
    }
}
