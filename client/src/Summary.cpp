#include "../include/Summary.h"
#include <string>
#include <iostream>
#include <map>
#include <vector>
#include <mutex>



Summary::Summary():mapUserToGameToEvents(),mapUserToGameToEvents_mutex(),mapUserToGameToGeneralEvents(),mapUserToGameToGeneralEvents_mutex(){

};


void Summary::insertMapSummary(std::string username, std::string channel, std::string eventByTime){
    std::lock_guard<std::mutex> lock((mapUserToGameToEvents_mutex));
    mapUserToGameToEvents[username][channel].push_back(eventByTime);
}

void Summary::setNewUserToChannel(std::string username, std::string channel){
     std::lock_guard<std::mutex> lock((mapUserToGameToEvents_mutex));
     mapUserToGameToEvents[username][channel] = std::vector<std::string>();
}

void Summary::deleteChannelFromUser(std::string username, std::string channel){
    std::lock_guard<std::mutex> lock(mapUserToGameToEvents_mutex);
    if(mapUserToGameToEvents.count(username) == 1){
        if(mapUserToGameToEvents[username].count(channel) == 1){
           mapUserToGameToEvents[username].erase(channel);
        }
        if(mapUserToGameToEvents[username].empty()){
            mapUserToGameToEvents.erase(username);
        }
    }
}

std::string Summary::printVectorGameFromUser(std::string username, std::string channel){
    std::lock_guard<std::mutex> lock(mapUserToGameToEvents_mutex);
    std::string allEvents;
    std::map<std::string, std::map<std::string, std::vector<std::string>>>::iterator itUser = mapUserToGameToEvents.find(username);
    if (itUser != mapUserToGameToEvents.end()) {
        std::map<std::string, std::vector<std::string>>::iterator itGame = itUser->second.find(channel);
        if (itGame != itUser->second.end()) {
            for (auto itEvent = itGame->second.begin(); itEvent != itGame->second.end(); ++itEvent) {
                allEvents += *itEvent + "\n";
            }
        }
    }
      return allEvents;          
}


std::map<std::string, std::map<std::string, std::vector<std::string>>>& Summary::getMapUserToEvents(){
    return this->mapUserToGameToEvents;
}

std::mutex& Summary::getMapUserToEvents_mutex(){
    return this->mapUserToGameToEvents_mutex;
}

std::map<std::string, std::map<std::string, std::string>>& Summary::getmapUserToGameToGeneralEvents(){
    return this->mapUserToGameToGeneralEvents;
}

std::mutex& Summary::getmapUserToGameToGeneralEvents_mutex(){
    return this->mapUserToGameToGeneralEvents_mutex;
}