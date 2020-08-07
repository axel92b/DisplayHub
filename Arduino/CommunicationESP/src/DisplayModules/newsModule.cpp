//
// Created by vov4ik on 6/2/20.
//

#include "newsModule.h"
#include "Extensions/Utilities.h"
#include <ArduinoJson.h>

//Creating module News Feed
//Module args {"name":"News Feed","index":3}

NewsModule* NewsModule::Instance(String const& string) {
    static NewsModule* nm = nullptr;
    if(!nm){

        StaticJsonDocument<200> doc;
        deserializeJson(doc, string);
        nm = new NewsModule();
        nm->SetName("News Feed");
        nm->_source = doc["feed"].as<String>();
        nm->_source.toLowerCase();
        nm->_row = doc["index"].as<uint8_t>();
        nm->SetName(doc["name"].as<String>());
    }
    return nm;
}
String NewsModule::getDisplayDataString() {
    return _news;
}
String NewsModule::GetData() {
    return String();
}

bool NewsModule::Update(bool forceUpdate) {
    static int timeout = 0;
    if(!timeout || forceUpdate) {
        timeout = 60*6;
        String server = "http://us-central1-displayhubcompanion.cloudfunctions.net/getNewsUpdates?request=" + _source;
        auto data = httpGETRequest(server.c_str());
        if(data.equals("{}")){
            Serial.println("Get request failed!");
            timeout = 5;
            return false;
        }
        Serial.println(data);
        _news = data;
        return true;
    }else{
        Serial.println(timeout);
        timeout--;
    }
    return false;
}
