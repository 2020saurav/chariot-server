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
import in.cs654.chariot.avro.BasicResponse;
import in.cs654.chariot.utils.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * This class contains the function process which handles the requests sent to this ashva server.
 */
public class AshvaProcessor {

    private static final Logger LOGGER = Logger.getLogger("Ashva Processor");
    public static final Long PROCESS_DEFAULT_TIMEOUT = 5000L; // milliseconds

    /**
     * This function handles all the requests routed to it. It matches the request against the following:
     * DEVICE_INSTALL : This requires the ashva server to install (pull the docker image corresponding to) the device
     * BECOME_PRASHTI2 : This required the ashva server to initiate it's prashti and zookeeper servers
     * @param request to handle
     * @return response of the request
     */
    public static BasicResponse process(BasicRequest request) {
        if (request.getFunctionName().equals(ReservedFunctions.DEVICE_INSTALL.toString())) {
            return handleDeviceInstallRequest(request);
        } else if (request.getFunctionName().equals(ReservedFunctions.BECOME_PRASHTI2.toString())) {
            return handleBecomePrashtiRequest(request);
        } else {
            return handleTaskProcessingRequest(request);
        }
    }

    /**
     * This function handles device installation request.
     * It gets the device id and name of the docker image from the request, and then runs docker pull image to
     * get the image from docker hub.
     * @param request consisting of (device id, dockerImage)
     * @return empty response as nothing useful can be returned
     */
    private static BasicResponse handleDeviceInstallRequest(BasicRequest request) {
        final String deviceId = request.getDeviceId();
        final String dockerImage = request.getExtraData().get("dockerImage");
        final String cmd = "docker pull " + dockerImage;
        try {
            Runtime.getRuntime().exec(cmd);
            Mongo.addDevice(new Device(deviceId, dockerImage));
            LOGGER.info("Pulled docker image " + dockerImage + " for device " + deviceId);
        } catch (IOException ignore) {
            LOGGER.severe("Error in installing device " + deviceId + " (" + dockerImage + ")");
        }
        return ResponseFactory.getEmptyResponse(request);
    }

    /**
     * This function handles request to become a prashti server. This requires running of prashti server
     * and zookeeper server. The jar containing the classes is assumed to be present in the directory
     * specified in `chariot.sh` in the project root.
     * @param request to start prashti and zookeeper servers
     * @return empty response
     */
    private static BasicResponse handleBecomePrashtiRequest(BasicRequest request) {
        try {
            LOGGER.info("Starting Prashti and ZooKeeper Server");
            Runtime.getRuntime().exec("./chariot.sh");
        } catch (IOException ignore) {
            LOGGER.severe("Error in starting Prashti and Zookeeper servers");
        }
        return ResponseFactory.getEmptyResponse(request);
    }

    /**
     * This function takes in request, executes the request and returns the response.
     * The request is written into /tmp/<request_id>.req file. While running the docker container, /tmp dir is mounted
     * to /tmp of the container. This enables ease of data exchange. TODO encryption
     * Docker runs the request and puts the result into /tmp/<request_id>.res.
     * If the request specifies a timeout, it is picked up, else default timeout is set for the process to run
     * @param request containing function_name and other required information
     * @return response of the request
     */
    private static BasicResponse handleTaskProcessingRequest(BasicRequest request) {
        final String requestID = request.getRequestId();
        try {
            final byte[] serializedBytes = AvroUtils.requestToBytes(request);
            final FileOutputStream fos = new FileOutputStream("/tmp/" + requestID + ".req");
            fos.write(serializedBytes);
            fos.close();
            String timeoutString = request.getExtraData().get("chariot_timeout");
            Long timeout;
            if (timeoutString != null) {
                timeout = Long.parseLong(timeoutString);
            } else {
                timeout = PROCESS_DEFAULT_TIMEOUT;
            }

            final String dockerImage = Mongo.getDockerImage(request.getDeviceId());
            final String cmd = "docker run -v /tmp:/tmp " + dockerImage + " /bin/chariot " + requestID;
            final Process process = Runtime.getRuntime().exec(cmd);
            TimeoutProcess timeoutProcess = new TimeoutProcess(process);
            timeoutProcess.start();
            try {
                timeoutProcess.join(timeout); // milliseconds
                if (timeoutProcess.exit != null) {
                    File file = new File("/tmp/" + requestID + ".res");
                    byte[] bytes = FileUtils.readFileToByteArray(file);
                    LOGGER.info("Response generated for request " + requestID);
                    return AvroUtils.bytesToResponse(bytes);
                } else {
                    LOGGER.severe("Timeout in generating response for request " + requestID);
                    return ResponseFactory.getTimeoutErrorResponse(request);
                }
            } catch (InterruptedException ignore) {
                timeoutProcess.interrupt();
                Thread.currentThread().interrupt();
                LOGGER.severe("Error in generating response for request " + requestID);
                return ResponseFactory.getErrorResponse(request);
            } finally {
                process.destroy();
            }
        } catch (IOException ignore) {
            LOGGER.severe("Error in generating response for request: " + requestID);
            return ResponseFactory.getErrorResponse(request);
        }
    }
}
