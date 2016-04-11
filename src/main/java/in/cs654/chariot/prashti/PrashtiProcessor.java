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
import in.cs654.chariot.utils.ReservedFunctions;

// TODO javadoc
public class PrashtiProcessor {

    public static BasicResponse process(BasicRequest request) {

        if (request.getFunctionName().equals(ReservedFunctions.ASHVA_JOIN.toString())) {
            /*
            TODO add the ashva to the pool
            - Since this prashti is receiving the request, this one is UP,
            - 2 cases : only this prashti is up. or the other is up too.
            - in either case, just add the ashva to the pool.
            - Recovery Manager will see to it that the ashva is promoted to prashti
            - Sync Manager will sync this information with other prashti
             */
            return new BasicResponse(); // fill in appropriate information in response
        } else if (request.getFunctionName().equals(ReservedFunctions.DEVICE_SETUP.toString())) {
            /*
            TODO Inform all servers to setup this new device
            - actual inclusion of information in database to be handled in DEVICE_INSTALL
             */
            return new BasicResponse(); // fill in appropriate information in response
        } else if (request.getFunctionName().equals(ReservedFunctions.DEVICE_INSTALL.toString())) {
            /*
            TODO install this device dependencies and update database.
            - add this device information in database
            - pull the docker image
             */
            return new BasicResponse(); // fill in appropriate information in response
        } else {
            // TODO
            return new BasicResponse(); // fill in appropriate information in response
        }
    }
}
