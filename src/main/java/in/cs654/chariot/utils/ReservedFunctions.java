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
 * DEVICE_SETUP : New devices send (id, dockerImageName) to Prashti to register themselves.
 *                Prashti server receives this request and forwards DEVICE_INSTALL request to all servers, including
 *                all Ashva and Prashti.
 * ASHVA_JOIN   : When an ashva comes up (all servers can become Ashva and Prashti at the same time), it requests the
 *                current Prashti servers to make it a member of the pool. Prashti will add this Ashva to the pool.
 *                Selection of Prashti will be done based on some strategy TBD
 * DEVICE_INSTALL : Prashti sends this request to all servers to install a device, by adding it's information in their
 *                  database and docker pull the specified docker image
 * HEARTBEAT    : All servers send this request to Prashti at some interval of time.
 *
 */
public enum ReservedFunctions {
    DEVICE_SETUP ("chariotDeviceSetup"),
    ASHVA_JOIN ("chariotAshvaJoin"),
    DEVICE_INSTALL ("chariotDeviceInstall"),
    HEARTBEAT ("chariotHeartbeat");

    private final String name;

    private ReservedFunctions(String s) {
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
