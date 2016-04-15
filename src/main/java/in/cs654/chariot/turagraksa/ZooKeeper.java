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
import in.cs654.chariot.utils.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ZooKeeper {
    public static final Long HB_TIME_THRESHOLD = 30000L; // milliseconds
    public static final Long HB_TIME_ASHVA = 15000L;
    public static final Long PING_ECHO_DURATION = 10000L;
    private static ZooKeeperClient otherZooKeeperClient = null;
    final static Logger LOGGER = Logger.getLogger("ZooKeeper");

    static void notifyOtherZooKeeperServer(Heartbeat heartbeat) {
        if (otherZooKeeperClient != null) {
            final BasicRequest request = RequestFactory.getHeartbeatSyncRequest(heartbeat);
            otherZooKeeperClient.call(request);
        }
    }

    static void startPingEcho() {
        final Thread thread = new Thread() {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(PING_ECHO_DURATION);
                        if (otherZooKeeperClient != null) {
                            try {
                                otherZooKeeperClient.call(RequestFactory.getPingRequest());
                                LOGGER.info("Other Zookeeper is alive");
                            } catch (Exception ignore) {
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
                        run();
                    }
                }
            }
        };
        thread.start();
    }

    // D2Client will call this
    static void resetOtherZooKeeperClient() {
        List<Prashti> prashtiList = D2Client.getOnlinePrashtiServers();
        if (prashtiList.size() == 2) {
            if (!prashtiList.get(0).getIpAddr().equals(CommonUtils.getIPAddress())) {
                otherZooKeeperClient = new ZooKeeperClient(prashtiList.get(0).getIpAddr());
            } else {
                otherZooKeeperClient = new ZooKeeperClient(prashtiList.get(1).getIpAddr());
            }
        }
    }

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
        }
    }
}
