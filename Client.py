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

MAX_LENGTH = 10

window = Tk()


def sender(fun):
    def wrapper(message):
        fun(message)
        time.sleep(0.1)

    return wrapper


sock.send = sender(sock.send)


def what(args):
    ret = []
    for arg in args:
        ret.append([(elem, getattr(arg, elem)) for elem in dir(arg)])
    return ret


def center_window(_window, width=300, height=200):
    screen_width = _window.winfo_screenwidth()
    screen_height = _window.winfo_screenheight()
    x = (screen_width / 2) - (width / 2)
    y = (screen_height / 2) - (height / 2)
    _window.geometry('{}x{}+{}+{}'.format(width, height, x, y))


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
        self.text2 = None
        self.om = OptionMenu(self.parent, self.om_variable, "")
        self.om.grid(column=0, row=0)
        self.create_button = Button(self.parent, text='Utworz dokument', command=self.create)
        self.create_button.grid(column=1, row=0)
        self.update_option_menu(0)

    def askForNewDoc(self, _window, e):
        docName = e.get()
        if len(docName) == 0:
            return
        print(docName)
        sock.send("N" + docName)
        self.update_option_menu(0)
        _window.destroy()
        self.parent.deiconify()

    def update_option_menu(self, data=None):
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
        textWindow.protocol('WM_DELETE_WINDOW', lambda: (textWindow.destroy(), self.parent.deiconify()))

        text1 = Text(textWindow, height=20, width=5)
        text1.insert(END, '\n')
        text1.pack(side=LEFT, fill=Y)

        self.text2 = Text(textWindow, height=20, width=50, undo=True)
        scroll = Scrollbar(textWindow, command=self.text2.yview)
        self.text2.configure(yscrollcommand=scroll.set)

        self.text2.pack(side=LEFT, fill=Y)
        scroll.pack(side=RIGHT, fill=Y)

        def check(event):
            print("check:")

            self.last = self.text2.index(INSERT)
            self.lastDel = self.text2.index('insert+1c')
            print(self.last)
            last = self.key
            self.key = event.keysym_num
            if self.key in {65288, 65535} or (self.key == 120 and last in {65507, 65508}):
                self.range = [x.string for x in self.text2.tag_ranges("sel")]

        def changed(event):

            flag = self.text2.edit_modified()
            if flag:
                print("changed:")
                input = ''
                if self.key == 65288:  # backspace
                    try:
                        input = str(self.range[0]) + "." + str(self.range[1]) + ":"
                    except:
                        input = str(self.text2.index(INSERT)) + "." + str(self.last) + ":"
                elif self.key == 65535:  # del
                    try:
                        input = str(self.range[0]) + "." + str(self.range[1]) + ":"
                    except:
                        input = str(self.text2.index(INSERT)) + "." + str(self.lastDel) + ":"
                elif self.key == 120:  # x
                    try:
                        input = str(self.range[0]) + "." + str(self.range[1]) + ":"
                    except:
                        input = str(self.last) + "." + str(self.text2.index(INSERT)) + ":x"
                elif self.key == 118:  # v
                    actual = self.text2.index(INSERT)
                    if actual != self.last:
                        input = str(self.last) + "." + str(actual) + ":" + self.text2.get(self.last, actual)
                    else:
                        input = str(self.last) + "." + str(self.text2.index(INSERT)) + ":v"
                elif self.key == 65293:
                    input = str(self.last) + "." + str(self.text2.index(INSERT)) + ":\n"
                elif self.key >= 0:
                    try:
                        input = str(self.last) + "." + str(self.text2.index(INSERT)) + ":" + chr(self.key)
                    except:
                        print("unexpected char = ", self.key)
                        self.text2.edit_undo()
                if len(input) > 0:
                    print(input)
                    try:
                        sock.send("Z" + input)
                    except:
                        pass
            self.text2.edit_modified(False)

        self.text2.focus_set()
        self.text2.bind('<Key>', check)
        self.text2.bind('<<Modified>>', changed)

        self.key = -1
        data = 'op'
        while not (len(data) == 0 or data[-1] == '\0'):
            data = sock.recv(100)
            self.text2.insert(END, data)
            print(data)
        self.text2.delete('end-2c')
        self.key = 0
        center_window(textWindow, 300, 100)

    def create(self):
        print("create")
        fileNameWindow = Tk()
        self.parent.withdraw()
        fileNameWindow.protocol('WM_DELETE_WINDOW', lambda: None)
        entry = Entry(fileNameWindow)
        ok = Button(fileNameWindow, text="OK", width=10, command=lambda: self.askForNewDoc(fileNameWindow, entry))
        back = Button(fileNameWindow, text="<-", width=10,
                      command=lambda: (fileNameWindow.destroy(), self.parent.deiconify()))
        entry.grid(columnspan=2)
        entry.focus_set()
        back.grid(column=0, row=1)
        ok.grid(column=1, row=1)
        center_window(fileNameWindow, 300, 100)

    def update(info):
        index, data = info.split(":")
        index = index.split(".")
        index1 = index[0] + "." + index[1]
        index2 = index[2] + "." + index[3]
        if data == "":
            self.text2.remove(index1, index2)
        else:
            self.text2.insert(index1, data)


class ClientThread(Thread):

    def __init__(self, socket, o_menu):
        self.o_menu = o_menu
        self.socket = socket
        Thread.__init__(self)

    def run(self):
        while True:
            self.o_menu.update(self.socket.recv(100))


try:
    sock.connect((HOST, PORT))
    serversocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    port = sock.recv(100)
    port = ord(port[1]) * 16 * 16 + ord(port[0])
    serversocket.bind((HOST, port))
    serversocket.listen(1)
    (clientsocket, address) = serversocket.accept()
    o_menu = optionmenu(window)
    center_window(window, 300, 100)
    ct = ClientThread(clientsocket, o_menu)
    ct.start()

    window.mainloop()
except Exception as e:
    print(e)
