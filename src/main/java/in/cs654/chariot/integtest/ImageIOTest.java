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

import in.cs654.chariot.ashva.AshvaProcessor;
import in.cs654.chariot.avro.BasicRequest;
import in.cs654.chariot.avro.BasicResponse;
import in.cs654.chariot.utils.CommonUtils;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ImageIOTest {

    public static void main(String[] args) {
        try {
            File fi = new File("/home/saurav/chariot1.jpg");
            Map<String, String> map = new HashMap<String, String>();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BufferedImage image = ImageIO.read(fi);
            try {
                ImageIO.write(image,"png", baos);
                byte[] imgBytes = baos.toByteArray();
                BASE64Encoder encoder = new BASE64Encoder();
                String imageString = encoder.encode(imgBytes);
                baos.close();
                map.put("imgBytes", imageString);
            } catch (IOException ignore) {
            }
            BasicRequest request = BasicRequest.newBuilder()
                    .setRequestId(CommonUtils.randomString(32))
                    .setArguments(new ArrayList<String>())
                    .setDeviceId("3")
                    .setFunctionName("blur")
                    .setExtraData(map)
                    .build();

            BasicResponse response = AshvaProcessor.process(request);
            BASE64Decoder decoder = new BASE64Decoder();
            byte[] bytes = decoder.decodeBuffer(response.getResponse().get("blurStr"));
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            BufferedImage img = ImageIO.read(bais);
            File outputfile = new File("/home/saurav/chariot1blur.png");
            ImageIO.write(img, "png", outputfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
