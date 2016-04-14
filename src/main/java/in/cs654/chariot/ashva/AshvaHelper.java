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

package in.cs654.chariot.ashva;

import in.cs654.chariot.avro.BasicRequest;
import in.cs654.chariot.turagraksa.ZooKeeper;
import in.cs654.chariot.utils.*;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class AshvaHelper {

    private static final Logger LOGGER = Logger.getLogger("Ashva");

    public static void joinOrStartChariotPool() {
        final List<Prashti> prashtiList = D2Client.getOnlinePrashtiServers();
        if (prashtiList.size() != 0) {
            final ZooKeeperClient zooKeeperClient = new ZooKeeperClient();
            final BasicRequest request = RequestFactory.getPoolJoinRequest();
            zooKeeperClient.call(request);
            LOGGER.info("Prashti [" + prashtiList.get(0).getIpAddr() + "] exists. Joining pool.");
        } else {
            // TODO locking D2 to avoid race condition
            LOGGER.info("No Prashti Server found.");
            try {
                LOGGER.info("Starting Prashti Server");
                LOGGER.info("Starting ZooKeeper Server");
                Runtime.getRuntime().exec("./chariot.sh");
            } catch (IOException e) {
                e.printStackTrace();
                LOGGER.severe("Prashti & ZooKeeper Server initialization failed");
            }
        }
    }

    public static void startHeartbeat() {
        final Thread thread = new Thread() {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(ZooKeeper.HB_TIME_ASHVA);
                        Heartbeat heartbeat = Heartbeat.buildHeartBeat("");
                        ZooKeeperClient client = new ZooKeeperClient(); // Old ZooKeeper may have gone away
                        client.call(RequestFactory.getHeartbeatRequest(heartbeat));
                    } catch (InterruptedException ignore) {
                    }
                }
            }
        };
        thread.start();
    }
}
