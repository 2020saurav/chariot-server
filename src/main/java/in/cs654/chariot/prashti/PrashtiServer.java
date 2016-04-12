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

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.AMQP.BasicProperties;
import in.cs654.chariot.avro.BasicRequest;
import in.cs654.chariot.avro.BasicResponse;
import in.cs654.chariot.utils.ResponseFactory;
import org.apache.avro.io.*;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;

import java.io.ByteArrayOutputStream;
import java.util.logging.Logger;

/**
 * This class is the RPC server for Prashti module, built using RabbitMQ. The server keeps listening to RPC requests,
 * and passes the request to PrashtiProcessor for execution and creation of response object. PrashtiProcessor
 * decides if the request is to be handled at Prashti level or be forwarded to Ashva as RPC.
 */
public class PrashtiServer {
    public static final String RPC_QUEUE_NAME = "rpc_queue_prashti";
    private static final Logger LOGGER = Logger.getLogger(PrashtiServer.class.getName());
    private static final String HOST_IP_ADDR = "0.0.0.0";
    private static BinaryDecoder decoder = null;
    private static BinaryEncoder encoder = null;
    private static ByteArrayOutputStream baos = new ByteArrayOutputStream();

    public static void main(String[] args) {
        Connection connection = null;
        Channel channel;
        try {
            final ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(HOST_IP_ADDR);
            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);
            channel.basicQos(1);

            final QueueingConsumer consumer = new QueueingConsumer(channel);
            channel.basicConsume(RPC_QUEUE_NAME, false, consumer);
            LOGGER.info("Prashti Server started. Waiting for requests...");

            while (true) {
                final QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                BasicResponse response = new BasicResponse();
                BasicRequest request = new BasicRequest();
                final BasicProperties props = delivery.getProperties();
                final BasicProperties replyProps = new BasicProperties.Builder()
                        .correlationId(props.getCorrelationId()).build();
                try {
                    final DatumReader<BasicRequest> avroReader =
                            new SpecificDatumReader<BasicRequest>(BasicRequest.class);
                    decoder = DecoderFactory.get().binaryDecoder(delivery.getBody(), decoder);
                    request = avroReader.read(request, decoder);
                    response = RequestProcessor.process(request);

                } catch (Exception e) {
                    e.printStackTrace();
                    LOGGER.severe("Error in handling request: " + e.getMessage());
                    response = ResponseFactory.getErrorResponse(request);

                } finally {
                    baos.reset();
                    final DatumWriter<BasicResponse> avroWriter =
                            new SpecificDatumWriter<BasicResponse>(BasicResponse.class);
                    encoder = EncoderFactory.get().binaryEncoder(baos, encoder);
                    avroWriter.write(response, encoder);
                    encoder.flush();
                    LOGGER.info("Responding to request id " + request.getRequestId() + " " + response.getStatus());
                    channel.basicPublish("", props.getReplyTo(), replyProps, baos.toByteArray());
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.severe("Error in RPC server: " + e.getMessage());
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception ignore) {
                }
            }
        }
    }
}
