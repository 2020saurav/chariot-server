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
 * Ashva will periodically send heartbeats to Prashti
 * The two (if at all) prashtis will use their Zookeeper to check on each other (ping-echo)
 */
public class Heartbeat {

    private String ipAddr;
    private String timeOfBeat; // will use it to compare against time when it actually reaches
    private String logs;

    public String getTimeOfBeat() {
        return timeOfBeat;
    }

    public String getIpAddr() {
        return ipAddr;
    }

    public String getLogs() {
        return logs;
    }

    public Heartbeat(String ipAddr, String timeOfBeat, String logs) {
        this.ipAddr = ipAddr;
        this.timeOfBeat = timeOfBeat;
        this.logs = logs;
    }

    public static Heartbeat buildHeartBeat(String logs) {
        String timeOfBeat = ((Long) System.currentTimeMillis()).toString();
        String ipAddr = CommonUtils.getIPAddress();
        return new Heartbeat(ipAddr, timeOfBeat, logs);
    }
}
