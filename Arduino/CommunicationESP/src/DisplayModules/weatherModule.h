//
// Created by vov4ik on 6/2/20.
//

#ifndef UNTITLED2_SRC_DISPLAYMODULES_WEATHERMODULE_H_
#define UNTITLED2_SRC_DISPLAYMODULES_WEATHERMODULE_H_

#include <Arduino.h>
#include "AbstractModule.h"

// {"country":"Israel","city":"Haifa","name":"Weather","index":3}

class WeatherModule : public AbstractModule {
  String _state;
  String _country;
  String _city;
  String _description;
  String _temperature;
  String _humidity;
  String _wind;
 public:
  String GetData() override;
 private:
public:
    bool Update(bool forceUpdate) override;

private:
    ~WeatherModule() override = default;
 public:
  static WeatherModule* Instance(String const& string);
  String getDisplayDataString() override;


};

#endif //UNTITLED2_SRC_DISPLAYMODULES_WEATHERMODULE_H_
