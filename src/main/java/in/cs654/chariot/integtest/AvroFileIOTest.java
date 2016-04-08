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

import in.cs654.chariot.avro.BasicRequest;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

public class AvroFileIOTest {
    public static void main(String[] args) {

        BasicRequest request = BasicRequest.newBuilder()
                .setRequestId("1222109")
                .setArguments(new ArrayList<String>())
                .setDeviceId("1")
                .setExtraData(new HashMap<String, String>())
                .setFunctionName("testFunc")
                .build();

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(baos, null);
            DatumWriter<BasicRequest> writer = new SpecificDatumWriter<BasicRequest>(BasicRequest.class);
            writer.write(request, encoder);
            encoder.flush();
            baos.close();
            byte[] serializedBytes = baos.toByteArray();
            FileOutputStream fos = new FileOutputStream("/tmp/"+request.getRequestId());
            fos.write(serializedBytes);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Path path = Paths.get("/tmp/"+request.getRequestId());
            byte[] bytes = Files.readAllBytes(path);
            Decoder decoder = DecoderFactory.get().binaryDecoder(bytes, null);
            DatumReader<BasicRequest> reader = new SpecificDatumReader<BasicRequest>(BasicRequest.class);
            BasicRequest request1 = reader.read(null, decoder);
            System.out.println(request1.getFunctionName());
            System.out.println(request1.getRequestId());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
