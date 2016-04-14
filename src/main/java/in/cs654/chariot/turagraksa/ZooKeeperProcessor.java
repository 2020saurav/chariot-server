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

import java.util.List;

/**
 * This class contains method process to handle requests sent to ZooKeeperServer
 *
 * Following requests are handled here:
 *
 * HEARTBEAT : Ashva sends this request to the ZooKeeperServer. This message contains ipAddr, timeOfBeat and logs in it
 *             The heartbeat is updated in Mongo collection called heartbeat. This is used in finding alive ashvas
 *
 * JOIN_POOL : Ashva sends this request to the ZooKeeperServer. This is sent when an ashva comes online and is ready
 *             to take requests to process. In this message, ashva sends a list of devices which are currently
 *             installed on it. This is used to find difference between Prashti's and Ashva's list. Any new device on
 *             Prashti is sent for installation at the ashva. This assumes that Prashti has complete and latest list
 *             of devices. This assumption wont harm because eventually ZooKeeper will keep syncing the device
 *             collection across all ashvas (including the ones which are both ashva and prashti).
 */
public class ZooKeeperProcessor {
    public static BasicResponse process(BasicRequest request) {

        if (request.getFunctionName().equals(ReservedFunctions.HEARTBEAT.toString())) {
            final String ipAddr = request.getExtraData().get("ipAddr");
            final String timeOfBeat = request.getExtraData().get("timeOfBeat");
            final String logs = request.getExtraData().get("logs");
            final Heartbeat heartbeat = new Heartbeat(ipAddr, timeOfBeat, logs);
            Mongo.updateHeartbeat(heartbeat);
            return ResponseFactory.getEmptyResponse(request);

        } else if (request.getFunctionName().equals(ReservedFunctions.JOIN_POOL.toString())) {
            final String ipAddr = request.getDeviceId();
            final AshvaClient client = new AshvaClient(ipAddr);
            final String serializedString = request.getExtraData().get("devicesString");
            final List<Device> devicesOnAshva = Device.deserializeDeviceListString(serializedString);
            final List<Device> devices = Mongo.getAllDevices();
            devices.removeAll(devicesOnAshva);
            for (Device d : devices) {
                final BasicRequest installRequest = RequestFactory.getInstallRequest(d);
                client.call(installRequest);
            }
            return ResponseFactory.getEmptyResponse(request);

        } else {
            return ResponseFactory.getErrorResponse(request);
        }
    }
}
