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
        self.index = ["1.0","1.0",0]
        self.range = ""
        self.om = OptionMenu(self.parent, self.om_variable, "")
        self.om.grid(column=0, row=0)

        self.create_button = Button(self.parent, text='Utworz dokument', command=self.create)
        #self.update_button = Button(self.parent, text='Update', command=self.update_option_menu)
        #self.update_button.grid(column=0, row=1)
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
             sock.send("U")
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
        #sock.send("get")
        sock.send("G" + fileName)
        self.parent.withdraw()
        textWindow.protocol('WM_DELETE_WINDOW',lambda: (textWindow.destroy(), self.parent.deiconify()))




        text1 = Text(textWindow, height=20, width=5)
        text1.insert(END,'\n')
        text1.pack(side=LEFT,fill=Y)


        text2 = Text(textWindow, height=20, width=50)
        scroll = Scrollbar(textWindow, command=text2.yview)
        text2.configure(yscrollcommand=scroll.set)

        #e.grid()


        # text2.tag_configure('bold_italics', font=('Arial', 12, 'bold', 'italic'))
        # text2.tag_configure('big', font=('Verdana', 20, 'bold'))
        # text2.tag_configure('color', foreground='#476042',
        # 					font=('Tempus Sans ITC', 12, 'bold'))
        # text2.tag_bind('follow', '<1>', lambda e, t=text2: t.insert(END, "Not now, maybe later!"))
        # text2.insert(END,'\nWilliam Shakespeare\n', 'big')
        # quote = """
        # To be, or not to be that is the question:
        # Whether 'tis Nobler in the mind to suffer
        # The Slings and Arrows of outrageous Fortune,
        # Or to take Arms against a Sea of troubles,
        # """
        # text2.insert(END, quote, 'color')
        # text2.insert(END, 'follow-up\n', 'follow')
        text2.pack(side=LEFT, fill=Y)
        scroll.pack(side=RIGHT, fill=Y)

        def elo():
            input=text2.get(self.index[0],self.index[1])
            print(input)
            #send()

        def changed(*args):
            flag = text2.edit_modified()
            #print(flag)
            if flag:     # prevent from getting called twice
                print("changed:")
                #print(event.char
                self.index[1]=text2.index(INSERT)
                #elo()
                #print(index1)
                print(self.index, self.range)

                #print(text2.get(1.0,END))
            ## reset so this will be called on the next change
            text2.edit_modified(False)

        def check(event):
            print("check:")
            self.index[2] = event.keysym_num
            self.index[0]=text2.index(INSERT)
            input=str(self.index[0])+"-"+str(self.index[1])+"-"+str(self.index[2])
            if(int(self.index[2]) <=127 or int(self.index[2]) in [ 65288, 65535,65293]):
                sock.send(input)
            #input=text2.get(index+"-1c",END)
            #print(input)
            #print(what(text2.tag_ranges("sel"))
            self.range=[x.string for x in text2.tag_ranges("sel")]
            print(self.range)








        text2.focus_set()
        text2.bind('<Key>', check)
        text2.bind('<<Modified>>', changed)

        data = 'op'
        while not (len(data) == 0 or data[-1] == '\0'):
            data = sock.recv(100)
            text2.insert(END, data)
            print(data)
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
