#pragma once
#include "../include/ConnectionHandler.h"
#include "../include/event.h"
#include "../include/Game.h"
#include "../include/Summary.h"
#include "../include/StompClient.h"
#include <mutex>
#include<string>
#include<vector>
#include <map>



class StompProtocol {
private:
  StompClient* stompClient;
  
public:
  StompProtocol(StompClient* stompClient);
  static void workForThreadKeyboard(StompClient* stompClient);
  static void workForServer(StompClient* stompClient);

  
  static std::string connectMessage(std::string& host,std::string username,std::string password);
  static std::string subscribe(std::string channel,int receiptId,int id);
  static std::string unsubscribe(int id,int receiptId);
  static std::string disconnect(int receiptId);
  static std::vector<std::string> error(std::string receiptId,StompClient* stompClient);
  static std::string stringFromMap( std::map<std::string,std::string>& map);
  static void send(struct::names_and_events fileEvent,int i,StompClient* stompClient);
  static std::string extractWord(std::string text, std::string substring);
  static void updateStatsEvent(std::vector<Event> events,Event& statsEvent);
 
};


