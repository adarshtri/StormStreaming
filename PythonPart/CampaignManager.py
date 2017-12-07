import tkinter
from tkinter import *
import requests

win = tkinter.Tk()
win.title("Campaign Manager")
win.minsize(width=550,height=75)

L1 = Label(win, text="Enter Campaign Name")
L1.pack(side=LEFT)


def buttoncallback():

    url = "http://localhost:5000/postRequest"
    data = '''{"key":"campaign","value":"'''+E1.get()+'''"}'''
    requests.post(url=url, data=data, headers={"Content-Type": "application/json"})


B = tkinter.Button(win, text="Go Ahead", command=buttoncallback)
B.pack(side=RIGHT)

E1 = Entry(win, bd=8, width=35)
E1.pack(side=RIGHT)

win.mainloop()
