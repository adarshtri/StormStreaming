package bolts;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Tuple;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class CampaignCountRegisterBolt implements IRichBolt {

    private Map<String,Integer> counter;
    private OutputCollector outputCollector;
    private Connection conn;
    private Statement statement;

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.outputCollector = outputCollector;
        this.counter = new HashMap<>();
        try {
            this.conn = DriverManager.getConnection("jdbc:mysql://localhost/kafkastormtest?" +
                    "user=root&password=720354M@noj");
            this.statement = conn.createStatement();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    @Override
    public void execute(Tuple tuple) {

        if(!counter.containsKey(tuple.getString(0))){
            counter.put(tuple.getString(0),1);

        }else{
            Integer c = counter.get(tuple.getString(0));
            c+=1;
            counter.put(tuple.getString(0),c);
            if(c==5){
                String insert_string = "insert into campaign_register_count(campaign_name,count) values(\'"+tuple.getString(0)+"\',"+c.toString()+")";
                System.out.println(insert_string);
                try {
                    statement.execute(insert_string);
                }catch (SQLException e){
                    e.printStackTrace();
                }

                counter.remove(tuple.getString(0));
            }
        }
    }

    @Override
    public void cleanup() {
        try {
            statement.close();
            conn.close();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}
