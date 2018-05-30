/*
    ** server.c -- serwer używający gniazd strumieniowych
    */
//g++ -std=c++11 Server.cpp ; echo 1 ; ./a.out
#include <stdio.h>
#include <unistd.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <sys/wait.h>
#include <deque>
#include <dirent.h>
#include <sys/stat.h>
#include "test.cpp"

#define MYPORT 8080    // port, z którym będą się łączyli użytkownicy
#define MYPORT1 8090
#define BACKLOG 10     // jak dużo możę być oczekujących połączeń w kolejce
#define MAX_LENGTH 20

struct stat info;
deque<string> FileList;
deque<int> ChildrenList;
fstream fileStream;
pthread_t t;

int fWrongName = 0;

void readDocumentNames(const string& dirName, deque<string>& list)
{
	DIR* dirp= opendir(dirName.c_str());
	struct dirent *dp;
	while((dp = readdir(dirp)) != NULL)
	{
		if(*(dp->d_name)!='.' && *(dp->d_name)!='.'+'.')
		list.push_back(dp->d_name);
	}
	closedir(dirp);
}


void sigchld_handler(int s)
{
    while(wait(NULL) > 0);
}


int main(int c, char** v)
{
		if(c > 1)
		{
			test();
			return 0;
		}
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
    string file = "123.txt";

    DATABASE dataBase;

    LISTENERS lis{1,2};
    SHEET sheet = bufferFile(file);
    DOCK dock = make_pair(sheet, lis);
    dataBase.emplace(file, dock);

    while(1)
    { // głowna pętla accept()
        sin_size = sizeof(struct sockaddr_in);
        if((new_fd = accept(sockfd,(struct sockaddr *)&their_addr, &sin_size)) == -1)
        {
            perror("accept");
            continue;
        }
				ChildrenList.push_back(new_fd);
        printf("server: got connection from %s\n", inet_ntoa(their_addr.sin_addr));
        if(!fork())
        { // to jest proces-dziecko
            close(sockfd); // dziecko nie potrzebuje gniazda nasłuchującego
            if(send(new_fd, "Hello, world!\n", 14, 0) == -1)
                 perror("send");
						printf("wyslane\n");
						int amount;
						char instr[MAX_LENGTH];
            //char tab[MAX_LENGTH];

						if( stat( DIR_PATH, &info ) != 0 )
						{
										system((string("mkdir -p ") + DIR_PATH).c_str());
						}
						else if( info.st_mode & S_IFDIR )  // S_ISDIR() doesn't exist on my windows
						    {}
						else
						    perror( (string("") + DIR_PATH + " is no directory\n").c_str());

						readDocumentNames(DIR_PATH,FileList);		//sczytanie listy plikow z folderu do filelist
            while (1)
            {
							printf("czekam\n");
              if((amount = recv(new_fd, instr, sizeof(instr) - 1, 0)) == -1)
              {
                  perror("recv");
                  exit(1);
              }

              if(amount == 0)
                break;

              instr[amount] = '\0';

              printf("%d: %s  \n", amount, instr);
              if (strlen(instr) < 2)
              {
                cout<<"zbyt krotka wiadomosc"<<endl;
                continue;
              }




              if(instr[0]=='N')
              {
                for(int i=0;i<MAX_LENGTH-1;i++)
									instr[i]=instr[i+1];
									for(int j=0; j<FileList.size();j++)
									{
										if(FileList[j]==((string)instr).append(".txt"))
										{
											fWrongName = 1;
										}

									}
									if(!fWrongName)
									{
					          FileList.push_back(((string)instr).append(".txt"));
										fileStream.open(DIR_PATH+FileList[FileList.size()-1],ios::out);
										if(fileStream.good())
										{
											printf("Creation went OK \n");
											//send(new_fd, "Creation went OK\n", 17, 0);
											fileStream << "\0";
											fileStream.flush();			/*Forcing buffer to go to harddisc's memory*/
											fileStream.close(); 			/*Close the file when finished*/
										}
										else
										{
											printf("Error in file creation\n");
											send(new_fd, "Error in file creation\n", 23, 0);
										}
									}
									else
									{
										printf("File with such a name already exists\n");
										send(new_fd, "File with such a name already exists\n", 37, 0);

										fWrongName = 0;

									}

					    }
							if(!strcmp(instr,"UP"))
							{
								if(FileList.size()==0)
									send(new_fd, "\n", strlen("\n"), 0);
								for(int j=0; j<FileList.size();j++)
								{
								send(new_fd, (FileList[j]+"\n").c_str(), strlen((FileList[j]+"\n").c_str()), 0);
								cout<<FileList[j]+"\n";
								}
							}
              if(instr[0]=='G')
              {
                string info(instr+1, strlen(instr)-1);
                ifstream file;
                string line;
                file.open ( (string)DIR_PATH + info );
                if (file.is_open())
                {
									printf("plik otwarty\n");
                  while ( getline (file,line) )
                  {
                    send(new_fd, (line+"\n").c_str(), strlen((line+"\n").c_str()), 0);
                  }
                  send(new_fd, "\0", sizeof(char), 0);
                  file.close();
                }
              }
							if(instr[0]=='Z')
              {
								string info(instr+1, strlen(instr)-1);
								cout<<info<<endl;
                add(dataBase, file, info);
                printSheet(dataBase[file].first);
								/*int i=0;
                auto it = ChildrenList.begin();
								while(it!=ChildrenList.end()){
									send(*it++, (buffer+"\n").c_str(),strlen((buffer+"\n").c_str()), 0);
								}*/

							}

            }
            close(new_fd);
            exit(0);
        }
        close(new_fd); // rodzic nie potrzebuje tego
    }
    return 0;
}
