#pragma once
#include <map>
#include <mutex>
#include<string>
#include<vector>
#include <fstream>
#include <string>

class Summary
{
private:
    std::map<std::string, std::map<std::string, std::vector<std::string>>> mapUserToGameToEvents;
    std::mutex mapUserToGameToEvents_mutex;
    
    std::map<std::string, std::map<std::string, std::string>> mapUserToGameToGeneralEvents;
    std::mutex mapUserToGameToGeneralEvents_mutex;
public:
     
    //std::mutex getMutex();
    Summary();
    void insertMapSummary(std::string username, std::string channel, std::string eventByTime);
    void setNewUserToChannel(std::string username, std::string channel);    
    void deleteChannelFromUser(std::string username, std::string channel);
    std::string printVectorGameFromUser(std::string username, std::string channel);

    std::map<std::string, std::map<std::string, std::vector<std::string>>>& getMapUserToEvents();
    std::mutex& getMapUserToEvents_mutex();

    std::map<std::string, std::map<std::string, std::string>>& getmapUserToGameToGeneralEvents();
    std::mutex& getmapUserToGameToGeneralEvents_mutex();

    
};


