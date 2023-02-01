#include <iostream>
// #include <thread>
#include <mutex>
#include <stdlib.h>
#include "../include/ConnectionHandler.h"
#include "../include/Game.h"
#include "../include/StompClient.h"
#include "../include/StompProtocol.h"
#include "../include/event.h"
#include <sstream>
#include <functional>
#include <string>



StompClient::StompClient(std::string host,short port):connectionHendler(host,port),summary(){
    
};

Summary& StompClient::getSummary(){
    return this->summary;
};

ConnectionHandler& StompClient::getConnectionHandler() {
        return this->connectionHendler;
    };

void StompClient::setUserIsConnected(bool state){
    userIsConnected = state;
}

bool StompClient::getUserIsConnected(){
    return userIsConnected;
}

void StompClient::setUserName(std::string newUserName){
    username = newUserName;
}
std::string StompClient::getUserName(){
    return username;
}


StompClient client("127.0.0.1",7777);
int main(int argc, char *argv[]) {
    // TODO: implement the STOMP client
    if (argc < 3) {
        std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
        return -1;
    }
    std::string host = argv[1];
    short port = atoi(argv[2]);
    StompClient client(host,port);
    if(client.getConnectionHandler().connect()){
    std::thread t1(&StompProtocol::workForThreadKeyboard,&client);
    std::thread t2(&StompProtocol::workForServer,&client);
    t1.join();
    t2.join();
    }
}

