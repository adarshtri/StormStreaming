from bokeh.layouts import layout
from bokeh.plotting import figure
from bokeh.models.annotations import Title
from bokeh.io import curdoc
from bokeh.models import ColumnDataSource
from bokeh.client import push_session
import datetime
from bokeh.models import HoverTool
import pandas as pd
import mysql.connector as sql
from bokeh.models.widgets import DataTable, DateFormatter, TableColumn

db_connection = sql.connect(host='localhost', database='kafkastormtest', user='root', password='720354M@noj')

data = pd.read_sql(con=db_connection,sql='''select c.name as c_id,
                                                sum(count) as cnt 
                                                from elm_campaign_summary ecs,
                                                campaigns c where c.id=ecs.campaign_id 
                                                group by 1 
                                                order by 2 desc 
                                                limit 1''')

db_connection.commit()

data2 = pd.read_sql(con=db_connection,sql='''select c.name as c_id,
case status when 0 then "Answered" when 201 then "Busy" when 200 then "Network Issue" else "Unknown" end as call_status,
sum(count) as cnt from elm_campaign_summary ecs, campaigns c where c.id=ecs.campaign_id group by 1,2
''')

db_connection.commit()

data_top_five = pd.read_sql(con=db_connection,sql='''select c.name as c_id,
                                                            sum(count) as cnt 
                                                            from elm_campaign_summary ecs,
                                                            campaigns c where c.id=ecs.campaign_id 
                                                            group by 1 
                                                            order by 2 desc,1  
                                                            limit 5''')

db_connection.commit()



title = list(data['c_id'])[0]

value1 = [0]*720
date = datetime.datetime.now()
time = [date]*720

top_five_campaign = list(data_top_five['c_id'])
top_five_value = list(data_top_five['cnt'])


source1 = ColumnDataSource(dict(time=time, value1=value1))
source2 = ColumnDataSource(dict(top_five_campaign=top_five_campaign,top_five_value=top_five_value))


hover = HoverTool(tooltips=[
    ("No. of calls", "$y"),

])

hover1 = HoverTool(tooltips=[
    ("Top Campaign", "$x"),

])


p1 = figure(sizing_mode='stretch_both', x_axis_type='datetime',title=title,
             toolbar_location=None, tools=[hover1])
p1.yaxis.major_label_orientation='vertical'
p1.line(source=source1, x='time', y='value1', line_width=2)


p2 = figure(x_range=top_five_campaign, sizing_mode = 'fixed', title = 'Top Five Campaigns')
p2.yaxis.major_label_orientation='vertical'
p2.vbar(source=source2, x='top_five_campaign', top='top_five_value', width=0.8)


source3 = ColumnDataSource(data2.to_dict(orient='List'))

columns = [
        TableColumn(field="c_id", title="Campaign ID"),
        TableColumn(field="cnt", title="Total Calls"),
        TableColumn(field="call_status", title="Call Status")
    ]
data_table = DataTable(source=source3, columns=columns, sizing_mode='stretch_both', width=1200, height=5000)


def update_data():

    global value1, time, p1
    data1 = pd.read_sql(con=db_connection,sql='''select  c.name as c_id,
                                                    sum(count) as cnt from 
                                                    elm_campaign_summary ecs, campaigns c
                                                    where ecs.campaign_id = c.id 
                                                    group by 1 
                                                    order by 2 desc 
                                                    limit 1;''')

    db_connection.commit()
    data2 = pd.read_sql(con=db_connection,sql='''select c.name as c_id,
                                                            sum(count) as cnt 
                                                            from elm_campaign_summary ecs,
                                                            campaigns c where c.id=ecs.campaign_id 
                                                            group by 1 
                                                            order by 2 desc, 1 
                                                            limit 5''')
    db_connection.commit()
    data_2 = pd.read_sql(con=db_connection, sql='''select c.name as c_id,     
                                                            case status 
                                                                when 0 then "Answered" 
                                                                when 201 then "Busy" 
                                                                when 200 then "Network Issue" 
                                                                else "Unknown" end as call_status,
                                                                sum(count) as cnt from 
                                                                elm_campaign_summary ecs, 
                                                                campaigns c where c.id=ecs.campaign_id 
                                                                group by 1,2
            ''')
    db_connection.commit()

    v1 = list(data1['cnt'])[0]
    value1 = value1[1:]
    value1.append(v1)
    time = time[1:]
    date1 = datetime.datetime.now()
    time.append(date1)
    t = Title()
    t.text = list(data1['c_id'])[0]
    #p1.title = t
    p1.title.text = t.text

    top_five_campaign1 = list(data2['c_id'])
    top_five_value1 = list(data2['cnt'])

    new_data1 = dict(time=time,value1=value1)
    new_data2 = dict(top_five_campaign=top_five_campaign1,top_five_value=top_five_value1)

    source1.stream(new_data1, 720)
    source2.stream(new_data2, len(top_five_campaign))
    source3.stream(data_2.to_dict(orient='list'),len(data_2.index))


plot = layout([p1, p2], [data_table], sizing_mode='stretch_both')

session = push_session(curdoc())
curdoc().add_root(plot)
curdoc().add_periodic_callback(update_data, 5000)
session.show(plot)
session.loop_until_closed(suppress_warning='Stop the python execution.')