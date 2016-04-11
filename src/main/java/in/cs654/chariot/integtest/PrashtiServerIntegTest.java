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

package in.cs654.chariot.integtest;

import in.cs654.chariot.avro.BasicResponse;
import in.cs654.chariot.utils.CommonUtils;
import in.cs654.chariot.utils.PrashtiClient;
import in.cs654.chariot.avro.BasicRequest;

import java.util.ArrayList;
import java.util.HashMap;

public class PrashtiServerIntegTest {

    public static void main(String[] args) {
        PrashtiClient testRpc = null;
        BasicResponse response;
        try {
            testRpc = new PrashtiClient();

            BasicRequest lifeUniv = BasicRequest.newBuilder()
                    .setRequestId(CommonUtils.randomString(32))
                    .setDeviceId("1")
                    .setFunctionName("testFunc")
                    .setArguments(new ArrayList<String>())
                    .setExtraData(new HashMap<String, String>())
                    .build();

            System.out.println("Requesting " + lifeUniv.getFunctionName());
            response = testRpc.call(lifeUniv);
            System.out.println(" [.] Got '" + response.getResponse().get("answer") + "'");
        }
        catch  (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (testRpc!= null) {
                try {
                    testRpc.close();
                }
                catch (Exception ignore) {}
            }
        }
    }
}
