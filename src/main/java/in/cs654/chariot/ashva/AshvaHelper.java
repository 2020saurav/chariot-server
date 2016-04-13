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
import in.cs654.chariot.utils.*;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class AshvaHelper {

    private static final Logger LOGGER = Logger.getLogger("Ashva");

    public static void joinOrStartChariotPool() {
        List<Prashti> prashtiList = D2Client.getPrashtiServers();
        if (prashtiList.size() != 0) {
            final ZooKeeperClient zooKeeperClient = new ZooKeeperClient();
            final String log = "Prashti [" + prashtiList.get(0).getIpAddr() + "] exists. Joining pool.";
            final Heartbeat heartbeat = Heartbeat.buildHeartBeat(log);
            final BasicRequest request = RequestFactory.getHeartbeatRequest(heartbeat);
            try {
                zooKeeperClient.call(request);
            } catch (Exception ignore) {
            }
            LOGGER.info(log);
        } else {
            // TODO 1. Test the following. 2. obtain lock from D2 to avoid race condition
            LOGGER.info("No Prashti Server found.");
            try {
                LOGGER.info("Starting Prashti Server");
                Process prashtiServerProcess = Runtime.getRuntime().exec("gradle -q rPS > pr.log 2> pr2.log");
                LOGGER.info("Starting ZooKeeper Server");
                Process zooKeeperServerProcess = Runtime.getRuntime().exec("gradle -q rZKS > zk.log 2> zk2.log");
                // TODO move the code below to PrashtiServer.
                String ipAddr = CommonUtils.getIPAddress();
                prashtiList.clear();
                prashtiList.add(new Prashti(ipAddr));
                LOGGER.info("Notifying D2 to set Prashti Server IP Address");
                D2Client.setPrashtiServers(prashtiList);
            } catch (IOException e) {
                LOGGER.severe("Prashti & ZooKeeper Server initialization failed");
            }
        }
    }
}