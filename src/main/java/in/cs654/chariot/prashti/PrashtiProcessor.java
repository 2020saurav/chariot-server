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

package in.cs654.chariot.prashti;

import in.cs654.chariot.avro.BasicRequest;
import in.cs654.chariot.avro.BasicResponse;
import in.cs654.chariot.utils.*;

import java.util.List;

// TODO javadoc
public class PrashtiProcessor {

    public static BasicResponse process(BasicRequest request) {

        if (request.getFunctionName().equals(ReservedFunctions.DEVICE_SETUP.toString())) {
            String dockerImage = request.getExtraData().get("dockerImage");
            Device device = new Device(request.getDeviceId(), dockerImage);
            BasicRequest installRequest = RequestFactory.getInstallRequest(device);
            List<Ashva> ashvaList = Mongo.getAliveAshvaList();
            for (Ashva ashva : ashvaList) {
                AshvaClient client = new AshvaClient(ashva.getIpAddr());
                client.call(installRequest);
            }
            return ResponseFactory.getEmptyResponse(request);

        } else {
            // TODO
            return new BasicResponse(); // fill in appropriate information in response
        }
    }
}
