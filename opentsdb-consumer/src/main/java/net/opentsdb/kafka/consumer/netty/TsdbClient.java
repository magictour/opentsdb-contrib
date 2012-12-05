package net.opentsdb.kafka.consumer.netty;

import com.google.common.util.concurrent.AbstractIdleService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

@Singleton
public class TsdbClient extends AbstractIdleService {
  private static final Logger logger = LoggerFactory.getLogger(TsdbClient.class);

  private final ClientBootstrap bootstrap;
  private final InetSocketAddress address;

  private Channel channel;
  private ChannelFuture lastWriteFuture;

  @Inject
  public TsdbClient(ChannelFactory factory, InetSocketAddress address, TsdbClientPipelineFactory pipelineFactory) {
    logger.info("Starting up tsdbclient");

    this.address = address;
    this.bootstrap = new ClientBootstrap(factory);
    bootstrap.setPipelineFactory(pipelineFactory);
  }

  @Override
  protected void startUp() throws Exception {
    ChannelFuture future = bootstrap.connect(address);

    channel = future.awaitUninterruptibly().getChannel();
    if(!future.isSuccess()) {
      future.getCause().printStackTrace();
      bootstrap.releaseExternalResources();
      System.exit(1);
    }

    logger.info("Done starting up tsdbclient");
  }

  @Override
  protected void shutDown() throws Exception {
    logger.info("Shutting down tsdb client");
    if(lastWriteFuture != null) {
      lastWriteFuture.awaitUninterruptibly();
    }

    channel.close().awaitUninterruptibly();
    bootstrap.releaseExternalResources();
  }

  public void send(String message) {
    logger.info("sending message {}", message);
    lastWriteFuture = channel.write(message + "\r\n");
  }
}