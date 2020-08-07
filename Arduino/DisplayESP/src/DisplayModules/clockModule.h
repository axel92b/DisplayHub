//
// Created by vov4ik on 6/2/20.
//

#ifndef UNTITLED2_SRC_DISPLAYMODULES_CLOCKMODULE_H_
#define UNTITLED2_SRC_DISPLAYMODULES_CLOCKMODULE_H_

#include <Arduino.h>
#include "AbstractModule.h"

// {"display_format":"HH:mm","format":"24H","name":"Clock","index":2,"region":"Israel"}

class ClockModule : public AbstractModule {
 private:
  String _display_format;
  String _format; //12HR/24HR
  String _year;
  String _month;
  String _day;
  String _hour;
 public:
  String GetModuleData() override;
 private:
  String _minute;
  String _region; // Won't be used
  //TODO: Add date somehow(there is in the API of CloudFunction)
  ~ClockModule() override = default;
 public:
  static ClockModule* Instance(String const& string);
  String getDisplayDataString() override;
};

#endif //UNTITLED2_SRC_DISPLAYMODULES_CLOCKMODULE_H_
