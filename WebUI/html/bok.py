from datetime import date
from random import randint

from bokeh.models import ColumnDataSource
from bokeh.models.widgets import DataTable, DateFormatter, TableColumn
from bokeh.io import output_file, show

from bokeh.io import curdoc
from bokeh.models import ColumnDataSource
from bokeh.plotting import figure
from bokeh.client import push_session
import numpy as np
import pandas as pd
import mysql.connector as sql


output_file("data_table.html")

'''data = dict(
        dates=[date(2014, 3, i+1) for i in range(10)],
        downloads=[randint(0, 100) for i in range(10)],
    )'''

db_connection = sql.connect(host='localhost', database='kafkastormtest', user='root', password='720354M@noj')
data = pd.read_sql(con=db_connection,sql='''select campaign_id as c_id,
case status when 0 then "Answered" when 201 then "Busy" when 200 then "Network Issue" else "Unknown" end as call_status,
sum(count) as cnt from elm_campaign_summary group by 1,2
''')

db_connection.commit()

def update_data():
    #db_connection1 = sql.connect(host='localhost', database='kafkastormtest', user='root', password='720354M@noj')
    data1 = pd.read_sql(con=db_connection, sql='''select c.name as c_id,     
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
    print(data1)
    source.stream(data1.to_dict(orient='list'),len(data1.index))


source = ColumnDataSource(data.to_dict(orient='List'))

columns = [
        TableColumn(field="c_id", title="Campaign ID"),
        TableColumn(field="cnt", title="Total Calls"),
        TableColumn(field="call_status", title="Call Status")
    ]
data_table = DataTable(source=source, columns=columns, width=500, height=1000)

session = push_session(curdoc())
curdoc().add_root(data_table)
curdoc().add_periodic_callback(update_data, 3000)
session.show(data_table)
session.loop_until_closed(suppress_warning='Stop the python execution.')

#show(data_table)
