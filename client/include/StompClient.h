#pragma once
#include "../include/Game.h"
#include "../include/Summary.h"
#include "../include/ConnectionHandler.h"
#include "../include/Summary.h"
#include "../include/event.h"
#include <iostream>
#include <thread>
#include <mutex>
#include <stdlib.h>
#include <sstream>
#include <string>
#include <atomic>
#include <vector>
class StompClient{
    private:
    
    // Game game;
    
    ConnectionHandler connectionHendler;
    std::string username = "meni";
    bool userIsConnected = false;
    Summary summary;

    public:
    StompClient(std::string host,short port); 
    
    Summary& getSummary();
    ConnectionHandler& getConnectionHandler();
    void setUserIsConnected(bool state);
    bool getUserIsConnected();
    void setUserName(std::string newUserName);
    std::string getUserName();

    std::map<int,std::vector<std::string>> mapReciept {};
    std::map<std::string,int> mapSubscription {};
    std::map<std::string,std::map<std::string,std::vector<Event>>> mapUserGameEvent {};
    
    std::map<std::string, std::map<std::string, std::vector<std::string>>> mapUserToGameToEvents {};

    std::map<std::string, std::map<std::string, std::vector<std::string>>> mapStatsFromMessage {};
};