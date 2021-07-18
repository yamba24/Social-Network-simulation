//
// Created by roizu@wincs.cs.bgu.ac.il on 12/31/18.
//
#include <thread>
#include "../include/connectionHandler.h"
#include "../include/EncoderDecoder.h"
#include <string>
#include <vector>

EncoderDecoder::EncoderDecoder(std::string host, short port) : host(host), port(port){}
bool loop = true;
void EncoderDecoder::run(ConnectionHandler *connectionHandler){
    std::thread keyboardReader(EncoderDecoder::Encode,std::ref(connectionHandler));
    std::thread keyboardSender(EncoderDecoder::Decode,std::ref(connectionHandler));

    keyboardReader.join();
    keyboardSender.join();

}
// encoding 1-8

void EncoderDecoder::Encode(ConnectionHandler *connectionHandler){

    while(loop) {
        const short bufsize = 1024; // the buffer size
        char bytes[bufsize]; // create new buffer char array
        std::cin.getline(bytes, bufsize); // the user puts an input and puts it inside the buffer
        std::string line(bytes); // make the command to a string
        std::vector<std::string> messageCommand;
        messageCommand=connectionHandler->split(line, " ");

        if (!messageCommand.empty()) {
            char opcode[2]; // array of char for the opcode
            if (messageCommand[0] == "REGISTER") {
                shortToBytes(1, opcode);
                connectionHandler->sendBytes(opcode, 2);
                connectionHandler->sendFrameAscii(messageCommand[1], '\0');
                connectionHandler->sendFrameAscii(messageCommand[2], '\0');
            }
            else if (messageCommand[0] == "LOGIN") {
                shortToBytes(2, opcode);
                connectionHandler->sendBytes(opcode, 2);
                connectionHandler->sendFrameAscii(messageCommand[1], '\0');
                connectionHandler->sendFrameAscii(messageCommand[2], '\0');
            }
            else if (messageCommand[0] == "LOGOUT") {
                shortToBytes(3, opcode);
                connectionHandler->sendBytes(opcode, 2);
                std::this_thread::sleep_for(std::chrono::milliseconds(10));
            }
            else if (messageCommand[0] == "FOLLOW") {
                shortToBytes(4, opcode);
                connectionHandler->sendBytes(opcode, 2);
                char followUn[1];
                if(messageCommand[1]=="0") followUn[0]=0;
                else followUn[0]=1;
                connectionHandler->sendBytes(followUn, 1);
                char numOfUsers[2];
                shortToBytes((short) std::stoi(messageCommand[2]), numOfUsers);
                connectionHandler->sendBytes(numOfUsers, 2);
                for (unsigned i = 0; i < std::stoi(messageCommand[2]); i++) {
                    connectionHandler->sendFrameAscii(messageCommand[i + 3], '\0');
                }
            }
            else if (messageCommand[0] == "POST") {
                shortToBytes(5, opcode);
                connectionHandler->sendBytes(opcode, 2);
                std::string sub = line.substr(5);
                connectionHandler->sendFrameAscii(sub, '\0');
            }
            else if (messageCommand[0] == "PM") {
                shortToBytes(6, opcode);
                connectionHandler->sendBytes(opcode, 2);
                connectionHandler->sendFrameAscii(messageCommand[1], '\0');
                std::string sub = line.substr(4 + messageCommand[1].size());
                connectionHandler->sendFrameAscii(sub, '\0');
            }
            else if (messageCommand[0] == "USERLIST") {
                shortToBytes(7, opcode);
                connectionHandler->sendBytes(opcode, 2);
            }

            else if (messageCommand[0] == "STAT") {
                shortToBytes(8, opcode);
                connectionHandler->sendBytes(opcode, 2);
                connectionHandler->sendFrameAscii(messageCommand[1], '\0');
            }
        }
    }
}
// convert bytes to string
void EncoderDecoder::Decode(ConnectionHandler *connectionHandler) {
    while(loop){
        std::string result="";
        char opCodeA[2];
        connectionHandler->getBytes(opCodeA, 2);
        short opCode = bytesToShort(opCodeA);

        if(opCode == 9){
            result+="NOTIFICATION ";
            char PMorPostA[1];
            connectionHandler->getBytes(PMorPostA,1);

            if(PMorPostA[0] == '\0'){
                result+="PM ";
            }
            else if(PMorPostA[0] == '\1'){
                result+="Public ";
            }
            std::string postingUser;
            connectionHandler->getFrameAscii(postingUser,'\0');
            postingUser=postingUser.substr(0,postingUser.size()-1);
            result+=postingUser;
            std::string content;
            connectionHandler->getFrameAscii(content,'\0');
            content=content.substr(0,content.size()-1);
            result=result+" "+content;
        }

        else if(opCode == 10){
            result = result + "ACK ";
            char opcodemsg[2];
            connectionHandler->getBytes(opcodemsg, 2);
            short opcodemsg1 = bytesToShort(opcodemsg);
            std::string opMsg = std::to_string(opcodemsg1);
            result = result + opMsg;

            if(opcodemsg1 == 3){
                loop = false;

            }
            else if(opcodemsg1 == 4){ // follow message
                char numOfUsers[2];
                connectionHandler->getBytes(numOfUsers, 2);
                short numOf = bytesToShort(numOfUsers);
                std:: string ret = std::to_string(numOf);
                result = result +" "+ ret;
                for(unsigned i=0;i<numOf;i++) {
                    std::string usernamelist;
                    connectionHandler->getFrameAscii(usernamelist, '\0');
                    result = result + " " + usernamelist.substr(0, usernamelist.size() - 1);
                }
            }
            else if(opcodemsg1 == 7){ // userlist message
                char numofusers[2];
                connectionHandler->getBytes(numofusers, 2);
                short num = bytesToShort(numofusers);
                std:: string ret = std:: to_string(num);
                result = result +" "+ ret;
                for(unsigned i=0;i<num;i++){
                    std::string userlist;
                    connectionHandler->getFrameAscii(userlist, '\0');
                    result = result +" "+ userlist.substr(0,userlist.size()-1);
                }
            }
            else if(opcodemsg1 == 8){ // stat message
                // num of post
                char numofpost[2];
                connectionHandler->getBytes(numofpost, 2);
                short num = bytesToShort(numofpost);
                std:: string ret = std::to_string(num);
                result = result +" "+ ret;
                // num of followers
                char numoffollowers[2];
                connectionHandler->getBytes(numoffollowers, 2);
                short numfollowers = bytesToShort(numoffollowers);
                std:: string retnumfollowers = std::to_string(numfollowers);
                result = result +" "+ retnumfollowers;
                // num of following
                char numoffollowing[2];
                connectionHandler->getBytes(numoffollowing, 2);
                short numfollowing = bytesToShort(numoffollowing);
                std:: string retnumfollowing = std:: to_string(numfollowing);
                result = result +" "+ retnumfollowing;
            }
        }
        else if(opCode == 11){
            result = result + "ERROR ";
            char opcodeMSG[2];
            connectionHandler->getBytes(opcodeMSG, 2);
            short opMSG = bytesToShort(opcodeMSG);
            std:: string opMsg = std::to_string(opMSG); // convert the short to string
            result = result + opMsg;
        }
        std::cout<<result<<std::endl;
    }
}

void EncoderDecoder:: shortToBytes(short num, char* bytesArr) {
    bytesArr[0] = ((num >> 8) & 0xFF);
    bytesArr[1] = (num & 0xFF);
}

short EncoderDecoder::bytesToShort(char *bytesArr) {
    short result = (short)((bytesArr[0] & 0xff) << 8);
    result += (short)(bytesArr[1] & 0xff);
    return result;
}
