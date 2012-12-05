package net.opentsdb.proxy.netty;

import net.opentsdb.proxy.kafka.KafkaProducer;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PutDataPointRpc implements TelnetRpc {
  private static final Logger logger = LoggerFactory.getLogger(PutDataPointRpc.class);
  private final KafkaProducer producer;

  public PutDataPointRpc(KafkaProducer producer) {
    this.producer = producer;
  }

  @Override
  public void execute(Channel chan, String[] command) {
    if (command.length < 5) {
      throw new IllegalArgumentException("not enough arguments (need least 4, got " + (command.length) + ')');
    }
    final String metric = command[1];
    if (metric.length() <= 0) {
      throw new IllegalArgumentException("empty metric name");
    }

    final String value = command[3];
    if (value.length() <= 0) {
      throw new IllegalArgumentException("empty value");
    }

    producer.sendMessage(command);
  }
}