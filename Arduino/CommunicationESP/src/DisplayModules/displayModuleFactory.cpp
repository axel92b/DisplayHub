//
// Created by vov4ik on 6/3/20.
//

#include "displayModuleFactory.h"

AbstractModule *DisplayModuleFactory::createDisplayDataModule(const String &moduleName, const String &args) {
    if(moduleName == "News Feed"){
        return NewsModule::Instance(args);
    }else if (moduleName == "Clock"){
        return ClockModule::Instance(args);
    }else if (moduleName.startsWith("Stocks")){
        return StocksModule::Instance(args);
    }else if (moduleName == "Weather"){
        return WeatherModule::Instance(args);
    }
    return nullptr;
}
