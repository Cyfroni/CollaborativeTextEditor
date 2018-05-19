/*
    ** server.c -- serwer używający gniazd strumieniowych
    */

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <sys/wait.h>
#include <signal.h>
#include <deque>

#define MYPORT 8080    // port, z którym będą się łączyli użytkownicy

#define BACKLOG 10     // jak dużo możę być oczekujących połączeń w kolejce

#define MAX_LENGTH 10

using namespace std;

void sigchld_handler(int s)
{
    while(wait(NULL) > 0);
}


int main()
{
    int sockfd, new_fd; // nasłuchuj na sock_fd, nowe połaczenia na new_fd
    struct sockaddr_in my_addr; // informacja o moim adresie
    struct sockaddr_in their_addr; // informacja o adresie osoby łączącej się
    unsigned int sin_size;
    struct sigaction sa;
    int yes = 1;

    //deque<deque<char> > sheet;

    if((sockfd = socket(AF_INET, SOCK_STREAM, 0)) == -1)
    {
        perror("socket");
        exit(1);
    }

    if(setsockopt(sockfd, SOL_SOCKET, SO_REUSEADDR, &yes, sizeof(int)) == -1)
    {
        perror("setsockopt");
        exit(1);
    }

    my_addr.sin_family = AF_INET; // host byte order
    my_addr.sin_port = htons(MYPORT); // short, network byte order
    my_addr.sin_addr.s_addr = INADDR_ANY; // uzupełnij moim adresem IP
    memset(&( my_addr.sin_zero ), '\0', 8); // wyzeruj resztę struktury

    if(bind(sockfd,(struct sockaddr *)&my_addr, sizeof(struct sockaddr)) == -1)
    {
        perror("bind");
        exit(1);
    }

    if(listen(sockfd, BACKLOG) == -1)
    {
        perror("listen");
        exit(1);
    }

    sa.sa_handler = sigchld_handler; // zbierz martwe procesy
    sigemptyset( & sa.sa_mask );
    sa.sa_flags = SA_RESTART;
    if(sigaction(SIGCHLD, & sa, NULL) == -1)
    {
        perror("sigaction");
        exit(1);
    }

    while(1)
    { // głowna pętla accept()
        sin_size = sizeof(struct sockaddr_in);
        if((new_fd = accept(sockfd,(struct sockaddr *)&their_addr, &sin_size)) == -1)
        {
            perror("accept");
            continue;
        }
        printf("server: got connection from %s\n", inet_ntoa(their_addr.sin_addr));
        if(!fork())
        { // to jest proces-dziecko
            close(sockfd); // dziecko nie potrzebuje gniazda nasłuchującego
            if(send(new_fd, "Hello, world!\n", 14, 0) == -1)
                 perror("send");

            int amount;
            char tab[MAX_LENGTH];
            while (1)
            {
              if((amount = recv(new_fd, tab, sizeof(tab) - 1, 0)) == -1)
              {
                  perror("recv");
                  exit(1);
              }
              if(amount == 0)
                break;
              tab[amount] = '\0';
              printf("%d: %s \n", amount, tab);

            }
            close(new_fd);
            exit(0);
        }
        close(new_fd); // rodzic nie potrzebuje tego
    }
    return 0;
}
