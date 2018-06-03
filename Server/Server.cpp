//g++ -std=c++11 Server.cpp -lpthread ; echo 1 ; ./a.out
#include <stdio.h>
#include <unistd.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <sys/wait.h>
#include <deque>
#include <dirent.h>
#include <pthread.h>
#include <sys/stat.h>
#include "DataBase.cpp"
#include <netdb.h>

#define MYPORT 8080    // port, z którym będą się łączyli użytkownicy
#define MYPORT1 8090
#define BACKLOG 10     // jak dużo możę być oczekujących połączeń w kolejce
#define MAX_LENGTH 20

struct stat info;
deque<string> FileList;
unordered_map<int, int> ChildrenSockets;
pthread_t thread_id;
DATABASE dataBase;
deque<pair<string, string> > messageQueue;

struct arg_struct {
	int sock;
	int PORT_num;
};

void *connection_handler(void *);
void readDocumentNames(const string&, deque<string>&);
void sigchld_handler(int);
void *listening(void*);

int main(int c, char** v)
{
	if (c > 1)
	{
		test();
		return 0;
	}
	int root_socket; // nasłuchuj na root_socket, nowe połaczenia na sock
	struct sockaddr_in my_addr; // informacja o moim adresie
	struct sockaddr_in their_addr; // informacja o adresie osoby łączącej się
	unsigned int sin_size;
	struct sigaction sa;
	int yes = 1;

	if ((root_socket = socket(AF_INET, SOCK_STREAM, 0)) == -1)
	{
		perror("socket");
		exit(1);
	}

	if (setsockopt(root_socket, SOL_SOCKET, SO_REUSEADDR, &yes, sizeof(int)) == -1)
	{
		perror("setsockopt");
		exit(1);
	}

	my_addr.sin_family = AF_INET; // host byte order
	my_addr.sin_port = htons(MYPORT); // short, network byte order
	my_addr.sin_addr.s_addr = INADDR_ANY; // uzupełnij moim adresem IP
	memset(&(my_addr.sin_zero), '\0', 8); // wyzeruj resztę struktury

	if (bind(root_socket, (struct sockaddr *)&my_addr, sizeof(struct sockaddr)) == -1)
	{
		perror("bind");
		exit(1);
	}

	if (listen(root_socket, BACKLOG) == -1)
	{
		perror("listen");
		exit(1);
	}

	sa.sa_handler = sigchld_handler; // zbierz martwe procesy
	sigemptyset(&sa.sa_mask);
	sa.sa_flags = SA_RESTART;
	if (sigaction(SIGCHLD, &sa, NULL) == -1)
	{
		perror("sigaction");
		exit(1);
	}

	pthread_create(&thread_id, NULL, listening, 0);
	readDocumentNames(DIR_PATH, FileList);
	int port_generator = 9000;
	while (1)
	{ // głowna pętla accept()
		sin_size = sizeof(struct sockaddr_in);
		struct arg_struct args;
		if ((args.sock = accept(root_socket, (struct sockaddr *)&their_addr, &sin_size)) == -1)
		{
			perror("accept");
			continue;
		}
		args.PORT_num = port_generator++;
		printf("server: got connection from %s\n", inet_ntoa(their_addr.sin_addr));
		pthread_create(&thread_id, NULL, connection_handler, &args);
		pthread_detach(thread_id);

		int mother_socket; // nasłuchuj na sock_fd, nowe połaczenia na sock

		int yes = 1;

		if ((mother_socket = socket(AF_INET, SOCK_STREAM, 0)) == -1)
		{
			perror("socket");
			continue;
		}

		if (setsockopt(mother_socket, SOL_SOCKET, SO_REUSEADDR, &yes, sizeof(int)) == -1)
		{
			perror("setsockopt");
			continue;
		}

		struct sockaddr_in addrRemote; // informacja o adresie osoby łączącej się
		memset(&addrRemote, 0, sizeof(struct sockaddr_in));
		addrRemote.sin_port = htons((port_generator - 1));
		addrRemote.sin_family = AF_INET;
		addrRemote.sin_addr.s_addr = htonl(INADDR_ANY);

		while (connect(mother_socket, (sockaddr*)&addrRemote, sizeof(addrRemote)) == -1)
		{
			sleep(1);
		}
		ChildrenSockets.emplace(args.sock, mother_socket);
	}
	return 0;
}

void readDocumentNames(const string& dirName, deque<string>& list)
{
	DIR* dirp = opendir(dirName.c_str());
	struct dirent *dp;
	while ((dp = readdir(dirp)) != NULL)
	{
		if (*(dp->d_name) != '.' && *(dp->d_name) != '.' + '.')
			list.push_back(dp->d_name);
	}
	closedir(dirp);
}

void *listening(void*)
{
	pair<string, string> message;

	while (1)
	{
		if (!messageQueue.empty())
		{
			message = messageQueue.front();
			messageQueue.pop_front();
			auto &listeners = dataBase[message.second].second;
			string info = message.first;
			cout << "Matula:  " << info << "###";
			for (auto x : listeners)
			{
				cout << x << " ";
				send(ChildrenSockets[x], info.c_str(), strlen(info.c_str()), 0);
				cout<<"tato"<<endl;
			}
			add(dataBase, message.second, info);
			cout << endl;
			cout<<"Alutam"<<endl;
		}

	}

}

void sigchld_handler(int s)
{
	while (wait(NULL) > 0);
}

void *connection_handler(void* socket_desc)
{
	// to jest proces-dziecko
	struct arg_struct arg = *(struct arg_struct*)socket_desc;
	int sock = arg.sock;
	string fileOpen = "";
	sleep(1);
	if (send(sock, &arg.PORT_num, sizeof(int), 0) == -1)
		perror("send");
	int amount;
	char instr[MAX_LENGTH];

	if (stat(DIR_PATH, &info) != 0)
		system((string("mkdir -p ") + DIR_PATH).c_str());
	else if (info.st_mode & S_IFDIR) {}
	else
		perror((string("") + DIR_PATH + " is no directory\n").c_str());

	while (1)
	{
		perror("czekam\n");
		if ((amount = recv(sock, instr, sizeof(instr) - 1, 0)) == -1)
			perror("recv");

		if (amount == 0)
			break;

		instr[amount] = '\0';

		printf("%d: %s  \n", amount, instr);
		if (strlen(instr) < 2)
		{
			cout << "zbyt krotka wiadomosc" << endl;
			continue;
		}

		if (instr[0] == 'N')
		{
			int fWrongName = 0;
			for (int i = 0; i < MAX_LENGTH - 1; i++)
				instr[i] = instr[i + 1];
			for (int j = 0; j < FileList.size(); j++)
			{
				if (FileList[j] == ((string)instr).append(".txt"))
				{
					fWrongName = 1;
				}

			}
			if (!fWrongName)
			{
				FileList.push_back(((string)instr).append(".txt"));
				fstream fileStream;
				fileStream.open(DIR_PATH + FileList[FileList.size() - 1], ios::out);
				if (fileStream.good())
				{
					perror("Creation went OK \n");
					fileStream << "\0";
					fileStream.flush();			/*Forcing buffer to go to harddisc's memory*/
					fileStream.close(); 			/*Close the file when finished*/
				}
				else
					perror("Error in file creation\n");
			}
			else
			{
				perror("File with such a name already exists\n");
				fWrongName = 0;
			}

		}
		if (!strcmp(instr, "UP"))
		{
			if (FileList.size() == 0)
				send(sock, "\n", strlen("\n"), 0);
			string temp="";
			for (int j = 0; j < FileList.size(); j++)
			{
				temp=temp+FileList[j]+"\n";
			}
			send(sock, temp.c_str(), strlen(temp.c_str()), 0);
		}
		if (instr[0] == 'G')
		{
			if(fileOpen.size()>0)
				perror("GG");
			string info(instr + 1, strlen(instr) - 1);
			fileOpen = info;
			if (dataBase.count(fileOpen) > 0)
				dataBase[fileOpen].second.insert(sock);
			else
			{
				LISTENERS lis({ sock });
				SHEET sheet = bufferFile(fileOpen);
				DOCK dock = make_pair(sheet, lis);
				dataBase.emplace(fileOpen, dock);
			}
				string sheet;
				for (auto i = dataBase[info].first.begin(); i != dataBase[info].first.end(); i++)
				{
					for (auto j = (*i).begin(); j != (*i).end(); j++)
					{
						sheet=sheet+*j;
					}
					cout<<sheet<<endl;
					send(sock, (sheet+"\n").c_str(), strlen((sheet+"\n").c_str()), 0);
					sheet="";
				}

			send(sock, "\0", sizeof(char), 0);


		}
		if (!strcmp(instr, "UG")) //close file
		{
			dataBase[fileOpen].second.erase(sock);
			cout << dataBase[fileOpen].second.size() << endl;
			if (dataBase[fileOpen].second.size() == 0)
			{
				DOCK &dock = dataBase[fileOpen];
				SHEET &sheet = dock.first;
				updateFile(fileOpen, sheet);
				dataBase.erase(fileOpen);
			}
			fileOpen = "";
		}
		if (instr[0] == 'Z')
		{
			string info(instr + 1, strlen(instr) - 1);
			messageQueue.emplace_back(info, fileOpen);

		}

	}
	if(fileOpen.size()>0)
	{
		dataBase[fileOpen].second.erase(sock);
		if (dataBase[fileOpen].second.size() == 0)
		{
			DOCK &dock = dataBase[fileOpen];
			SHEET &sheet = dock.first;
			updateFile(fileOpen, sheet);
			dataBase.erase(fileOpen);

		}
	}
	ChildrenSockets.erase(sock);
	close(sock);
}
