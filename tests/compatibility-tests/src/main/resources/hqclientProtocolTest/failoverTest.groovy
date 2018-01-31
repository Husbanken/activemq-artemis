package meshTest

import org.apache.activemq.artemis.tests.compatibility.GroovyRun

import javax.jms.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// starts an artemis server
String serverType = arg[0];
String clientType = arg[1];
String operation = arg[2];


String queueName = "queue";


String textBody = "a rapadura e doce mas nao e mole nao";

println("clientType " + clientType);
println("serverType " + serverType);

if (clientType.startsWith("ARTEMIS")) {
    // Can't depend directly on artemis, otherwise it wouldn't compile in hornetq
    GroovyRun.evaluate("clients/artemisHQClientHA.groovy", "serverArg", serverType);
} else {
    throw new RuntimeException("The test is not meant for client type: " + clientType);
}


Connection connection = cf.createConnection();
Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
Queue queue = session.createQueue(queueName);

if (operation.equals("failoverTestSend")) {

    CountDownLatch latch = new CountDownLatch(10);

    CompletionListener completionListener = new CompletionListener() {
        @Override
        void onCompletion(Message message) {
            latch.countDown();
        }

        @Override
        void onException(Message message, Exception exception) {

        }
    }

    MessageProducer producer = session.createProducer(queue);
    producer.setDeliveryMode(DeliveryMode.PERSISTENT);
    for (int i = 0; i < 10; i++) {
        producer.send(session.createTextMessage(textBody + i), completionListener);
    }

    GroovyRun.assertTrue(latch.await(10, TimeUnit.SECONDS));

    System.out.println("Message sent");

    return connection;
} else {
    throw new RuntimeException("Invalid operation " + operation);
}




