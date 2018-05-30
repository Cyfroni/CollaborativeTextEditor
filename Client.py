import socket
import time
from collections import deque
from Tkinter import *
import tkMessageBox
import time
from threading import Thread

HOST = '127.0.0.1'
PORT = 8080
sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
HOST1 = '127.0.0.1'
PORT1 = 8090
sock1 = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
MAX_LENGTH = 10

window = Tk()

#@sock.send
def sender(fun):
    def wrapper(message):
        fun(message)
        time.sleep(0.1)
    return wrapper

sock.send = sender(sock.send)

def what(args):
    ret = []
    for arg in args:
        ret.append([(elem, getattr(arg,elem)) for elem in dir(arg)])
    return ret


def center_window(_window,width=300, height=200):
    # get screen width and height
    screen_width = _window.winfo_screenwidth()
    screen_height = _window.winfo_screenheight()
    # calculate position x and y coordinates
    x = (screen_width/2) - (width/2)
    y = (screen_height/2) - (height/2)
    _window.geometry('{}x{}+{}+{}'.format(width,height,x,y))

class optionmenu():
    def __init__(self, parent):

        self.parent = parent
        self.options = set()
        self.om_variable = StringVar(self.parent)
        self.om_variable.trace('w', self.option_select)
        self.key = None
        self.range = None
        self.last = None
        self.lastDel = None
        self.om = OptionMenu(self.parent, self.om_variable, "")
        self.om.grid(column=0, row=0)
        self.create_button = Button(self.parent, text='Utworz dokument', command=self.create)
        self.create_button.grid(column=1, row=0)
        self.update_option_menu(0)


    def askForNewDoc(self,_window,e):
        docName = e.get()
        if len(docName) == 0:
            return
        #sock.send("nowy")
        print(docName)
        sock.send("N" + docName)
        self.update_option_menu([docName + ".txt"])
        _window.destroy()
        self.parent.deiconify()

    def update_option_menu(self, data = None):
         if data == 0:
             sock.send("UP")
             data = sock.recv(100).split("\n")
             print(data)
         for option in data:
             if option not in self.options:
                 self.options.add(option)
         if len(self.options) == 0:
             self.options.add("")
         elif len(self.options) > 1 and "" in self.options:
             self.options.remove("")
         menu = self.om["menu"]
         menu.delete(0, "end")
         for string in self.options:
             menu.add_command(label=string,
                              command=lambda value=string: self.om_variable.set(value))


    def option_select(self, *args):
        fileName = self.om_variable.get()
        textWindow = Tk()
        sock.send("G" + fileName)
        self.parent.withdraw()
        textWindow.protocol('WM_DELETE_WINDOW',lambda: (textWindow.destroy(), self.parent.deiconify()))

        text1 = Text(textWindow, height=20, width=5)
        text1.insert(END,'\n')
        text1.pack(side=LEFT,fill=Y)

        text2 = Text(textWindow, height=20, width=50, undo=True)
        scroll = Scrollbar(textWindow, command=text2.yview)
        text2.configure(yscrollcommand=scroll.set)

        text2.pack(side=LEFT, fill=Y)
        scroll.pack(side=RIGHT, fill=Y)

        def check(event):
            print("check:")

            self.last = text2.index(INSERT)
            self.lastDel = text2.index('insert+1c')
            print(self.last)
            last = self.key
            self.key = event.keysym_num
            if self.key in {65288, 65535} or (self.key == 120 and last in {65507, 65508}):
                self.range=[x.string for x in text2.tag_ranges("sel")]


        def changed(event):

            flag = text2.edit_modified()
            if flag:
                print("changed:")
                input = ''
                if self.key == 65288: #backspace
                    try:
                        input = str(self.range[0])+"."+str(self.range[1])+":"
                    except:
                        input = str(text2.index(INSERT))+"." + str(self.last) + ":"
                elif self.key == 65535: #del
                    try:
                        input = str(self.range[0])+"."+str(self.range[1])+":"
                    except:
                        input = str(text2.index(INSERT))+"." + str(self.lastDel) + ":"
                elif self.key == 120: #x
                    try:
                        input = str(self.range[0])+"."+str(self.range[1])+":"
                    except:
                        input = str(self.last)+"."+ str(text2.index(INSERT))+":x"
                elif self.key == 118: #v
                    actual = text2.index(INSERT)
                    if actual != self.last:
                        input = str(self.last)+"."+str(actual) + ":" + text2.get(self.last,actual)
                    else:
                        input = str(self.last)+"."+ str(text2.index(INSERT))+":v"
                elif self.key == 65293:
                    input = str(self.last)+"."+ str(text2.index(INSERT))+":\n"
                elif self.key >= 0:
                    try:
                        input = str(self.last)+"."+ str(text2.index(INSERT))+":" + chr(self.key)
                    except:
                        print("unexpected char = ", self.key)
                        text2.edit_undo()
                if len(input) > 0:
                    print(input)
                    try:
                        sock.send("Z" + input)
                    except:
                        pass
            text2.edit_modified(False)

        text2.focus_set()
        text2.bind('<Key>', check)
        text2.bind('<<Modified>>', changed)

        self.key = -1
        data = 'op'
        while not (len(data) == 0 or data[-1] == '\0'):
            data = sock.recv(100)
            text2.insert(END, data)
            print(data)
        text2.delete('end-2c')
        self.key = 0
        center_window(textWindow,300,100)

    def create(self):
        print("create")
        fileNameWindow=Tk()
        self.parent.withdraw()
        fileNameWindow.protocol('WM_DELETE_WINDOW',lambda : None)
        entry = Entry(fileNameWindow)
        ok = Button(fileNameWindow, text = "OK", width = 10, command = lambda: self.askForNewDoc(fileNameWindow,entry))
        back = Button(fileNameWindow, text = "<-", width = 10, command = lambda: (fileNameWindow.destroy(), self.parent.deiconify()))
        entry.grid(columnspan=2)
        entry.focus_set()
        back.grid(column=0, row=1)
        ok.grid(column=1, row=1)
        center_window(fileNameWindow,300,100)
def receive():
    while 1:
        print(sock1.recv(1000))

try:
    sock.connect((HOST, PORT))
    #sock1.connect((HOST1,PORT1))
    print(sock.recv(100))
    optionmenu(window)
    center_window(window,300,100)
    #thread1=Thread(target=receive)
    #thread1.start()
    #thread1.join()
    window.mainloop()

except Exception as e:
    print(e)
