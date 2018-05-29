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
#include <string>
#include <iostream>
#include <fstream>
#include <dirent.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <vector>
#include <unordered_map>
#include <sstream>
#include <regex>
//#include <algorithm/string.hpp>

#define MYPORT 8080    // port, z którym będą się łączyli użytkownicy
#define BACKLOG 10     // jak dużo możę być oczekujących połączeń w kolejce
#define MAX_LENGTH 20
#define DIR_PATH "./Files/"

using namespace std;

typedef vector<char> LINE;
typedef vector<int> LISTENERS;
typedef vector<LINE> SHEET;
typedef pair<SHEET, LISTENERS> DOCK;
typedef unordered_map<string, DOCK> DATABASE;

void add (DATABASE &, string, string);

/*int main()
{
    string file = "kohan.txt";

    LINE line1{'a','b','4'}, line2{'2','q'};
    SHEET sheet{line1, line2};
    LISTENERS lis{1,2};

    DOCK dock = make_pair(sheet, lis);

    DATABASE dataBase;
    dataBase.emplace(file, dock);
    //int a[0];
    //przychodzi info od klienta
    string info = "1.2.1.3:t";
    // ab4
    // 2q

    // abtg
    // w4
    // 2q

    add(dataBase, file, info);


    return 0;
}*/

void add(DATABASE &dataBase, string file, string info)
{
    regex re1(":"), re2("[0-9]+"),re3("[a-z0-9]+");
    int startIndex[2], endIndex[2];
    string data,change;
    smatch tmp;

    //sregex_iterator next(subject.begin(), subject.end(), re);
    regex_search(info,tmp,re1);
    data = tmp.prefix().str();
    change = tmp.suffix().str();

    sregex_iterator next(data.begin(), data.end(), re2);
    startIndex[0] = atoi((*next).str().c_str());
    startIndex[1] = atoi((*(++next)).str().c_str());
    endIndex[0] = atoi((*(++next)).str().c_str());
    endIndex[1] = atoi((*(++next)).str().c_str());

    /*cout<<startIndex[0]<<" "<<startIndex[1]<<endl
      <<endIndex[0]<<" "<<endIndex[1]<< endl << change <<endl;*/


    DOCK &dock = dataBase[file];
    SHEET &sheet = dock.first;
    int linesAmount = endIndex[0] - startIndex[0];
    vector<LINE> newLines(linesAmount,LINE{});

    sheet.insert( sheet.begin() + startIndex[0], newLines.begin(), newLines.end() );

    sregex_iterator next2(change.begin(), change.end(), re3), end;
    int q = 0;
    int index = startIndex[1];
    for (sregex_iterator it = next2; it != end; it++ , q++)
    {
        if(q == 0 && linesAmount > 0){
          LINE &line = sheet[startIndex[0]-1];
          LINE &lineEnd = sheet[endIndex[0]-1];
          auto str = (*it).str();
          vector<char> vec(str.begin(), str.end());
          lineEnd.insert(lineEnd.begin(), line.begin()+startIndex[1], line.end());
          line.erase(line.begin()+startIndex[1], line.end());
          line.insert(line.begin()+index, vec.begin(), vec.end());

          index = 0;
          continue;
        }
        LINE &line = sheet[startIndex[0] - 1 + q];
        auto str = (*it).str();
        vector<char> vec(str.begin(), str.end());
        line.insert(line.begin()+index, vec.begin(), vec.end());
        index = 0;

    }
    for(auto x : sheet){
      for(auto y : x){
        cout<<y<<" ";
      }
      cout<<endl;
    }

    //regex re("\\d+");

    //start/end points of tokens in str
    //std::sregex_token_iterator begin(str.begin(), str.end(), re), end;

    //copy(begin, end, std::back_inserter(tokens));

    LISTENERS &lis = dock.second;


}
