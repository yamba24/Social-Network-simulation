//
// Created by roizu@wincs.cs.bgu.ac.il on 1/1/19.
//


#include <string>
#include "../include/connectionHandler.h"
#include <mutex>
#include "../include/EncoderDecoder.h"

int main (int argc, char *argv[]) {
    std:: string host= argv[1];// "132.72.44.31";
    short port = std::atoi(argv[2]);//7777;
    bool connected;

    ConnectionHandler *connectionHandler = new ConnectionHandler(host, port);
    if(!connectionHandler->connect()){
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }

    EncoderDecoder encoderDecoder(host,port);
    encoderDecoder.run(connectionHandler);
}