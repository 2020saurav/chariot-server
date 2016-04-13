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

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.AMQP.BasicProperties;
import in.cs654.chariot.avro.BasicRequest;
import in.cs654.chariot.avro.BasicResponse;
import in.cs654.chariot.prashti.PrashtiServer;
import org.apache.avro.io.*;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

/**
 * This class used to create client objects for making calls to PrashtiServer.
 */
public class PrashtiClient {

    private Connection connection;
    private Channel channel;
    private String replyQueueName;
    private QueueingConsumer consumer;
    private static final String requestQueueName = PrashtiServer.RPC_QUEUE_NAME;
    private BinaryEncoder encoder = null;
    private BinaryDecoder decoder = null;
    private ByteArrayOutputStream baos;
    final ConnectionFactory factory = new ConnectionFactory();
    Prashti prashtiServer = null;
    final static Logger LOGGER = Logger.getLogger("Prashti Client");

    public PrashtiClient() {
        setupPrashtiClient();
    }

    public PrashtiClient(String ipAddr) {
        factory.setHost(ipAddr);
        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            replyQueueName = channel.queueDeclare().getQueue();
            consumer = new QueueingConsumer(channel);
            channel.basicConsume(replyQueueName, true, consumer);
            baos = new ByteArrayOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    private void setupPrashtiClient() {
        List<Prashti> prashtiList = D2Client.getPrashtiServers();
        if (prashtiList.size() > 0) {
            prashtiServer = prashtiList.get(0);
            factory.setHost(prashtiServer.getIpAddr());
            try {
                connection = factory.newConnection();
                channel = connection.createChannel();
                replyQueueName = channel.queueDeclare().getQueue();
                consumer = new QueueingConsumer(channel);
                channel.basicConsume(replyQueueName, true, consumer);
                baos = new ByteArrayOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
                setupPrashtiClient(); // TODO profile this
            }
        } else {
            LOGGER.severe("No Prashti available");
        }
    }

    public BasicResponse call(BasicRequest request) throws IOException, InterruptedException {
        // TODO profile this to see if rabbitmq calls fixes it or a timeout is needed
        // TODO in case timeout is needed, run setupPrashtiClient() after timeout
        BasicResponse response = new BasicResponse();
        final String corrId = UUID.randomUUID().toString();
        final BasicProperties props = new BasicProperties.Builder()
                .correlationId(corrId)
                .replyTo(replyQueueName)
                .build();
        baos.reset();
        final DatumWriter<BasicRequest> avroWriter = new SpecificDatumWriter<BasicRequest>(BasicRequest.class);
        encoder = EncoderFactory.get().binaryEncoder(baos, encoder);
        avroWriter.write(request, encoder);
        encoder.flush();
        channel.basicPublish("", requestQueueName, props, baos.toByteArray());

        while (true) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                final DatumReader<BasicResponse> avroReader =
                        new SpecificDatumReader<BasicResponse>(BasicResponse.class);
                decoder = DecoderFactory.get().binaryDecoder(delivery.getBody(), decoder);
                response = avroReader.read(response, decoder);
                break;
            }
        }
        return response;
    }

    public void close() throws Exception {
        connection.close();
    }
}
