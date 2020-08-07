//
// Created by vov4ik on 6/2/20.
//

#include "clockModule.h"
#include "Extensions/Utilities.h"
#include <ArduinoJson.h>


//Module args {"display_format":"HH:mm","name":"Clock","format":"24H","index":0,"time":"18:24","region":"Israel"}
ClockModule* ClockModule::Instance(String const& string) {
    static ClockModule* sm = nullptr;
    if(!sm){
        StaticJsonDocument<500> doc;
        deserializeJson(doc, string);
        sm = new ClockModule();
        sm->SetName("Clock");
        sm->_row = doc["index"].as<uint8_t>();
        sm->SetName(doc["name"].as<String>());
        sm->_format = doc["format"].as<String>();
        sm->_display_format = doc["display_format"].as<String>();
        sm->_region = doc["region"].as<String>();
    }
    return sm;
}
String ClockModule::getDisplayDataString() {
    if(_format.equals("12H")){
        auto hours = strtol(_hour.c_str(), nullptr, 10);
        auto am_pm = hours >= 12  ? String("pm") : String("am");

        if(_display_format.equals("HH:mm")){
            if (hours == 0){
                return "    " + String(12) + ":" +
                       (_minute.length() < 2 ? "0" + _minute : _minute) + am_pm;
            }else {
                return "    " + String(hours > 12 ? (hours % 13) + 1 : hours) + ":" +
                       (_minute.length() < 2 ? "0" + _minute : _minute) + am_pm;
            }
        }else if(_display_format.equals("M/D HH:MM")){
            return  _day + "." + (_month.length() < 2 ? "0" + _month : _month) + "  " + String(hours > 12 ? (hours % 13) + 1 :  hours)  + ":" + (_minute.length() < 2 ? "0" + _minute : _minute ) + am_pm;
        }
    }else{
        auto hours = strtol(_hour.c_str(), nullptr, 10);
        Serial.println(String(hours) + ":" +  String(_minute));
        if(_display_format.equals("HH:mm")){
            return  "    " + _hour + ":" + (_minute.length() < 2 ? "0" + _minute : _minute);
        }else if(_display_format.equals("M/D HH:MM")){
            return  _day + "." + (_month.length() < 2 ? "0" + _month : _month) + "  " + _hour + ":" + (_minute.length() < 2 ? "0" + _minute : _minute);
        }
    }
    return "";
}
String ClockModule::GetData() {
    return String();
}

// -> response
// >  {
// >    dayofweek: 1,
// >    dayofweekName: 'Monday',
// >    day: 8,
// >    month: 6,
// >    monthName: 'June',
// >    year: 2020,
// >    hours: 1,
// >    minutes: 19,
// >    seconds: 31,
// >    millis: 377,
// >    fulldate: 'Mon, 08 Jun 2020 01:19:31 +0300',
// >    timezone: 'Asia/Jerusalem',
// >    status: 'ok'
//     >  }
bool ClockModule::Update(bool forceUpdate) {
    static int timeout = 0; //30min update_interval
    if(forceUpdate){
        Serial.println(GetModuleName() + " force update!");
    }
    if(!timeout || forceUpdate) {
        timeout = 30;
        String server = "http://us-central1-displayhubcompanion.cloudfunctions.net/getTime";
        StaticJsonDocument<500> doc;
        auto data = httpGETRequest(server.c_str());
        if(data.equals("{}")){
            Serial.println("Get request failed!");
            timeout = 3;
            return false;
        }
        DeserializationError error = deserializeJson(doc, data);

        // Test if parsing succeeds.
        if (error) {
            Serial.print(F("deserializeJson() failed: "));
            Serial.println(error.c_str());
            timeout = 3;
            return false;
        }
        Serial.println(server);

        _year = doc["year"].as<String>();
        _month = doc["month"].as<String>();
        _day = doc["day"].as<String>();
        _minute = doc["minutes"].as<String>();
        _hour = doc["hours"].as<String>();

        return true;
    }else{
        Serial.println(timeout);
        timeout--;
    }
    return false;
}
