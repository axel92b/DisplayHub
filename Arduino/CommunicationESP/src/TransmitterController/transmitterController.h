//
// Created by vov4ik on 6/7/20.
//

#ifndef UNTITLED2_SRC_DISPLAYMODULES_TRANSMITTINGDISPLAYMODULECONFIGURATOR_H_
#define UNTITLED2_SRC_DISPLAYMODULES_TRANSMITTINGDISPLAYMODULECONFIGURATOR_H_

#include "DisplayModules/AbstractModule.h"
#include <BluetoothSerial.h>
#include <vector>
#include <Arduino.h>

class TransmitterController {
 private:
  std::vector<AbstractModule*> modules;
  BluetoothSerial* bt = nullptr;
  String wifi_config{""};

  bool SetDisplayMode(const String& mode);
 public:
  ~TransmitterController() = default;
  void SetBt(BluetoothSerial* p_serial);
  static TransmitterController* LoadDisplayModuleConfiguration(BluetoothSerial* p_serial, HardwareSerial* p_hardware_serial);
  bool UpdateExistingDisplayConfiguration(bool forceUpdate);
  bool UpdateWifiConfiguration(String const& configuration);
  static std::pair<String, String> extractWifiCredentials(String const& rxBuffer) ;
  bool BuildDisplayModuleConfiguration(const String &receivedConfiguration,
                                       bool updateModulesInitially);
  bool SendUpdatedModulesDataToDisplay(int row, const String &update_data);
  bool SendNewConfigurationToDisplay(const String& config);
  void LoadWifiConfig();
  bool SaveDisplayModuleConfiguration(String const& configuration, const char* filename);
  void SetHardwareSerial(HardwareSerial* p_serial);
 protected:
  HardwareSerial* hw_s = nullptr;
};

#endif //UNTITLED2_SRC_DISPLAYMODULES_TRANSMITTINGDISPLAYMODULECONFIGURATOR_H_
