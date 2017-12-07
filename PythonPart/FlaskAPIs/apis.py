from flask import Flask,request,jsonify
import FlaskAPIs.SingletonProducer as SP
import json
import ast

app = Flask(__name__)


@app.route('/postRequest',methods=['POST'])
def postFunction():
        key,value = getKeyValue(request)
        producerInst = SP.SingleTonProducer.Instance()
        producer = producerInst.getProducer()
        producer.send('test_topic',key=key.encode(),value=value.encode())
        return "Post Function Called."+key+value


@app.route('/getRequest',methods=['GET'])
def getFunction():
    return "Get Function Called."


def getKeyValue(request):
        string_data = request.json
        json_data = ast.literal_eval(json.dumps(string_data))
        key = json_data["key"]
        value = json_data["value"]
        return key,value



if __name__=='__main__':
	app.debug = True
	app.run(host="localhost", port=5000)
