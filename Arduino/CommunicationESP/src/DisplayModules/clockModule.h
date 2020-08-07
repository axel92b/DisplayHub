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
  String _display_format; //HH:MM, DD:MM HH:MM
  String _format; //12HR/24HR
  String _year;
  String _month;
  String _day;
  String _minute;
  String _hour;
 public:
  String GetData() override;
 private:
  String _region; // Won't be used
public:
    bool Update(bool forceUpdate) override;

private:
  ~ClockModule() override = default;
 public:
  static ClockModule* Instance(String const& string);
  String getDisplayDataString() override;
};

#endif //UNTITLED2_SRC_DISPLAYMODULES_CLOCKMODULE_H_
