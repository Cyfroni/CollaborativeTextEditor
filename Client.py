import socket
import time
from collections import deque
from Tkinter import *
import tkMessageBox
import time


HOST = '127.0.0.1'
PORT = 8080
sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
MAX_LENGTH = 10

window = Tk()


#@sock.send
def sender(fun):
    def wrapper(message):
        fun(message)
        time.sleep(0.1)
    return wrapper

sock.send = sender(sock.send)


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
        sock.send("nowy")
        print(docName)
        sock.send(docName)
        self.update_option_menu([docName + ".txt"])
        _window.destroy()
        self.parent.deiconify()

    def update_option_menu(self, data = None):
         if data == 0:
             sock.send("update")
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
        print(self.om_variable.get())


    def create(self):
        print("create")
        FileNameWindow=Tk()
        self.parent.withdraw()
        FileNameWindow.protocol('WM_DELETE_WINDOW',lambda : None)
        entry = Entry(FileNameWindow)
        ok = Button(FileNameWindow, text = "OK", width = 10, command = lambda: self.askForNewDoc(FileNameWindow,entry))
        back = Button(FileNameWindow, text = "<-", width = 10, command = lambda: (FileNameWindow.destroy(), self.parent.deiconify()))
        entry.grid(columnspan=2)
        entry.focus_set()
        back.grid(column=0, row=1)
        ok.grid(column=1, row=1)

        center_window(FileNameWindow,300,100)

try:
    sock.connect((HOST, PORT))
    print(sock.recv(100))
    optionmenu(window)
    center_window(window,300,100)
    window.mainloop()

except Exception as e:
    print(e)
