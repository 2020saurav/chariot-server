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

/**
 * This class contains helper functions for Ashva Server and Processor.
 */
class AshvaHelper {

    private static final Logger LOGGER = Logger.getLogger("Ashva");

    /**
     * This function queries D2Client for current online prashti servers.
     * If there is atleast one prashti server present, this ashva server requests the Zookeeper running
     * on any of the present prashti server to add it to the pool. This request also contains the list of
     * devices that are 'installed' (whose docker images are present) on the server.
     * If there isn't any prashti server present, this ashva server starts prashti server and zookeeper server
     * on the machine. Informing D2 is taken care of by the prashti server itself.
     */
    static void joinOrStartChariotPool() {
        final List<Prashti> prashtiList = D2Client.getOnlinePrashtiServers();
        if (prashtiList.size() != 0) {
            final ZooKeeperClient zooKeeperClient = new ZooKeeperClient();
            final BasicRequest request = RequestFactory.getPoolJoinRequest();
            zooKeeperClient.call(request);
            LOGGER.info("Prashti [" + prashtiList.get(0).getIpAddr() + "] exists. Joining pool.");
        } else {
            LOGGER.info("No Prashti Server found.");
            try {
                LOGGER.info("Starting Prashti and ZooKeeper Server");
                Runtime.getRuntime().exec("./chariot.sh");
            } catch (IOException ignore) {
                LOGGER.severe("Prashti & ZooKeeper Server initialization failed");
            }
        }
    }

    /**
     * This function runs on its separate thread and continuously sends heartbeat to the Zookeeper.
     * Either of the zookeeper will sync this information with the other one (if present).
     */
    static void startHeartbeat() {
        final Thread thread = new Thread() {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(ZooKeeper.HB_TIME_ASHVA);
                        final Heartbeat heartbeat = Heartbeat.buildHeartBeat("");
                        final ZooKeeperClient client = new ZooKeeperClient(); // Old ZooKeeper may have gone away
                        client.call(RequestFactory.getHeartbeatRequest(heartbeat));
                    } catch (InterruptedException ignore) {
                        LOGGER.severe("Error in heartbeat. Retrying..");
                        run();
                    }
                }
            }
        };
        thread.start();
    }
}
