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

import in.cs654.chariot.avro.BasicRequest;

import java.util.*;

/**
 * This class contains functions to build requests required by several other components.
 */
public class RequestFactory {

    /**
     * This function is to be used by devices to build setup request
     * @param device containing id and docker image
     * @return request body
     */
    public static BasicRequest getSetupRequest(Device device) {
        final Map<String, String> map = new HashMap<String, String>();
        map.put("dockerImage", device.getDockerImage());
        return BasicRequest.newBuilder()
                .setDeviceId(device.getId())
                .setFunctionName(ReservedFunctions.DEVICE_SETUP.toString())
                .setRequestId(CommonUtils.randomString(32))
                .setArguments(new ArrayList<String>())
                .setExtraData(map)
                .build();
    }

    /**
     * This function is to be used by Prashti and Zookeeper to tell ashvas to install
     * the said device.
     * @param device to be installed
     * @return request object corresponding to this
     */
    public static BasicRequest getInstallRequest(Device device) {
        final Map<String, String> map = new HashMap<String, String>();
        map.put("dockerImage", device.getDockerImage());
        return BasicRequest.newBuilder()
                .setDeviceId(device.getId())
                .setFunctionName(ReservedFunctions.DEVICE_INSTALL.toString())
                .setRequestId(CommonUtils.randomString(32))
                .setArguments(new ArrayList<String>())
                .setExtraData(map)
                .build();
    }

    /**
     * This function is used to build request for heartbeat
     * @param heartbeat to be put in the request object
     * @return request object
     */
    public static BasicRequest getHeartbeatRequest(Heartbeat heartbeat) {
        final Map<String, String> map = new HashMap<String, String>();
        map.put("ipAddr", heartbeat.getIpAddr());
        map.put("timeOfBeat", heartbeat.getTimeOfBeat());
        map.put("logs", heartbeat.getLogs());
        return BasicRequest.newBuilder()
                .setDeviceId(heartbeat.getIpAddr())
                .setFunctionName(ReservedFunctions.HEARTBEAT.toString())
                .setRequestId(CommonUtils.randomString(32))
                .setArguments(new ArrayList<String>())
                .setExtraData(map)
                .build();
    }

    /**
     * This function is used to build request for syncing heartbeat among Zookeepers
     * @param heartbeat whose request is to be built
     * @return request object
     */
    public static BasicRequest getHeartbeatSyncRequest(Heartbeat heartbeat) {
        final Map<String, String> map = new HashMap<String, String>();
        map.put("ipAddr", heartbeat.getIpAddr());
        map.put("timeOfBeat", heartbeat.getTimeOfBeat());
        map.put("logs", heartbeat.getLogs());
        return BasicRequest.newBuilder()
                .setDeviceId(heartbeat.getIpAddr())
                .setFunctionName(ReservedFunctions.HB_SYNC.toString())
                .setRequestId(CommonUtils.randomString(32))
                .setArguments(new ArrayList<String>())
                .setExtraData(map)
                .build();
    }

    /**
     * This function is to build request for ashva to be sent to Zookeeper for ashva to be
     * added to the chariot pool. It packs in the list of devices present on the machine.
     * @return request object
     */
    public static BasicRequest getPoolJoinRequest() {
        final List<Device> devices = Mongo.getAllDevices();
        final String serializedString = Device.serializeDeviceList(devices);
        final Map<String, String> map = new HashMap<String, String>();
        map.put("devicesString", serializedString);
        return BasicRequest.newBuilder()
                .setDeviceId(CommonUtils.getIPAddress())
                .setFunctionName(ReservedFunctions.JOIN_POOL.toString())
                .setRequestId(CommonUtils.randomString(32))
                .setArguments(new ArrayList<String>())
                .setExtraData(map)
                .build();
    }

    /**
     * This function is to build request to ask an ashva to start its prashti and zookeeper servers
     * @return request object
     */
    public static BasicRequest getBecomePrashti2Request() {
        return BasicRequest.newBuilder()
                .setDeviceId(CommonUtils.getIPAddress())
                .setFunctionName(ReservedFunctions.BECOME_PRASHTI2.toString())
                .setRequestId(CommonUtils.randomString(32))
                .setArguments(new ArrayList<String>())
                .setExtraData(new HashMap<String, String>())
                .build();
    }

    /**
     * This is used to build ping request
     * @return request object
     */
    public static BasicRequest getPingRequest() {
        return BasicRequest.newBuilder()
                .setDeviceId(CommonUtils.getIPAddress())
                .setFunctionName(ReservedFunctions.PING.toString())
                .setRequestId(CommonUtils.randomString(32))
                .setArguments(new ArrayList<String>())
                .setExtraData(new HashMap<String, String>())
                .build();
    }
}
