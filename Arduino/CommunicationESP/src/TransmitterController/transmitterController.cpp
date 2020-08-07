//
// Created by vov4ik on 6/7/20.
//

#include "transmitterController.h"
#include "DisplayModules/displayModuleFactory.h"
#include "Extensions/Utilities.h"
#include "SPIFFS.h"
#include "WiFi.h"
#include <vector>

using namespace std;


TransmitterController *TransmitterController::LoadDisplayModuleConfiguration(BluetoothSerial *p_serial,
                                                                             HardwareSerial *p_hardware_serial) {
    String display_configuration;
    File file = SPIFFS.open("/config.txt", FILE_READ);
    if (!file) {
        return nullptr;
    }
    while (true) {
        String line = file.readString();
        display_configuration += line;
        if (line.isEmpty()) {
            break;
        }
    }
    file.close();
    if (display_configuration.length() == 0) {
        return nullptr;
    }
    auto tdmc = new TransmitterController();
    tdmc->SetHardwareSerial(p_hardware_serial);
    tdmc->SetBt(p_serial);
    if (tdmc->BuildDisplayModuleConfiguration(display_configuration, false)) {
        tdmc->LoadWifiConfig();
        tdmc->SendNewConfigurationToDisplay(display_configuration);
        return tdmc;
    } else {
        delete tdmc;
    }
    return nullptr;
}

bool TransmitterController::BuildDisplayModuleConfiguration(const String &receivedConfiguration,
                                                            bool updateModulesInitially) {
    for(auto* module: modules){
        if(module) {
            module = nullptr;
        }
    }
    modules.clear();

    if (!SetDisplayMode("Config")) {
        debugPrint("Display isn't responding!");
    }

    debugPrint(receivedConfiguration);

    std::vector<String> split_conf;

    split(receivedConfiguration, split_conf, '|', false, true);
    auto new_modules = getModulesFromConfiguration(split_conf);

    for (const auto &module : new_modules) {
        debugPrint("Creating module " + module.first);
        debugPrint("Module args " + module.second);
        AbstractModule *am = DisplayModuleFactory::createDisplayDataModule(module.first, module.second);
        if (am) {
            Serial.print(am->GetModuleName() + "\n");
        }
        modules.push_back(am);
    }
    if (!modules.empty()) {
        debugPrint("Successfully built configuration with " + String(modules.size()) + " modules");
    } else {
        debugPrint("Couldn't create modules out of the following configuration:");
        debugPrint(receivedConfiguration);
        if (!SetDisplayMode("Display")) {
            debugPrint("Display isn't responding!");
        }
        return false;
    }
    if (!SetDisplayMode("Display")) {
        debugPrint("Display isn't responding!");
    }
    if (updateModulesInitially) {
        UpdateExistingDisplayConfiguration(true);
    }
    return true;
}

bool compareModules(const AbstractModule* a, const AbstractModule* b) {
    if(!b){
        return a;
    }
    if(!a){
        return b;
    }
    return a->GetRow() < b->GetRow();
}

bool TransmitterController::UpdateExistingDisplayConfiguration(bool forceUpdate) {
    sort(modules.begin(), modules.end(),compareModules);
    for (auto module : modules) {
        if (module) {
            if (module->Update(forceUpdate)) {
                Serial.print("Module: ");
                Serial.print(module->GetModuleName());
                Serial.println(" Updated!");
                int retries = 3;
                String received = "";
                bool ackReceived = false;
                while (retries-- > 0) {
                    SendUpdatedModulesDataToDisplay(module->GetRow(), module->getDisplayDataString());
                    if(module->GetModuleName().startsWith("News")){
                        delay(1000);
                    }
                    delay(600);
                    while (hw_s->available()) {
                        received = hw_s->readStringUntil('\n');
                        if (received.indexOf("^Ack") >= 0) {
                            debugPrint("Display received new ModuleData!");
                            ackReceived = true;
                            retries = 0;
                            break;
                        } else {
                            debugPrint("Received garbage: " + received);
                        }
                    }
                    if (ackReceived) {
                        retries = 0;
                        break;
                    }
                    delay(200);
                }
                if (ackReceived) {
                    debugPrint("Ack Received");
                }
            }
        }
    }
    return true;
}

bool TransmitterController::UpdateWifiConfiguration(String const &configuration) {
    debugPrint(configuration);
    pair<String, String> credentials = extractWifiCredentials(configuration);
    WiFi.begin(credentials.first.c_str(), credentials.second.c_str());
    int counter = 30;
    while (WiFiClass::status() != WL_CONNECTED && counter-- > 0) {
        delay(500);
        debugPrint(".", true);
    }
    debugPrint("");
    if (WiFiClass::status() != WL_CONNECTED) {
        bt->print("^WifiStatus|Error\n");
        debugPrint("Can't connect to desired WIFI");
        WiFi.disconnect();
        return false;
    } else {
        bt->print("^WifiStatus|Success\n");
    }
    debugPrint("WiFi connected");
    debugPrint("IP address: ");
    debugPrint(WiFi.localIP());
    return true;
}

pair<String, String> TransmitterController::extractWifiCredentials(String const &rxBuffer) {
    vector<String> args;
    split(rxBuffer, args, '|');
    return pair<String, String>(args[1], args[2]);
}

void TransmitterController::SetBt(BluetoothSerial *p_serial) {
    TransmitterController::bt = p_serial;
}

bool TransmitterController::SendUpdatedModulesDataToDisplay(int row, const String &update_data) {
    int retries = 3;
    String received = "";
    bool ackReceived = false;
    while (retries-- > 0) {
        if (hw_s->availableForWrite()) {
            const String &update_module_string = String("^Module|" + String(row) + "|" + update_data + "\n");
            hw_s->write(update_module_string.c_str());
            debugPrint(update_module_string);
        }
        delay(500);
        while (hw_s->available()) {
            received = hw_s->readStringUntil('\n');
            if (received.indexOf("^Ack") >= 0) {
                debugPrint("Display received Updated_data!");
                ackReceived = true;
                break;
            } else {
                debugPrint("Received garbage: " + received);
            }
        }
        if (ackReceived) {
            break;
        }
        delay(2000);
    }
    if (ackReceived) {
        debugPrint("Ack Received");
    }
    return ackReceived;
}

bool TransmitterController::SendNewConfigurationToDisplay(const String &config) {
    int retries = 3;
    String received = "";
    bool ackReceived = false;
    while (retries-- > 0) {
        if (hw_s->availableForWrite()) {
            hw_s->write(String("^Settings\n").c_str());
            debugPrint("sent config  To Display\n");
        }
        delay(500);
        while (hw_s->available()) {
            received = hw_s->readStringUntil('\n');
            if (received.indexOf("^Ack") >= 0) {
                debugPrint("Display received new config!");
                ackReceived = true;
                break;
            } else {
                debugPrint("Received garbage: " + received);
            }
        }
        if (ackReceived) {
            break;
        }
        delay(2000);
    }
    if (ackReceived) {
        debugPrint("Ack Received");
    }
    return ackReceived;
}

bool TransmitterController::SetDisplayMode(String const &mode) {
    int retries = 3;
    String received = "";
    bool ackReceived = false;
    while (retries-- > 0) {
        if (hw_s->availableForWrite()) {
            String configCommand("^" + mode + "\n");
            hw_s->write(configCommand.c_str());
            debugPrint("sent ^" + mode);
        }
        delay(500);
        while (hw_s->available()) {
            received = hw_s->readStringUntil('\n');
            if (received.indexOf("^Ack") >= 0) {
                debugPrint("Display is In " + mode + " mode!");
                ackReceived = true;
                break;
            } else {
                debugPrint("Received garbage: " + received);
            }
        }
        if (ackReceived) {
            break;
        }
        delay(2000);
    }
    if (ackReceived) {
        debugPrint("Ack Received");
    }
    return ackReceived;
}

void TransmitterController::LoadWifiConfig() {
    wifi_config = "";
    File file = SPIFFS.open("/wifi_config.txt", FILE_READ);
    if (!file) {
        debugPrint("Couldn't load WiFi configuration!");
        return;
    }
    while (true) {
        String line = file.readString();
        wifi_config += line;
        if (line.isEmpty()) {
            break;
        }
    }
    file.close();
    if (wifi_config.length() == 0) {
        debugPrint("WiFi configuration was empty!");
        return;
    }
    UpdateWifiConfiguration(wifi_config);
}

bool TransmitterController::SaveDisplayModuleConfiguration(String const &configuration, const char *filename) {
    File file = SPIFFS.open(filename, FILE_WRITE);
    if (!file) {
        return false;
    }
    debugPrint(configuration);
    if (file.print(configuration)) {
        debugPrint("File was written with \n" + configuration);
    } else {
        debugPrint("File write failed with \n" + configuration);
    }
    file.close();
    return false;
}

void TransmitterController::SetHardwareSerial(HardwareSerial *p_serial) {
    hw_s = p_serial;
}