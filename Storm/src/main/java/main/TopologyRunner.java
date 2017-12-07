package main;

import bolts.CampaignCountRegisterBolt;
import bolts.CountTumbleBolt;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseWindowedBolt;
import spout.CampaignSpout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TopologyRunner {

    public static void main(String[] args){

        Config config = new Config();
        config.setDebug(true);
        config.put(Config.TOPOLOGY_DEBUG,true);
        config.put(Config.TOPOLOGY_MAX_SPOUT_PENDING, 6000);
        config.put(Config.TOPOLOGY_MESSAGE_TIMEOUT_SECS,180000);
        String zkConnString = "localhost:2181";
        String topic = "test_topic";

        TopologyBuilder topologyBuilder = new TopologyBuilder();
        topologyBuilder.setSpout("campaign_spout",new CampaignSpout(zkConnString,topic).getSpout());
        topologyBuilder.setBolt("tumble_count_bolt",new CountTumbleBolt().
                withTumblingWindow(BaseWindowedBolt.Count.of(10))).shuffleGrouping("campaign_spout");
        topologyBuilder.setBolt("campaign_register_bolt", new CampaignCountRegisterBolt()).shuffleGrouping("campaign_spout");

        LocalCluster localCluster = new LocalCluster();
        config.setNumWorkers(1);

        localCluster.submitTopology("campaign_topology", config, topologyBuilder.createTopology());

        try(InputStreamReader input = new InputStreamReader(System.in);
            BufferedReader br = new BufferedReader(input)
        ){
            while(true) {

                System.out.println("Enter [stop] to stop the streaming application.");
                String cmd = br.readLine();

                if ("stop".equalsIgnoreCase(cmd)) {
                    localCluster.shutdown();
                }else{
                    System.out.println(cmd+" command is not supported.");
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }

    }

}