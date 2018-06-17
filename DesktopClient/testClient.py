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
import select

HOST = '127.0.0.1'
PORT = 8181
queue = deque()
msgLen=4096
sec=0.2
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
        time.sleep(sec)
        sock.send("UP")
        data = sock.recv(msgLen).split("\n")
        self.assertTrue(set(data) == set(['123.txt', 'test1.txt', '']))

    def test02_NewDoc2(self):
        docName="test1"
        sock.send("N" + docName)
        time.sleep(sec)
        sock.send("N" + docName)
        time.sleep(sec)
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
        time.sleep(sec)
        data1=sock.recv(msgLen)
        self.assertTrue(set(str(data1))==set(chr(0)))


    def test05_Writing_letter(self):
        sock.send("Z"+"1.0.1.1:A")
        time.sleep(sec)
        info=queue.popleft()
        index,data=info.split(":")
        index=index.split(".")
        index1=index[0]+"."+index[1]
        index2=index[2]+"."+index[3]
        self.assertTrue(index1=="1.0" and index2=="1.1" and set(str(data))==set('A'))

    def test06_Writing_two_letterts(self):
        sock.send("Z"+"1.1.1.3:BC")
        time.sleep(sec)
        info=queue.popleft()
        index,data=info.split(":")
        index=index.split(".")
        index1=index[0]+"."+index[1]
        index2=index[2]+"."+index[3]
        self.assertTrue(index1=="1.1" and index2=="1.3" and str(data)==("BC"))

    def test07_Writing_letter_space_letter(self):
        sock.send("Z"+"1.3.1.6:d e")
        time.sleep(sec)
        info=queue.popleft()
        index,data=info.split(":")
        index=index.split(".")
        index1=index[0]+"."+index[1]
        index2=index[2]+"."+index[3]
        self.assertTrue(index1=="1.3" and index2=="1.6" and str(data)==("d e"))

    def test08_exit_enter_check(self):
        data2="ABCd e"
        sock.send("UG")
        time.sleep(sec)
        sock.send("G"+"test1.txt")
        time.sleep(sec)
        data1=sock.recv(msgLen)
        self.assertTrue(str(data1)==(str(data2)+chr(0)))

    def test09_Writing_delete_last_letter(self):
        data2="ABCd "
        sock.send("Z"+"1.5.1.6:")
        time.sleep(sec)
        sock.send("UG")
        time.sleep(sec)
        sock.send("G"+"test1.txt")
        time.sleep(sec)
        data1=sock.recv(msgLen)
        self.assertTrue(str(data1)==(str(data2)+chr(0)))

    def test10_Writing_new_line(self):#dodac z enterem
        data2="ABCd \n"
        sock.send("Z"+"1.5.2.0:\n")
        time.sleep(sec)
        sock.send("UG")
        time.sleep(sec)
        sock.send("G"+"test1.txt")
        time.sleep(sec)
        data1=sock.recv(msgLen)
        self.assertTrue(str(data1)==(str(data2)+chr(0)))

    def test11_Writing(self):
        data2="ABCd \nFGH\n"
        sock.send("Z"+"2.0.3.0:FGH\n")
        time.sleep(sec)
        sock.send("UG")
        time.sleep(sec)
        sock.send("G"+"test1.txt")
        time.sleep(sec)
        data1=sock.recv(msgLen)
        self.assertTrue(str(data1)==(str(data2)+chr(0)))

    def test12_Writing(self):
        data2="ABCd \n\n"
        sock.send("Z"+"2.0.2.3:")
        time.sleep(sec)
        sock.send("UG")
        time.sleep(sec)
        sock.send("G"+"test1.txt")
        time.sleep(sec)
        data1=sock.recv(msgLen)
        self.assertTrue(str(data1)==(str(data2)+chr(0)))

    def test13_Writing(self):
        data2="ABCd "
        sock.send("Z"+"1.5.3.0:")
        time.sleep(sec)
        sock.send("UG")
        time.sleep(sec)
        sock.send("G"+"test1.txt")
        time.sleep(sec)
        data1=sock.recv(msgLen)
        self.assertTrue(str(data1)==(str(data2)+chr(0)))

    def test13_Writing(self):
        data2="ABCd "
        sock.send("Z"+"1.5.3.0:")
        time.sleep(sec)
        sock.send("UG")
        time.sleep(sec)
        sock.send("G"+"test1.txt")
        time.sleep(sec)
        data1=sock.recv(msgLen)
        self.assertTrue(str(data1)==(str(data2)+chr(0)))

    def test14_Writing(self):
        data2="ABCd "
        sock.send("Z"+"1.5.3.0:")
        time.sleep(sec)
        sock.send("UG")
        time.sleep(sec)
        sock.send("G"+"test1.txt")
        time.sleep(sec)
        data1=sock.recv(msgLen)
        self.assertTrue(str(data1)==(str(data2)+chr(0)))

    def test15_Writing(self):
        data2="ABC"
        sock.send("Z"+"1.3.1.5:")
        time.sleep(sec)
        sock.send("UG")
        time.sleep(sec)
        sock.send("G"+"test1.txt")
        time.sleep(sec)
        data1=sock.recv(msgLen)
        self.assertTrue(str(data1)==(str(data2)+chr(0)))

    def test16_Writing(self):
        data2="ABBBC"
        sock.send("Z"+"1.1.1.3:BB")
        time.sleep(sec)
        sock.send("UG")
        time.sleep(sec)
        sock.send("G"+"test1.txt")
        time.sleep(sec)
        data1=sock.recv(msgLen)
        self.assertTrue(str(data1)==(str(data2)+chr(0)))

    def test17_Writing(self):
        data2="ABBBC<1&2/\|~$3*>"
        sock.send("Z"+"1.5.1.17:<1&2/\|~$3*>")
        time.sleep(sec)
        sock.send("UG")
        time.sleep(sec)
        sock.send("G"+"test1.txt")
        time.sleep(sec)
        data1=sock.recv(msgLen)
        self.assertTrue(str(data1)==(str(data2)+chr(0)))

    def test18_HACK(self):
        data2="ABBBC<1&2/\|~$3*>Z1.17.1.28:"
        sock.send("Z"+"1.17.1.28:Z1.17.1.28:")
        time.sleep(sec)
        sock.send("UG")
        time.sleep(sec)
        sock.send("G"+"test1.txt")
        time.sleep(sec)
        data1=sock.recv(msgLen)
        self.assertTrue(str(data1)==(str(data2)+chr(0)))

    def test19_badinput(self):
        data2=""
        sock.send("")
        sock.setblocking(0)
        check=0
        ready = select.select([sock], [], [], 1)
        if ready[0]:
            data1=sock.recv(msgLen)
        else:
            check=1
        self.assertTrue(check==1)

    def test20_badinput(self):
        data2=""
        sock.send("aaaa")
        sock.setblocking(0)
        check=0
        ready = select.select([sock], [], [], 1)
        if ready[0]:
            data1=sock.recv(msgLen)
        else:
            check=1
        self.assertTrue(check==1)

    def test21_badinput(self):
        data2=""
        sock.send("1. .1.1:2")
        check=0
        data1=""
        ready = select.select([sock], [], [], 1)
        if ready[0]:
            data1=sock.recv(msgLen)
        else:
            check=1
        self.assertTrue(check==1)

    def test22_badinput(self):
        data2=""
        sock.send("1.0 .1.1:afg")

        check=0
        ready = select.select([sock], [], [], 1)
        if ready[0]:
            data1=sock.recv(msgLen)
        else:
            check=1
        self.assertTrue(check==1)

    def test23_badinput(self):
        data2=""
        sock.send("1.0.1.1;bad")

        check=0
        ready = select.select([sock], [], [], 1)
        if ready[0]:
            data1=sock.recv(msgLen)
        else:
            check=1
        self.assertTrue(check==1)

    def test24_badinput(self):
        data2=""
        sock.send("Z1.0.1.1:b")

        check=0
        ready = select.select([sock], [], [], 1)
        if ready[0]:
            data1=sock.recv(msgLen)
        else:
            check=1
        self.assertTrue(check==1)

    def test25_delete_rest(self):
        data2=""
        sock.send("Z"+"1.0.1.29:")
        time.sleep(sec)
        sock.send("UG")
        time.sleep(sec)
        sock.send("G"+"test1.txt")
        time.sleep(sec)
        data1=sock.recv(msgLen)
        self.assertTrue(str(data1)==(str(data2)+chr(0)))

    def test99_close(self):
        time.sleep(sec)
        sock.close()
        server_socket.close()
        client_socket3.close()
        ct._Thread__stop()

if __name__ == '__main__':
    unittest.main()
