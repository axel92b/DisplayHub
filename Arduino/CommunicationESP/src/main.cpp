
#include <Arduino.h>
#include <HardwareSerial.h>
#include <BluetoothSerial.h>
#include <WiFi.h>
#include <SPIFFS.h>
#include "Extensions/Utilities.h"
#include "TransmitterController/transmitterController.h"

HardwareSerial* hw_s = nullptr;
BluetoothSerial* SerialBT = nullptr;
TransmitterController* tc = nullptr;

void setup() {
    hw_s = new HardwareSerial(2);
    SPIFFS.begin(true);
    SerialBT = new BluetoothSerial();
    Serial.begin(115200);
    SerialBT->begin("ESP32_Dev");
    WiFiClass::mode(WIFI_STA);
    WiFi.disconnect();
    hw_s->begin(115200, SERIAL_8N1);
    tc = TransmitterController::LoadDisplayModuleConfiguration(SerialBT, hw_s);
    if (!tc) {
        tc = new TransmitterController();
        tc->SetBt(SerialBT);
        tc->SetHardwareSerial(hw_s);
        debugPrint("No configuration found on flash!");
    } else {
        debugPrint("Successfully loaded configuration from flash!");
    }
}

void loop() {
    String rxBuffer;
    if(WiFiClass::status() !=  WL_CONNECTED){
        tc->LoadWifiConfig();
    }
    if (SerialBT->available()) {
        rxBuffer = SerialBT->readStringUntil('\n');
    }
    if (!rxBuffer.isEmpty()) {
        if (rxBuffer.startsWith("^WifiConnect")) {
            tc->UpdateWifiConfiguration(rxBuffer);
            tc->SaveDisplayModuleConfiguration(rxBuffer, "/wifi_config.txt");
        } else if (rxBuffer.startsWith("^Settings")) {
            if (tc->BuildDisplayModuleConfiguration(rxBuffer, true)) {
                tc->SaveDisplayModuleConfiguration(rxBuffer, "/config.txt");
//                tc->SendNewConfigurationToDisplay(rxBuffer);
            }
        } else {
            debugPrint("Got unexpected data: " + rxBuffer);
        }
    } else {
        if(WiFiClass::status() !=  WL_CONNECTED){
            tc->LoadWifiConfig();
        }
        tc->UpdateExistingDisplayConfiguration(false);
        delay(1000);
    }
    delay(100);
}




