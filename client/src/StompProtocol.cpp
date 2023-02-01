
#include "../include/StompProtocol.h"
#include "../include/event.h"
#include "../include/ConnectionHandler.h"
#include "../include/StompClient.h"
#include <map>
#include <mutex>
#include<string>
#include<vector>
#include <fstream>

StompProtocol::StompProtocol(StompClient* stompClient):stompClient(stompClient){};

std::string StompProtocol::connectMessage(std::string& host,std::string username,std::string password){
    std::string request = "CONNECT\naccept-version:1.2\nhost:"+host+"\n"+"login:"+ username + "\n" +"passcode:"+ password+"\n"+"\n"+'\0';
    return request;
}

std::string StompProtocol::subscribe(std::string channel,int receiptId,int id){
    std::string request = "SUBSCRIBE\ndestination:/" + channel + "\n"+"id:"+ std::to_string(id) + "\n"+"receipt:"+std::to_string(receiptId)+"\n"+"\n"+'\0';
    return request;
}

std::string StompProtocol::unsubscribe(int id,int receiptId){
    std::string request = "UNSUBSCRIBE\nid:"+ std::to_string(id) + "\n" + "receipt:"+std::to_string(receiptId)+"\n"+"\n"+'\0';
    return request;
}

std::string StompProtocol::disconnect(int receiptId){
    std::string request = "DISCONNECT\nreceipt:"+std::to_string(receiptId)+"\n"+"\n"+'\0';
    return request;
}

std::vector<std::string> StompProtocol::error(std::string receiptId,StompClient* stompClient){
    size_t colonPos = receiptId.find(':');
    std::string id = receiptId.substr(colonPos+1);
    int idReceipt = std::stoi(id);
    std::vector<std::string> commandVector = stompClient->mapReciept[idReceipt];
    return commandVector;
}

std::string StompProtocol::stringFromMap( std::map<std::string,std::string>& mapA){
    std::string result;
    for (std::map<std::string,std::string>::iterator iter = mapA.begin(); iter!= mapA.end();iter++) {
        std::string k = iter->first;
        result += "\n" + k + ":" +mapA[k];
    }
    

    return result;
}

void StompProtocol::updateStatsEvent(std::vector<Event> events,Event& statsEvent){
    for(std::vector<Event>::size_type i = 0;i < events.size();i++){ // weird for loop
        std::map<std::string,std::string> mapGeneralGameUpdates = events[i].get_game_updates();
        std::map<std::string,std::string> mapTeamA = events[i].get_team_a_updates();
        std::map<std::string,std::string> mapTeamB = events[i].get_team_b_updates();
        for (std::map<std::string,std::string>::iterator iter = mapGeneralGameUpdates.begin(); iter!= mapGeneralGameUpdates.end();iter++) {
        std::string k = iter->first;
        statsEvent.setGameupdae(k,mapGeneralGameUpdates[k]); 
        }    
    

     for (std::map<std::string,std::string>::iterator iter = mapTeamA.begin(); iter!= mapTeamA.end();iter++) {
        std::string k = iter->first;
        statsEvent.setGameupdaeA(k,mapTeamA[k]); 
        }    
    

     for (std::map<std::string,std::string>::iterator iter = mapTeamB.begin(); iter!= mapTeamB.end();iter++) {
        std::string k = iter->first;
        statsEvent.setGameupdaeB(k,mapTeamB[k]); 
        }    
    }
} 




void StompProtocol::send(struct::names_and_events fileEvent,int i,StompClient* stompClient){
    std::map<std::string,std::string> mapGeneralGameUpdates = fileEvent.events[i].get_game_updates();
    std::map<std::string,std::string> mapTeamA = fileEvent.events[i].get_team_a_updates();
    std::map<std::string,std::string> mapTeamB = fileEvent.events[i].get_team_b_updates();
    std::string gameUpdate = stringFromMap(mapGeneralGameUpdates);
    std::string teamAupdate = stringFromMap(mapTeamA);
    std::string teamBupdate = stringFromMap(mapTeamB);

    // insert game report to the map
    std::string channel = fileEvent.team_a_name + "_" + fileEvent.team_b_name;
    std::string event = std::to_string(fileEvent.events[i].get_time()) +" - "+ fileEvent.events[i].get_name() + "\n" + fileEvent.events[i].get_discription() + "\n";
    Summary& summary = stompClient->getSummary();
    summary.insertMapSummary(stompClient->getUserName(),channel,event);

    //insert fileEvent to UserGameEvent for stats
    stompClient->mapUserGameEvent[stompClient->getUserName()][channel].push_back(fileEvent.events[i]);

    std::string request = "SEND\ndestination:/" +fileEvent.team_a_name+"_"+fileEvent.team_b_name+"\n"+"\n"+"user: "+stompClient->getUserName()+"\n"+"team a: "+fileEvent.team_a_name+"\n"+"team b: "+ fileEvent.team_b_name+"\n"+"event name: "+fileEvent.events[i].get_name()+"\n"+"time: "+std::to_string(fileEvent.events[i].get_time())+"\n"+ "general game updates:\n"+gameUpdate+ "\n"+ "team a updates: " + teamAupdate +"\n"+"team b updates: "+ teamBupdate +"\n"+ "description:\n"+ fileEvent.events[i].get_discription()+ "\n" +"\0" +'\n' + '\0';
    
    stompClient->getConnectionHandler().sendLine(request);
}

std::string StompProtocol::extractWord(std::string text, std::string substring) {
    std::size_t found = text.find(substring);
    if (found != std::string::npos) {
        std::size_t start = found + substring.length();
        std::size_t end = text.find_first_of("\n", start);
        return text.substr(start, end - start);
    }
    return std::string();
}

std::atomic<int> receiptId(1);
std::atomic<int> id(1);

void StompProtocol::workForServer(StompClient* stompClient){ 
    while(1){
        std::string answer;
        if(!(*stompClient).getConnectionHandler().getFrameAscii(answer,'\0')){
            std::cout << "string not right from the server \n" << std::endl;
            
        }
        
        std::istringstream stream(answer);
        std::string line;
        std::vector<std::string> frameVector;
        frameVector.clear();
        while(std::getline(stream,line)){
            if(line != "\x00" && !line.empty()){
                frameVector.push_back(line);
            
            }
        }

        if (!frameVector.empty()){
            std::string frameType = frameVector[0];
    
        if(frameType == "CONNECTED"){
            std::cout << "Login successful" << std::endl;
            stompClient->setUserIsConnected(true);
            
        }else if(frameType == "MESSAGE"){
            
            std::string username = extractWord(answer,"user: "); // added space
            
            std::string destination = extractWord(answer,"destination:/");
            
            std::string time = extractWord(answer,"time:"); 
            
            std::string eventName = extractWord(answer,"event name:"); 
            
            std::string description = extractWord(answer,"description:\n");
            
            std::string event = time + " -" + eventName + "\n" + description;
            
            stompClient->mapUserToGameToEvents[username][destination].push_back(event);
            

            std::string teamA = extractWord(answer,"team a: ");
            std::string teamB = extractWord(answer,"team b: ");
            std::string geeneralGameUpdates = extractWord(answer,"general game updates: \n");
            std::string teamAUpdates = extractWord(answer,"team a updates: \n");
            std::string teamBUpdates = extractWord(answer,"team b updates: \n");
            
            
            std::string statsFromMessage = teamA + " VS " + teamB + "\n" + "Game stats: " + "\n" + "General stats:" + "\n" + geeneralGameUpdates + "\n" + teamA + " stats: " + "\n" + teamAUpdates + "\n" + teamB + " stats: " + "\n" + teamBUpdates + "\n" + "Game event reports:" + "\n";
            stompClient->mapStatsFromMessage[username][destination].push_back(statsFromMessage);


            

            std::string allEvents;
            for (std::vector<std::string>::size_type i =0; i<stompClient->mapUserToGameToEvents[username][destination].size();i++){
                    allEvents += stompClient->mapUserToGameToEvents[username][destination][i]+"\n";
            }

               

        }else if(frameType == "RECEIPT"){
            std::vector<std::string> commandVector;
            if(!frameVector[1].empty()){
            std::string receiptIdFromServer = frameVector[1];
            std::vector<std::string> commandVector = error(receiptIdFromServer,stompClient);
            
                        
            std::string command = commandVector[0];
            if(command == "logout"){
                
                stompClient->getConnectionHandler().close();
                break;  
            }else{

            std::string channel = "no channel";
            if(!commandVector[1].empty()){channel = commandVector[1];}
        
            if(command == "join"){
                std::cout << "Joined channel " + channel << std::endl; 
            }else if(command == "exit"){
                std::cout << "Exited channel " + channel << std::endl;
                stompClient->mapSubscription.erase(channel);
            }
            }
            }
        }else if(frameType == "ERROR"){
            std::string MessageError = frameVector[2];
            size_t colonPos = MessageError.find(':');
            std::string error = MessageError.substr(colonPos+1);
            std::cout << error << std::endl;   
        }
        
        }
        
    }
};


void StompProtocol::workForThreadKeyboard(StompClient* stompClient){
    while(1){ 
       const short bufsize = 1024; 
       char buf[bufsize];  
       std::cin.getline(buf, bufsize); 
       std::string line(buf);          
       std::vector<std::string> commandlineVector;
       std::istringstream iss(line);
       std::string word;
       while(iss >> word){
            commandlineVector.push_back(word);
       }
       if (!commandlineVector.empty()){
       std::string command = commandlineVector[0];
    
       if(command == "login" ){
            if(stompClient->getUserIsConnected()){
                std::cout << "The client is already logged in, log out before trying again" << std::endl;
                
            }else{
                
                std::string hostPort = commandlineVector[1];
                size_t colonPos = hostPort.find(':');
                std::string host = hostPort.substr(0,colonPos);
                stompClient->setUserName(commandlineVector[2]);
                std::string password = commandlineVector[3];
                std::string request = connectMessage(host,stompClient->getUserName(),password);
               
                stompClient->getConnectionHandler().sendLine(request);
                
                
            }
           
        }else if(command == "join" && stompClient->getUserIsConnected()){
            std::string channel = commandlineVector[1];
            // open new channel
            Summary& summary = stompClient->getSummary();
            summary.setNewUserToChannel(stompClient->getUserName(),channel);
            // send a subscribe message to server
            std::string request = subscribe(channel,receiptId,id);
            stompClient->mapReciept.insert(std::make_pair(receiptId++,commandlineVector)); 
            stompClient->mapSubscription.insert(std::make_pair(channel,id++));
           
           stompClient->getConnectionHandler().sendLine(request);
           
            
        }else if(command == "exit" && stompClient->getUserIsConnected()){
            std::string channel = commandlineVector[1]; 
            //delete the vector from the channel from the userame
            Summary& summary = stompClient->getSummary();
            summary.deleteChannelFromUser(stompClient->getUserName(),channel);

            std::string request = unsubscribe(stompClient->mapSubscription[channel],receiptId);//receiptId
            stompClient->mapReciept.insert(std::make_pair(receiptId++,commandlineVector));
            stompClient->getConnectionHandler().sendLine(request);
            
        }else if(command == "report" && stompClient->getUserIsConnected()){
            std::string file = commandlineVector[1];
            names_and_events fileEvent = parseEventsFile(file);
            
           for (std::size_t i = 0; i < fileEvent.events.size();i++){
               send(fileEvent,i,stompClient); 
            }
            
            
        }else if(command == "summary" && stompClient->getUserIsConnected()){
            std::string channel = commandlineVector[1];
            std::string user = commandlineVector[2];
            std::string filename = commandlineVector[3];

            if(stompClient->getUserName() == user){
            Summary& summary = stompClient->getSummary();
            std::string allEvents = summary.printVectorGameFromUser(user,channel);
            
           
             // user make summary for himself.
            Event statsEvent = stompClient->mapUserGameEvent[user][channel][0];
            std::vector<Event> events = stompClient->mapUserGameEvent[user][channel];
            
            updateStatsEvent(events,statsEvent); // make user th data save in statsEvent

            std::map<std::string,std::string> mapGeneralGameUpdates = statsEvent.get_game_updates();
            std::map<std::string,std::string> mapTeamA = statsEvent.get_team_a_updates();
            std::map<std::string,std::string> mapTeamB = statsEvent.get_team_b_updates();
            std::string gameUpdate = stringFromMap(mapGeneralGameUpdates);
            std::string teamAupdate = stringFromMap(mapTeamA);
            std::string teamBupdate = stringFromMap(mapTeamB);

            std::string allStats = statsEvent.get_team_a_name() + " VS " + statsEvent.get_team_b_name() + "\n" + "Game stats: " + "\n" + "General stats:" + "\n" + gameUpdate + "\n" + statsEvent.get_team_a_name() + " stats: " + "\n" + teamAupdate + "\n" + statsEvent.get_team_b_name() + " stats: " + "\n" + teamBupdate + "\n" + "Game event reports:" + "\n";
            
            std::ofstream file(filename);
            std::string finalSummary = allStats + "\n" + allEvents;
            file << finalSummary;
            file.close();
            
            }else{

                std::vector<std::string> vectorAllEvents = stompClient->mapUserToGameToEvents[user][channel];
                if(stompClient->mapUserToGameToEvents[user][channel].size() > 0){
                
                std::string allEvents;
                for (std::vector<std::string>::size_type i =0; i<vectorAllEvents.size();i++){
                    allEvents += vectorAllEvents[i]+"\n";
                }

                std::vector<std::string> statsGameVector = stompClient->mapStatsFromMessage[user][channel];
                std::string statsGame = statsGameVector[statsGameVector.size()-1];

                std::ofstream file(filename);
                std::string finalSummary =statsGame+ "\n"+ allEvents;
                file << finalSummary;
                file.close();

            }
            
        }
        }else if(command == "logout" && stompClient->getUserIsConnected()){
            stompClient->setUserIsConnected(false);
            std::string request = disconnect(receiptId);
            stompClient->mapReciept.insert(std::make_pair(receiptId++,commandlineVector));
            stompClient->getConnectionHandler().sendLine(request);
            
        } 
       }
   }
}


