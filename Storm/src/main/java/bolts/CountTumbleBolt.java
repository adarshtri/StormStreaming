package bolts;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.base.BaseWindowedBolt;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.windowing.TupleWindow;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class CountTumbleBolt extends BaseWindowedBolt {

    private OutputCollector outputCollector;
    private Connection conn;

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector){
        this.outputCollector = collector;
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost/kafkastormtest?" +
                    "user=root&password=720354M@noj");
        }catch (SQLException e){
            e.printStackTrace();
        }
    }


    @Override
    public void execute(TupleWindow tupleWindow) {

        HashMap<String,Integer> counter = new HashMap<>();

        for(Tuple tuple : tupleWindow.get()){
            if(!counter.containsKey(tuple.getString(0)))
                counter.put(tuple.getString(0),1);
            else
                counter.put(tuple.getString(0),counter.get(tuple.getString(0))+1);
        }

        try{
            Statement statement = conn.createStatement();
            for(String str : counter.keySet()){
                String insert_string = "insert into tumble_count(campaign_name,count) values(\'"+str+"\',"+counter.get(str).toString()+")";
                System.out.println(insert_string);
                statement.execute(insert_string);
            }
            statement.close();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }
}
