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
import java.util.logging.Logger;

// TODO write javadoc
public class PrashtiServer {
    public static final String RPC_QUEUE_NAME = "rpc_queue_prashti";
    private static final Logger LOGGER = Logger.getLogger(PrashtiServer.class.getName());
    private static final String HOST_IP_ADDR = "0.0.0.0";

    private static int fib(int n) {
        if (n==0) return 0;
        if (n==1) return 1;
        return fib(n-1) + fib(n-2);
    }

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
                String response = "";
                final QueueingConsumer.Delivery delivery = consumer.nextDelivery();

                BasicProperties props = delivery.getProperties();
                final BasicProperties replyProps = new BasicProperties.Builder()
                        .correlationId(props.getCorrelationId()).build();
                try {
                    String message = new String(delivery.getBody(), "UTF-8");
                    final int n = Integer.parseInt(message);
                    LOGGER.info("Message received: " + message);
                    response = "" + fib(n);
                } catch (Exception e) {
                    LOGGER.severe("Error in handling request: " + e.getMessage());
                    response = "";
                } finally {
                    channel.basicPublish("", props.getReplyTo(), replyProps, response.getBytes("UTF-8"));
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                }
            }
        } catch (Exception e) {
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
