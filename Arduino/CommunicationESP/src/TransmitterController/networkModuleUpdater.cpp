//
// Created by vov4ik on 6/13/20.
//
#include <WiFi.h>
#include <HTTPClient.h>
#include <Extensions/Utilities.h>
#include "networkModuleUpdater.h"

using namespace std;
bool NetworkModuleUpdater::UpdateModule(AbstractModule& module, String module_data) {
    if(WiFiClass::status() != WL_CONNECTED){
        debugPrint("Wifi isn't connected can't update " + module.GetModuleName());
        return false;
    }
    HTTPClient http;

    if(module.GetModuleName() == "News Feed"){
        // ?request=ynet
        String news_function_url = "https://us-central1-displayhubcompanion.cloudfunctions.net/getNewsUpdates";

    }else if (module.GetModuleName() == "Clock"){
        //TODO: Hardcoded to Jerusalem
        String time_function_url = "https://us-central1-displayhubcompanion.cloudfunctions.net/getTime";

    }else if (module.GetModuleName() == "Stocks"){
        // getStocks?request=ibm_intc
        String stocks_function_url = "https://us-central1-displayhubcompanion.cloudfunctions.net/getStocks";

    }else if (module.GetModuleName() == "Weather"){
        // ?country=Israel&city=Haifa usage example to test
        String weather_function_url = "https://us-central1-displayhubcompanion.cloudfunctions.net/getWeather";

    }
}
