/*******************************************************************************
 * Copyright 2015 Amit Kumar Mondal <admin@amitinside.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package de.tum.in.client.example;

import de.tum.in.client.IKuraMQTTClient;
import de.tum.in.client.KuraMQTTClient;
import de.tum.in.client.adapter.MessageListener;
import de.tum.in.client.message.KuraPayload;

public final class Snippets {

	private static IKuraMQTTClient client;
	private static String clientId = "AMIT";
	private static String HEARTBEAT = "$EDC/tum/B8:27:EB:A6:A9:8A/HEARTBEAT-V1/mqtt/heartbeat";
	private static boolean status;

	public static void main(final String... args) {
		// Create the connection object
		client = new KuraMQTTClient.Builder().setHost("broker-sandbox.everyware-cloud.com").setPort("1883")
				.setClientId("CLIENT_176992").setUsername("akm").setPassword("ChangeMeS00n!").build();
		;

		// Connect to the Message Broker
		status = client.connect();

		if (status) {
			client.subscribe(HEARTBEAT, new MessageListener() {

				@Override
				public void processMessage(final KuraPayload payload) {
					System.out.println(payload.metrics());
				}
			});

			System.out.println("Subscribed to channels " + client.getSubscribedChannels());

			System.out.println("Waiting for new messages");

		}

		final KuraPayload payload = new KuraPayload();
		payload.addMetric("request.id", "454545454545456");
		payload.addMetric("requester.client.id", clientId);
		payload.addMetric("nodeId", "8");
		payload.addMetric("encVal",
				"821b5d53fcba7680aecafbfd9a29658923e6d0a27315daa4345ffa865c4fd7a964bfe51a252cd8e891a8503ae09b82836ffb5e15b1e61233b7f3938b5869900a93da74ceb1ba272d26f3cf0f7ba073b1");
		System.out.println(status);

		if (status) {
			// client.publish("", payload);

			System.out.println("--------------------------------------------------------------------");
			System.out.println("Request Published");
			System.out.println("Request ID : " + "454545454545456");
			System.out.println("Request Client ID : " + clientId);
			System.out.println("--------------------------------------------------------------------");
		}

		while (!Thread.currentThread().isInterrupted()) {
		}
	}

}