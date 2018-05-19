import socket
import time
from collections import deque

HOST = '127.0.0.1'
PORT = 8080
sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
MAX_LENGTH = 10

#sheet = deque()

try:
    sock.connect((HOST, PORT))
    print(sock.recv(100))
    time.sleep(1)
    while True:
      sock.send(str(input()))
except Exception as e:
    print(e)
