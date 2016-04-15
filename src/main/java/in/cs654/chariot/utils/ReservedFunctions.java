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

package in.cs654.chariot.utils;

/**
 * This enum lists the reserved functions in chariot framework
 * Reserved functions are handled differently than usual task processing which is done by Ashva servers.
 * Following is a brief explanation of all the enums:
 *
 * DEVICE_SETUP    : New devices send (id, dockerImageName) to Prashti to register themselves.
 *                   Prashti server receives this request and forwards DEVICE_INSTALL request to all Ashva servers
 *
 * DEVICE_INSTALL  : Prashti sends this request to all ashva servers to install a device, by adding it's information in
 *                   their database and docker pull the specified docker image
 *
 * HEARTBEAT       : All ashva servers send this request to Prashti at some interval of time.
 *
 * BECOME_PRASHTI2 : Existing (single) prashti's Zookeeper sends this to an ashva to start its prashti server and
 *                   become the second prashti server.
 *
 * JOIN_POOL       : Ashva sends this request to Zookeeper when it is ready to join the chariot pool. Along with this
 *                   request, Ashva packs in the list of devices which it has installed (i.e. their docker images
 *                   pulled in)
 *
 * HB_SYNC         : Zookeeper sends this message to another Zookeeper (if it is online). This is essentially
 *                   forwarding of the heartbeat sent by an Ashva
 *
 * PRASHTI_CHANGE  : D2Client sends
 *
 * PING            : D2Client sends PING to Prashti to check if it is up. ZooKeeper also sends PING to other ZK.
 *
 */
public enum ReservedFunctions {
    DEVICE_SETUP ("chariotDeviceSetup"),
    DEVICE_INSTALL ("chariotDeviceInstall"),
    HEARTBEAT ("chariotHeartbeat"),
    BECOME_PRASHTI2 ("chariotBecomePrashti2"),
    JOIN_POOL ("chariotJoinPool"),
    HB_SYNC ("chariotHBSync"),
    PRASHTI_CHANGE ("chariotPrashtiChange"),
    PING ("chariotPing");

    private final String name;

    ReservedFunctions(String s) {
        name = s;
    }

    public String toString() {
        return this.name;
    }

    public static boolean contains(String functionName) {
        for (ReservedFunctions r : ReservedFunctions.values()) {
            if (r.name.equals(functionName)) {
                return true;
            }
        }
        return false;
    }
}
