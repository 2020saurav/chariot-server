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

import in.cs654.chariot.avro.BasicRequest;
import in.cs654.chariot.avro.BasicResponse;
import org.apache.avro.io.*;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Helper functions to convert request object to byte array; and byte array to response object
 */
public class AvroUtils {
    /**
     * Function to serialize request object into byte array
     * @param request object to serialize
     * @return byte array of the request object
     * @throws IOException
     */
    public static byte[] requestToBytes(BasicRequest request) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(baos, null);
        final DatumWriter<BasicRequest> writer = new SpecificDatumWriter<BasicRequest>(BasicRequest.class);
        writer.write(request, encoder);
        encoder.flush();
        baos.close();
        return baos.toByteArray();
    }

    /**
     * Function to deserialize byte array into response object
     * @param bytes byte array of serialized object
     * @return response object
     * @throws IOException
     */
    public static BasicResponse bytesToResponse(byte[] bytes) throws IOException {
        final Decoder decoder = DecoderFactory.get().binaryDecoder(bytes, null);
        final DatumReader<BasicResponse> reader = new SpecificDatumReader<BasicResponse>(BasicResponse.class);
        return reader.read(null, decoder);
    }
}
