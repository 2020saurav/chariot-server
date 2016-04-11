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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RequestFactory {

    public static BasicRequest getSetupRequest(Device device) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("dockerImage", device.getDockerImage());
        return BasicRequest.newBuilder()
                .setDeviceId(device.getId())
                .setFunctionName(ReservedFunctions.DEVICE_SETUP.toString())
                .setRequestId(CommonUtils.randomString(32))
                .setArguments(new ArrayList<String>())
                .setExtraData(map)
                .build();
    }

    public static BasicRequest getInstallRequest(Device device) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("dockerImage", device.getDockerImage());
        return BasicRequest.newBuilder()
                .setDeviceId(device.getId())
                .setFunctionName(ReservedFunctions.DEVICE_INSTALL.toString())
                .setRequestId(CommonUtils.randomString(32))
                .setArguments(new ArrayList<String>())
                .setExtraData(map)
                .build();
    }

    public static BasicRequest getHeartbeatRequest(Heartbeat heartbeat) {
        Map<String, String> map = new HashMap<String, String>();
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
}
