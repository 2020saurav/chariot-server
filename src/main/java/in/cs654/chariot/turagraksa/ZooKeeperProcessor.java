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
import java.util.logging.Logger;

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
 *             After that, it checks if there are less than 2 prashti servers online, it requests the new ashva to
 *             become the second prashti. First prashti trivially exists because this zookeeper is running on it.
 *
 * PRASHTI_CHANGE : D2Client sends this notification for Zookeeper to update it's otherZookeeperClient
 */
class ZooKeeperProcessor {
    private final static Logger LOGGER = Logger.getLogger("ZooKeeper Processor");
    /**
     * This function matches the request for reserved functions and accordingly handles them
     * @param request to be handled
     * @return response of the request
     */
    public static BasicResponse process(BasicRequest request) {

        if (request.getFunctionName().equals(ReservedFunctions.HEARTBEAT.toString())) {
            return handleHeartBeatRequest(request);

        } else if(request.getFunctionName().equals(ReservedFunctions.HB_SYNC.toString())) {
            return handleHeartBeatSyncRequest(request);

        } else if (request.getFunctionName().equals(ReservedFunctions.JOIN_POOL.toString())) {
            return handlePoolJoinRequest(request);

        } else if (request.getFunctionName().equals(ReservedFunctions.PRASHTI_CHANGE.toString())) {
            ZooKeeper.resetOtherZooKeeperClient();
            return ResponseFactory.getEmptyResponse(request);

        } else if (request.getFunctionName().equals(ReservedFunctions.PING.toString())) {
            return ResponseFactory.getEmptyResponse(request);

        } else {
            return ResponseFactory.getErrorResponse(request);
        }
    }

    /**
     * This function handles request of ashva which wishes to join the system.
     * The ashva also sends the list of devices installed on it. In case the ashva lacks some devices,
     * installation requests are sent to ashva for remaining devices.
     * And, then function to check if it can be brought up as a prashti is called.
     * @param request to be handled
     * @return empty response
     */
    private static BasicResponse handlePoolJoinRequest(BasicRequest request) {
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
        LOGGER.info("Ashva (" + ipAddr + ") joining Chariot Pool");
        checkAndBringUpNewPrashti(ipAddr);
        return ResponseFactory.getEmptyResponse(request);
    }

    /**
     * This function is to handle the heartbeat sent by an ashva.
     * The heartbeat is sent to MongoDB to update the database state.
     * And, the request is forwarded to other zookeeper so that the system
     * maintains active redundancy to recover from failures.
     * @param request containing heartbeat
     * @return empty response
     */
    private static BasicResponse handleHeartBeatRequest(BasicRequest request) {
        final String ipAddr = request.getExtraData().get("ipAddr");
        final String timeOfBeat = request.getExtraData().get("timeOfBeat");
        final String logs = request.getExtraData().get("logs");
        final Heartbeat heartbeat = new Heartbeat(ipAddr, timeOfBeat, logs);
        Mongo.updateHeartbeat(heartbeat);
        ZooKeeper.notifyOtherZooKeeperServer(heartbeat);
        LOGGER.info("Heartbeat received from Ashva (" + ipAddr + ")");
        return ResponseFactory.getEmptyResponse(request);
    }

    /**
     * This function is to handle the heartbeat sync sent by other zookeeper.
     * The heartbeat is sent to MongoDB to update the database state.
     * @param request containing heartbeat
     * @return empty response
     */
    private static BasicResponse handleHeartBeatSyncRequest(BasicRequest request) {
        final String ipAddr = request.getExtraData().get("ipAddr");
        final String timeOfBeat = request.getExtraData().get("timeOfBeat");
        final String logs = request.getExtraData().get("logs");
        final Heartbeat heartbeat = new Heartbeat(ipAddr, timeOfBeat, logs);
        Mongo.updateHeartbeat(heartbeat);
        LOGGER.info("Heartbeat (sync) received from Ashva (" + ipAddr + ")");
        return ResponseFactory.getEmptyResponse(request);
    }

    /**
     * This function gets list of currently online prashti servers and matches if the given ipAddr is already
     * in the list. Else it is sent the signal to become another prashti, provided there aren't 2 already.
     * @param ipAddr of the server to be next possible prashti
     */
    private static void checkAndBringUpNewPrashti(String ipAddr) {
        boolean isPresent = false;
        final List<Prashti> onlinePrashtiList = D2Client.getOnlinePrashtiServers();
        for (Prashti prashti : onlinePrashtiList) {
            if (prashti.getIpAddr().equals(ipAddr)) {
                isPresent = true;
                break;
            }
        }
        if (D2Client.getOnlinePrashtiServers().size() < 2 && !isPresent) {
            final AshvaClient client = new AshvaClient(ipAddr);
            final BasicRequest req = RequestFactory.getBecomePrashti2Request();
            client.call(req);
            LOGGER.info("Bringing up new prashti server (" + ipAddr + ")");
        } else {
            LOGGER.info(ipAddr + " cannot become another Prashti server");
        }
    }
}
