import socket
import time
from collections import deque
from Tkinter import *
import tkMessageBox
import time
from threading import Thread
import unittest
import os
import signal

HOST = '127.0.0.1'
PORT = 8181
queue = deque()
msgLen=4096
"""
class TestConnection(unittest.TestCase):

    def test_server_connects(self):
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.connect((HOST, PORT))
        server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        port = sock.recv(msgLen)
        port = ord(port[1]) * 16 * 16 + ord(port[0])
        server_socket.bind((HOST, port))
        server_socket.listen(1)
        (client_socket, address) = server_socket.accept()
        sock.close()
        server_socket.close()
        client_socket.close()
"""

class ClientThread(Thread):

    def __init__(self, socket):
        self.socket = socket
        Thread.__init__(self)

    def run(self):
        while True:
            #print("czeka")
            info = self.socket.recv(msgLen)
            if info == '':
            #    print("infobot")
                break
            #print("#", info)
            queue.append(info)


def otherClientProgram():
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.connect((HOST, PORT))
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    port = sock.recv(msgLen)
    port = ord(port[1]) * 16 * 16 + ord(port[0])
    server_socket.bind((HOST, port))
    server_socket.listen(1)
    (client_socket2, address2) = server_socket.accept()

    docName="test1"
    sock.send("N" + docName)

    sock.close()
    server_socket.close()
    client_socket2.close()
    #print(os.getpid())
    os.kill(os.getpid(), signal.SIGKILL)

sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
sock.connect((HOST, PORT))
server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
port = sock.recv(msgLen)
port = ord(port[1]) * 16 * 16 + ord(port[0])
server_socket.bind((HOST, port))
server_socket.listen(1)
(client_socket3, address3) = server_socket.accept()
ct = ClientThread(client_socket3)
ct.start()





class TestStringMethods(unittest.TestCase):

    def test00_portReceive(self):
        self.assertTrue( port >= 9000)

    def test01_NewDoc1(self):
        docName="test1"
        sock.send("N" + docName)
        time.sleep(0.1)
        sock.send("UP")
        data = sock.recv(msgLen).split("\n")
        self.assertTrue(set(data) == set(['123.txt', 'test1.txt', '']))

    def test02_NewDoc2(self):
        docName="test1"
        sock.send("N" + docName)
        time.sleep(0.1)
        sock.send("N" + docName)
        time.sleep(0.1)
        sock.send("UP")
        data = sock.recv(msgLen).split("\n")
        self.assertTrue(set(data) == set(['123.txt', 'test1.txt', '']))

    def test03_NewDoc3(self):
        newpid = os.fork()
        if(newpid==0):
            otherClientProgram()
        #    print("exit nie dziala")

        time.sleep(1.0)
        sock.send("UP")
        data = sock.recv(msgLen).split("\n")
        self.assertTrue(set(data) == set(['123.txt', 'test1.txt', '']))

    def test04_Open_Doc(self):
        sock.send("G"+"test1.txt")
        time.sleep(0.1)
        data1=sock.recv(msgLen)
        self.assertTrue(set(str(data1))==set(chr(10)+chr(0)))


    def test05_Writing_letter(self):
        sock.send("Z"+"1.0.1.1:A")
        time.sleep(0.1)
        info=queue.popleft()
        index,data=info.split(":")
        index=index.split(".")
        index1=index[0]+"."+index[1]
        index2=index[2]+"."+index[3]
        self.assertTrue(index1=="1.0" and index2=="1.1" and set(str(data))==set('A'))

    def test06_Writing_two_letterts(self):
        sock.send("Z"+"1.1.1.3:BC")
        time.sleep(0.1)
        info=queue.popleft()
        index,data=info.split(":")
        index=index.split(".")
        index1=index[0]+"."+index[1]
        index2=index[2]+"."+index[3]
        self.assertTrue(index1=="1.1" and index2=="1.3" and str(data)==("BC"))

    def test07_Writing_letter_space_letter(self):
        sock.send("Z"+"1.3.1.6:d e")
        time.sleep(0.1)
        info=queue.popleft()
        index,data=info.split(":")
        index=index.split(".")
        index1=index[0]+"."+index[1]
        index2=index[2]+"."+index[3]
        self.assertTrue(index1=="1.3" and index2=="1.6" and str(data)==("d e"))

    def test08_exit_enter_check(self):
        data2="ABCd e"
        sock.send("UG")
        time.sleep(0.1)
        sock.send("G"+"test1.txt")
        time.sleep(0.1)
        data1=sock.recv(msgLen)
        self.assertTrue(str(data1)==(str(data2)+chr(10)+chr(0)))

    def test09_Writing_delete_last_letter(self):
        data2="ABCd "
        sock.send("Z"+"1.5.1.6:")
        time.sleep(0.1)
        sock.send("UG")
        time.sleep(0.1)
        sock.send("G"+"test1.txt")
        time.sleep(0.1)
        data1=sock.recv(msgLen)
        self.assertTrue(str(data1)==(str(data2)+chr(10)+chr(0)))

    def test10_Writing_new_line(self):#dodac z enterem
        data2="ABCd \n"
        sock.send("Z"+"1.5.2.0:\n")
        time.sleep(0.1)
        sock.send("UG")
        time.sleep(0.1)
        sock.send("G"+"test1.txt")
        time.sleep(0.1)
        data1=sock.recv(msgLen)
        self.assertTrue(str(data1)==(str(data2)+chr(10)+chr(0)))


    def test99_close(self):

        sock.close()
        server_socket.close()
        client_socket3.close()
        ct._Thread__stop()


if __name__ == '__main__':
    unittest.main()
