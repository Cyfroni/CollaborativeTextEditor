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

using namespace std;

#define DIR_PATH "./Files/"

typedef vector<char> LINE;
typedef vector<int> LISTENERS;
typedef vector<LINE> SHEET;
typedef pair<SHEET, LISTENERS> DOCK;
typedef unordered_map<string, DOCK> DATABASE;

void add (DATABASE &, string, string);
void printSheet(SHEET);
SHEET bufferFile(string);

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
    string info = "1.2.1.4:qw";
    // ab4
    // 2q

    // abtg
    // w4
    // 2q

    printSheet(dataBase[file].first);
    add(dataBase, file, info);
    printSheet(dataBase[file].first);

    return 0;
}*/

void add(DATABASE &dataBase, string file, string info)
{
    regex re1(":"), re2("[0-9]+"),re3("[a-z0-9]+"), re4("\n");
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

    cout<<"#"<<change<<"#\n";
    //return;
    /*cout<<startIndex[0]<<" "<<startIndex[1]<<endl
      <<endIndex[0]<<" "<<endIndex[1]<< endl << change <<endl;*/
    if (change == "")
    {

      return;
    }

    regex_search ( change, tmp, re4 );

    DOCK &dock = dataBase[file];
    SHEET &sheet = dock.first;
    int linesAmount = tmp.size();


    vector<LINE> newLines(linesAmount,LINE{});

    sheet.insert( sheet.begin() + startIndex[0], newLines.begin(), newLines.end() );

    sregex_iterator next2(change.begin(), change.end(), re3), end;
    //cout<<(*next2).str()<<endl;
    int q = 0;
    int index = startIndex[1];
    for (sregex_iterator it = next2; it != end; it++ , q++)
    {
        //printSheet(sheet);
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
    // for(auto x : sheet){
    //   for(auto y : x){
    //     cout<<y<<" ";
    //   }
    //   cout<<endl;
    // }

    //regex re("\\d+");

    //start/end points of tokens in str
    //std::sregex_token_iterator begin(str.begin(), str.end(), re), end;

    //copy(begin, end, std::back_inserter(tokens));

    LISTENERS &lis = dock.second;


}

void printSheet(SHEET sheet)
{
  cout << "POCZATEK PLIKU" << endl;
  for(auto i = sheet.begin(); i!=sheet.end(); i++)
  {
      for(auto j = (*i).begin(); j!=(*i).end(); j++)
      {
        cout << *j;
      }
      cout << endl;
  }
  cout << "KONIEC PLIKU" << endl;

}

SHEET bufferFile(string fileName)
{
  SHEET sheet;
  ifstream file;
  string line;
  file.open ( (string)DIR_PATH + fileName );
  if (file.is_open())
  {
    printf("bufferFile\n");
    while ( getline (file,line) )
    {
      vector<char> vec(line.begin(), line.end());
      sheet.emplace_back(vec);
    }
    file.close();
  }
  printSheet(sheet);
  return sheet;
}
