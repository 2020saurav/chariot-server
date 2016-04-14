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

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * This class provides implements 2 methods called process and error to prepare response when the request is fulfilled,
 * or it fails.
 */
public class AshvaProcessor {

    /**
     * This function takes in request, executes the request and returns the response.
     * The request is written into /tmp/<request_id>.req file. While running the docker container, /tmp dir is mounted
     * to /tmp of the container. This enables ease of data exchange. TODO encryption
     * Docker runs the request and puts the result into /tmp/<request_id>.res. A timeout has been set as 10s, failing
     * which error response is sent.
     * @param request containing function_name and other required information
     * @return response of the request
     */
    public static BasicResponse process(BasicRequest request) {
        if (request.getFunctionName().equals(ReservedFunctions.DEVICE_INSTALL.toString())) {
            final String deviceId = request.getDeviceId();
            final String dockerImage = request.getExtraData().get("dockerImage");
            final String cmd = "docker pull " + dockerImage;
            try {
                Runtime.getRuntime().exec(cmd);
                Mongo.addDevice(new Device(deviceId, dockerImage));
            } catch (IOException ignore) {
            }
            return ResponseFactory.getEmptyResponse(request);
        } else if (request.getFunctionName().equals(ReservedFunctions.BECOME_PRASHTI2.toString())) {
            return null;
        } else {
            final String requestID = request.getRequestId();
            try {
                final byte[] serializedBytes = AvroUtils.requestToBytes(request);
                final FileOutputStream fos = new FileOutputStream("/tmp/" + requestID + ".req");
                fos.write(serializedBytes);
                fos.close();
                // TODO fetch docker image name from DB based on device_id
                final String cmd = "docker run -v /tmp:/tmp 2020saurav/chariot:1.0 /bin/chariot " + requestID;
                final Process process = Runtime.getRuntime().exec(cmd);
                try {
                    if (process.waitFor(10L, TimeUnit.SECONDS)) {
                        final Path path = Paths.get("/tmp/" + requestID + ".res");
                        byte[] bytes = Files.readAllBytes(path);
                        return AvroUtils.bytesToResponse(bytes);
                    } else {
                        return ResponseFactory.getErrorResponse(request);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return ResponseFactory.getErrorResponse(request);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseFactory.getErrorResponse(request);
            }
        }
    }
}
