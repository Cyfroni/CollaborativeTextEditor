import socket
import time
from collections import deque
from Tkinter import *
import tkMessageBox
import time
from threading import Thread

HOST = '127.0.0.1'
PORT = 8181

sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
window = Tk()
queue = deque()


def sender(fun):
    def wrapper(message):
        #sock.makefile().flush()
        fun(message)
        #time.sleep(0.1)

    return wrapper


sock.send = sender(sock.send)


def what(args):
    ret = []
    for arg in args:
        ret.append([(elem, getattr(arg, elem)) for elem in dir(arg)])
    print(ret)


def center_window(_window, width=300, height=200):
    screen_width = _window.winfo_screenwidth()
    screen_height = _window.winfo_screenheight()
    x = (screen_width / 2) - (width / 2)
    y = (screen_height / 2) - (height / 2)
    _window.geometry('{}x{}+{}+{}'.format(width, height, x, y))


class Menu:
    def __init__(self, parent):

        def update_option_menu_button(event):
            if(self.open==False):
                self.update_option_menu(0)


        self.mother = None
        self.parent = parent
        self.options = set()
        self.om_variable = StringVar(self.parent)
        self.om_variable.trace('w', self.option_select)
        self.key = None
        self.range = None
        self.last = None
        self.lastDel = None
        self.text2 = None
        self.open=False
        self.om = OptionMenu(self.parent, self.om_variable,"")
        self.om.grid(column=0, row=0)
        self.om.config(width=10)
        self.create_button = Button(self.parent,width=15,bg="blue",text='Utworz dokument', command=self.create)
        self.create_button.grid(column=1, row=0)
        self.om.bind('<Button-1>', update_option_menu_button)
        self.update_mot()

    def ask_for_new_doc(self, _window, field):
        doc_name = field.get()
        if len(doc_name) == 0:
            return
        print(doc_name)
        sock.send("N" + doc_name)
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

    def option_select(self,*args):
        file_name = self.om_variable.get()
        if len(file_name) == 0:
            return
        self._window = Tk()
        sock.send("G" + file_name)
        self.parent.withdraw()
        self._window.protocol('WM_DELETE_WINDOW',
                             lambda: (self._window.destroy(), self.parent.deiconify(), sock.send("UG")))


        self.text2 = Text(self._window, height=150, width=120, undo=True)
        scroll = Scrollbar(self._window, command=self.text2.yview)
        self.text2.configure(yscrollcommand=scroll.set)

        self.text2.pack(side=LEFT, fill=Y)
        scroll.pack(side=RIGHT, fill=Y)

        def check(event):
            print("check:")
            self.last = self.text2.index(INSERT)
            self.lastDel = self.text2.index('insert+1c')
            print(self.last)
            last = self.key # TODO: trzeba dodac obluge przytrzymania ctrl - jak ktos wklei pare razy,
                            # albo wcisnie ctrl+x+v+...
                            # https://stackoverflow.com/questions/39606019/tkinter-using-two-keys-at-the-same-time?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
            self.key = event.keysym_num
            if self.key in {65288, 65535} or (self.key == 120 and last in {65507, 65508}):
                self.range = [x.string for x in self.text2.tag_ranges("sel")]
            print("Wszystko: ", self.last, self.lastDel, last, self.key, self.range)
            #self.text2.edit_reset()
            self.text2.edit_separator()

        def changed(event):
            if self.mother:
                self.text2.edit_modified(False)
                return
            print("Change1")
            flag = self.text2.edit_modified()

            if flag:
                print("changed2:")
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
                else:
                    try:

                        input = str(self.last) + "." + str(self.text2.index(INSERT)) + ":" + chr(self.key)
                    except:
                        print("unexpected char = ", self.key)
                        self.text2.edit_undo()

                if len(input) > 0:
                    print(input)
                    try:
                        self.text2.edit_undo()
                        sock.send("Z" + input)
                    except Exception as e:
                        print(e)
            self.text2.edit_modified(False)

        self.text2.focus_set()
        self.text2.bind('<Key>', check)
        self.text2.bind('<<Modified>>', changed)

        self.mother = True
        data = 'op'
        while not (len(data) == 0 or data[-1] == '\0'):
            data = sock.recv(100)
            self.text2.insert(END, data)
            print(data)
        self.text2.delete('end-2c')
        self.mother = False

        center_window(self._window,858, 300)

    def validate(self, action, index, value_if_allowed,
                       prior_value, text, validation_type, trigger_type, widget_name):

        if text not in '\\:?*<>|/\"' and len(value_if_allowed)<16:
            return True
        else:
            return False

    def create(self):
        print("create")
        self._window = Tk()
        self.parent.withdraw()
        self._window.protocol('WM_DELETE_WINDOW', lambda: None)
        vcmd = (self._window.register(self.validate),
                '%d', '%i', '%P', '%s', '%S', '%v', '%V', '%W')
        entry = Entry(self._window,validate = 'key', validatecommand = vcmd)

        ok = Button(self._window, text="OK", width=10, command=lambda: self.ask_for_new_doc(self._window, entry))
        back = Button(self._window, text="<-", width=10,
                      command=lambda: (self._window.destroy(), self.parent.deiconify()))
        entry.grid(columnspan=2)
        entry.focus_set()
        back.grid(column=0, row=1)
        ok.grid(column=1, row=1)
        center_window(self._window, 215, 48)

    def update_mot(self):
        try:
            info = queue.popleft()
	    if info == '\0':
		window.destroy()
		self._window.destroy()
            index, data = info.split(":", 1)
            index = index.split(".")
            index1 = index[0] + "." + index[1]
            index2 = index[2] + "." + index[3]
            print("indeksy", index, index1, index2, data)
            print("matka na true")
            self.mother = True
            if data == "":
                print("index1",index1)
                print("index2",index2)
                self.text2.delete(index1, index2)
            else:
                self.text2.insert(index1, data)
            print("matka na false")
            self.mother = False

        except:
            pass
        finally:
            window.after(1, self.update_mot)


class ClientThread(Thread):

    def __init__(self, socket, o_menu):
        self.o_menu = o_menu
        self.socket = socket
        Thread.__init__(self)

    def run(self):
        while True:
            print("czeka")
            info = self.socket.recv(100)
	    print("#", info)
	    queue.append(info)
            if info == '\0':
                break
	print("listening thread end")

try:

    sock.connect((HOST, PORT))
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    port = sock.recv(100)
    port = ord(port[1]) * 16 * 16 + ord(port[0])
    server_socket.bind((HOST, port))
    server_socket.listen(1)
    (client_socket, address) = server_socket.accept()
    o_menu = Menu(window)
    center_window(window, 273, 60)
    ct = ClientThread(client_socket, o_menu)
    ct.start()
    window.mainloop()
except Exception as e:
    print(e)
finally:
    print("Ending.. ")
    sock.close()

    server_socket.close()
    client_socket.close()
    ct._Thread__stop()
