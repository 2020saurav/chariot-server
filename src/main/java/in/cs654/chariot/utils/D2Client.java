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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import in.cs654.chariot.avro.BasicRequest;
import in.cs654.chariot.avro.BasicResponse;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

/**
 * D2Client acts as the intermediary between chariot server and D2 service
 */
public class D2Client {
    private static final String d2ServiceURL = "http://172.27.25.236:4567/prashtis";
    private static final long D2_PING_TIMEOUT = 2000L; // milliseconds

    /**
     * This function gets the list of prashti servers from D2 server
     * and then checks if the prashti is really online and returns the filtered list
     * @return list of online prashti servers
     */
    public static List<Prashti> getOnlinePrashtiServers() {
        final List<Prashti> prashtiList = getPrashtiServers();
        final List<Prashti> onlinePrashtis = new ArrayList<Prashti>();
        for (Prashti prashti : prashtiList) {
            if (isActive(prashti.getIpAddr())) {
                onlinePrashtis.add(prashti);
            }
        }
        return onlinePrashtis;
    }

    /**
     * This function gets the list of prashti servers from D2 server
     * and returns the list, as is.
     * @return list of prashti servers as present on D2 server
     */
    static List<Prashti> getPrashtiServers() {
        final List<Prashti> prashtiList = new ArrayList<Prashti>();
        try {
            final URL url = new URL(d2ServiceURL);
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            final BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            final JsonParser parser = new JsonParser();
            final JsonElement element = parser.parse(in.readLine());
            final JsonArray jsonArray = element.getAsJsonArray();
            for (JsonElement e : jsonArray) {
                final String ipAddr = e.getAsJsonObject().get("ipAddr").getAsString();
                if (!ipAddr.equals("")) {
                    prashtiList.add(new Prashti(ipAddr));
                }
            }
        } catch (Exception ignore) {
        }
        return prashtiList;
    }

    /**
     * This function tests if a prashti server (given it's IP address) is online
     * @param ipAddr of the prashti server
     * @return true if online, false otherwise
     */
    private static boolean isActive(final String ipAddr) {
        final ExecutorService executorService = Executors.newCachedThreadPool();
        final Callable<BasicResponse> task = new Callable<BasicResponse>() {
            @Override
            public BasicResponse call() throws Exception {
                final PrashtiClient client = new PrashtiClient(ipAddr);
                final BasicRequest request = BasicRequest.newBuilder()
                        .setRequestId(CommonUtils.randomString(32))
                        .setArguments(new ArrayList<String>())
                        .setDeviceId("D2")
                        .setExtraData(new HashMap<String, String>())
                        .setFunctionName(ReservedFunctions.PING.toString())
                        .build();
                return client.call(request);
            }
        };
        final Future<BasicResponse> future = executorService.submit(task);
        try {
            final BasicResponse ignore = future.get(D2_PING_TIMEOUT, TimeUnit.MILLISECONDS);
            return true;
        } catch (Exception ignore) {
            return false;
        }
    }

    /**
     * This function is used to set the new list of prashti servers
     * The architecture permits a maximum to 2 prashti servers (recommended is 2 too)
     * After updating the list, the current zookeepers are notified to make necessary changes.
     * @param prashtiList to be set as new list of prashti server IP addresses
     */
    public static void setPrashtiServers(List<Prashti> prashtiList) {
        try {
            final URL url = new URL(d2ServiceURL);
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            String params = "";
            if (prashtiList.size() == 1) {
                params = "ip1=" + prashtiList.get(0).getIpAddr();
                params += "&ip2=";
            } else if (prashtiList.size() >= 2) {
                params = "ip1=" + prashtiList.get(0).getIpAddr();
                params += "&ip2=" + prashtiList.get(1).getIpAddr();
            }
            connection.setDoOutput(true);
            DataOutputStream stream = new DataOutputStream(connection.getOutputStream());
            stream.writeBytes(params);
            stream.flush();
            stream.close();
            connection.getResponseCode();
            notifyZooKeeperAboutChange();
        } catch (Exception ignore) {
        }
    }

    /**
     * This function is used to notify the zookeepers (max 2) about any change in D2 state.
     */
    private static void notifyZooKeeperAboutChange() {
        final BasicRequest request = BasicRequest.newBuilder()
                .setDeviceId("D2Client")
                .setRequestId(CommonUtils.randomString(32))
                .setArguments(new ArrayList<String>())
                .setFunctionName(ReservedFunctions.PRASHTI_CHANGE.toString())
                .setExtraData(new HashMap<String, String>())
                .build();
        final List<Prashti> prashtiList = getOnlinePrashtiServers();
        if (prashtiList.size() == 2) {
            for (Prashti prashti : prashtiList) {
                final ZooKeeperClient client = new ZooKeeperClient(prashti.getIpAddr());
                client.call(request);
            }
        }
    }
}
