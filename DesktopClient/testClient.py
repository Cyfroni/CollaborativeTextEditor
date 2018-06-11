import socket
import time
from collections import deque
from Tkinter import *
import tkMessageBox
import time
from threading import Thread
import unittest
HOST = '127.0.0.1'
PORT = 8181
queue = deque()

class TestConnection(unittest.TestCase):

    def test_server_connects(self):
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.connect((HOST, PORT))
        server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        port = sock.recv(100)
        port = ord(port[1]) * 16 * 16 + ord(port[0])
        server_socket.bind((HOST, port))
        server_socket.listen(1)
        (client_socket, address) = server_socket.accept()
        sock.close()
        server_socket.close()
        client_socket.close()

class ClientThread(Thread):

    def __init__(self, socket, o_menu):
        self.o_menu = o_menu
        self.socket = socket
        Thread.__init__(self)

    def run(self):
        while True:
            print("czeka")
            info = self.socket.recv(100)
            if info == '':
                break
            print("#", info)
            queue.append(info)

sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
sock.connect((HOST, PORT))
server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
port = sock.recv(100)
port = ord(port[1]) * 16 * 16 + ord(port[0])
server_socket.bind((HOST, port))
server_socket.listen(1)
(client_socket, address) = server_socket.accept()
o_menu="hej"
ct = ClientThread(client_socket, o_menu)
ct.start()




class TestStringMethods(unittest.TestCase):

    def test_portReceive(self):
        self.assertTrue( port in {9000,9001,9002,9003,9004,9005,9006})

    def test_NewDoc(self):
        docName="test1"
        sock.send("N" + docName)
        time.sleep(0.3)
        sock.send("UP")
        data = sock.recv(100).split("\n")
        self.assertTrue(data==['123.txt', 'test1.txt', ''])

#sock.close()
#server_socket.close()
#client_socket.close()
if __name__ == '__main__':
    unittest.main()
