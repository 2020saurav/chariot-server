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
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

/**
 * This class contains all required functions for database operations required in this application.
 * Database : chariot
 * Collections : devices - collection of device, each having device id and corresponding name of docker image
 *               <other> - <update>
 */
public class Mongo {

    private static final MongoClient mongoClient = new MongoClient("localhost", 27017);
    private static final MongoDatabase db = mongoClient.getDatabase("chariot");
    private static final MongoCollection<Document> devicesCollection = db.getCollection("devices");

    public static void addDevice(Device device) {
        devicesCollection.insertOne(new Document("device", device));
    }

    public static String getDockerImage(String deviceID) {
        // TODO complete this
        return null;
    }

    // TODO add other required collections and corresponding helper methods
}
