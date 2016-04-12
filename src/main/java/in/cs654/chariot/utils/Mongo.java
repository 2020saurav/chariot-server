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

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import in.cs654.chariot.turagraksa.ZooKeeper;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains all required functions for database operations required in this application.
 * Database : chariot
 * Collections : devices - collection of device, each having device id and corresponding name of docker image
 *               heartbeat - collection of server status : ipAddr, time of last beat, network time lag, logs
 *               <other> - <update>
 */
public class Mongo {

    private static final MongoClient mongoClient = new MongoClient("localhost", 27017);
    private static final MongoDatabase db = mongoClient.getDatabase("chariot");
    private static final MongoCollection<Document> devicesCollection = db.getCollection("devices");
    private static final MongoCollection<Document> heartbeatCollection = db.getCollection("heartbeat");

    // using _id ensures unique id
    public static void addDevice(Device device) {
        devicesCollection.insertOne(
                new Document("_id", device.getId()).append("docker_image", device.getDockerImage()));
    }

    public static String getDockerImage(String deviceID) {
        final FindIterable<Document> docs = devicesCollection.find(new Document("_id", deviceID));
        return docs.first().get("docker_image").toString();
    }

    public static void deleteDeviceById(String deviceID) {
        final FindIterable<Document> docs = devicesCollection.find(new Document("_id", deviceID));
        devicesCollection.deleteOne(docs.first());
    }

    public static void updateHeartbeat(Heartbeat heartbeat) {
        final Long timeLag = System.currentTimeMillis() - Long.parseLong(heartbeat.getTimeOfBeat());
        final Document doc = heartbeatCollection.find(new Document("_id", heartbeat.getIpAddr())).first();
        if (doc == null){
            heartbeatCollection.insertOne(
                    new Document("_id", heartbeat.getIpAddr())
                            .append("last_beat", heartbeat.getTimeOfBeat())
                            .append("logs", heartbeat.getLogs())
                            .append("network_lag", timeLag)
            );
        } else {
            heartbeatCollection.replaceOne(
                    new Document("_id", heartbeat.getIpAddr()),
                    new Document("last_beat", heartbeat.getTimeOfBeat())
                            .append("logs", heartbeat.getLogs())
                            .append("network_lag", timeLag));
        }
    }

    public static Long getLastBeatTime(String ipAddr) {
        final FindIterable<Document> docs = heartbeatCollection.find(new Document("_id", ipAddr));
        return Long.parseLong(docs.first().get("last_beat").toString());
    }

    public static void deleteHeartbeatByIP(String ipAddr) {
        final FindIterable<Document> docs = heartbeatCollection.find(new Document("_id", ipAddr));
        heartbeatCollection.deleteOne(docs.first());
    }

    public static List<Ashva> getAliveAshvaList() {
        final List<Ashva> ashvaList = new ArrayList<Ashva>();
        final Long currentTime = System.currentTimeMillis();
        final FindIterable<Document> docs = heartbeatCollection.find();
        for (Document doc : docs) {
            Long tau = currentTime - Long.parseLong(doc.get("last_beat").toString());
            if (tau < ZooKeeper.HB_TIME_THRESHOLD) {
                ashvaList.add(new Ashva(doc.get("_id").toString()));
            }
        }
        return ashvaList;
    }
}
