//
// Created by vov4ik on 6/2/20.
//

#include "weatherModule.h"
#include "Extensions/Utilities.h"
#include <ArduinoJson.h>



WeatherModule *WeatherModule::Instance(String const &string) {
    WeatherModule* wm = new WeatherModule();
    StaticJsonDocument<500> doc;
//    const char* server = "http://us-central1-displayhubcompanion.cloudfunctions.net/getNewsUpdates?request=ynet";
    deserializeJson(doc, string);
    wm = new WeatherModule();
    wm->SetName("Weather");
    wm->_city = doc["city"].as<String>();
    wm->_country = doc["country"].as<String>();
    wm->_row = doc["index"].as<uint8_t>();
    wm->SetName(doc["name"].as<String>());
    return wm;
}

String WeatherModule::getDisplayDataString() {
    auto dot_index = _temperature.lastIndexOf('.');
    return "temp: " + _temperature.substring(0,dot_index) + "'";
}

String WeatherModule::GetData() {
    return String();
}

bool WeatherModule::Update(bool forceUpdate) {
    static int timeout = 0; //30min update_interval
    if(!timeout || forceUpdate) {
        timeout = 60*3;
        String server = "http://us-central1-displayhubcompanion.cloudfunctions.net/getWeather?country=" + _country + "&city=" + _city;
        StaticJsonDocument<500> doc;
        auto data = httpGETRequest(server.c_str());
        if(data.equals("{}")){
            Serial.println("Get request failed!");
            timeout = 5;
            return false;
        }
        DeserializationError error = deserializeJson(doc, data);

        // Test if parsing succeeds.
        if (error) {
            Serial.print(F("deserializeJson() failed: "));
            Serial.println(error.c_str());
            timeout = 5;
            return false;
        }


        // {"state":"Israel","city":"Haifa","temp":"23.33","description":"clear sky","humidity":75,"wind":4.01}
        _temperature = String(doc["temp"].as<double>());
        _humidity = String(doc["humidity"].as<int>());
        _wind = String(doc["wind"].as<double>());
        _description = String(doc["description"].as<String>());
        Serial.println("city: " + String(_city));
        Serial.println("description: " + String(_description));
        Serial.println("humidity: " + String(_humidity));
        Serial.println("temperature: " + String(_temperature));
        Serial.println("wind: " + String(_wind));
        return true;
    }else{
        Serial.println(timeout);
        timeout--;
    }
    return false;
}


