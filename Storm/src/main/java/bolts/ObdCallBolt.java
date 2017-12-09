package bolts;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;

import org.apache.storm.topology.base.BaseWindowedBolt;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.apache.storm.windowing.TupleWindow;
import pojos.ObdCall;

import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class  ObdCallBolt extends BaseWindowedBolt {

    private OutputCollector outputCollector;
    private Connection connection;
    private Statement statement;

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {


        this.outputCollector = outputCollector;

        try{

            connection = DriverManager.getConnection("jdbc:mysql://localhost/kafkastormtest?" +
                    "user=root&password=720354M@noj");
            statement = connection.createStatement();

        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    @Override
    public void execute(TupleWindow tupleWindow){
        try {
            connection.setAutoCommit(false);
        }catch (SQLException e){
            e.printStackTrace();
        }

        List<ObdCall> obdCalls = new ArrayList<>();

        for(Tuple tuple : tupleWindow.get()){

            String obd_call_string = tuple.getString(0);
            String[] obd = obd_call_string.split("\t");
            ObdCall obdCall = new ObdCall(obd[0],obd[1],obd[2],obd[3]);
            obdCalls.add(obdCall);
        }

        try {

            PreparedStatement preparedStatement = connection.prepareStatement(
                    "insert into obd_call_log(campaign_id,attempts,status,call_time) values(?,?,?,?)"
            );

            Iterator<ObdCall> iterator = obdCalls.iterator();
            while(iterator.hasNext()){
                ObdCall call = iterator.next();

                preparedStatement.setString(1,call.getCampaign_id());
                preparedStatement.setString(2,call.getAttempts());
                preparedStatement.setString(3,call.getStatus());
                preparedStatement.setString(4,call.getCallTime());
                preparedStatement.addBatch();
            }

            int [] numUpdates = preparedStatement.executeBatch();

            for (int i=0; i < numUpdates.length; i++) {
                if (numUpdates[i] == -2)
                    System.out.println("Execution " + i +
                            ": unknown number of rows updated");
                else
                    System.out.println("Execution " + i +
                            "successful: " + numUpdates[i] + " rows updated");
            }

            statement.execute("insert into elm_campaign_summary (select date_format(call_time,\"%Y-%m-%d %H:00:00\"),campaign_id,attempts,status,count(*) from obd_call_log group by 1,2,3,4)");
            statement.execute("delete from obd_call_log");
            connection.commit();
        }catch (SQLException e){
            e.printStackTrace();
        }


        outputCollector.emit(new Values(obdCalls));
    }



}
