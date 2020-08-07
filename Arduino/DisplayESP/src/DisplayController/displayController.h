//
// Created by vov4ik on 6/7/20.
//

#ifndef UNTITLED2_SRC_DISPLAYMODULES_RECEIVINGDISPLAYMODULECONFIGURATIOR_H_
#define UNTITLED2_SRC_DISPLAYMODULES_RECEIVINGDISPLAYMODULECONFIGURATIOR_H_

#include <Arduino.h>
class DisplayController {

  ~DisplayController() = default;
 public:
  DisplayController() = default;
  static DisplayController* LoadDisplayModuleConfigurationFromDisk();
  bool BuildDisplayModuleConfiguration(String const& configurationJSON);
  bool UpdateExistingDisplayConfiguration();
  bool SaveDisplayModuleConfiguration(String const& configuration, const char* filename);
  void SetHardwareSerial(HardwareSerial* p_serial);
 protected:
  HardwareSerial* hw_s = nullptr;
};

#endif //UNTITLED2_SRC_DISPLAYMODULES_RECEIVINGDISPLAYMODULECONFIGURATIOR_H_
