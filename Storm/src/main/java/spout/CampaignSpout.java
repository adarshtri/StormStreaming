package spout;

import org.apache.storm.kafka.*;
import org.apache.storm.spout.SchemeAsMultiScheme;

import java.util.UUID;

public class CampaignSpout {

    private SpoutConfig kafkaSpoutConfig;

    public CampaignSpout(String zkString, String topic){

        BrokerHosts hosts = new ZkHosts(zkString);
        this.kafkaSpoutConfig = new SpoutConfig(hosts, topic, "/" + topic,
                UUID.randomUUID().toString());
        this.kafkaSpoutConfig.bufferSizeBytes = 1024 * 1024 * 4;
        this.kafkaSpoutConfig.fetchSizeBytes = 1024 * 1024 * 4;
        this.kafkaSpoutConfig.scheme = new SchemeAsMultiScheme(new StringScheme());

    }

    public KafkaSpout getSpout(){


        return new KafkaSpout(this.kafkaSpoutConfig);
    }


}
