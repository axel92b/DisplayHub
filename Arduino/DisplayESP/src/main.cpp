#include <Arduino.h>
#include <vector>
#include <HardwareSerial.h>
#include <LedMatrix/LedMatrix.h>
#include "Extensions/Utilities.h"

#pragma clang diagnostic push
#pragma ide diagnostic ignored "EndlessLoop"

RGBLEDmatrix ledMatrix(27, 33, 32, 26, 25, 12, 19, 0, 64);
HardwareSerial hs_2(2);
hw_timer_t *timer = nullptr;
TaskHandle_t Task1;
TaskHandle_t Task2;
SemaphoreHandle_t current_screen_mutex;
SemaphoreHandle_t updated_screen_mutex;

bool bIsConfigModeEnabled = false;
bool bConfigScreenReady = false;

void drawScreen(void *);

void drawConfigUpdateScreen();

void serialListener(void *);

void split(const String &, std::vector<String> &, char);

void update_row(int row, String data);

bool check_for_updates();

void clearCurrentScreen();

void IRAM_ATTR onTimer() {
    xSemaphoreTake(current_screen_mutex, portMAX_DELAY );
    ledMatrix.shiftOutRow();
    xSemaphoreGive(current_screen_mutex );
}

std::vector<String> current_screen;
std::vector<unsigned int> current_screen_position;
std::vector<unsigned int> current_screen_string_length;
std::vector<String> updated_Screen;

void setup() {
    Serial.begin(115200);
    hs_2.begin(115200, SERIAL_8N1);
    current_screen_mutex = xSemaphoreCreateMutex();
    updated_screen_mutex = xSemaphoreCreateMutex();
    timer = timerBegin(0, 80, true);
    timerAttachInterrupt(timer, &onTimer, true);
    timerAlarmWrite(timer, 625/* 1000000(freq) / (16(scan line) * 50(FPS)) = 1250 */, true);
    timerAlarmEnable(timer);
    current_screen.reserve(8);
    for (int i = 0; i < 8; i++) {
        current_screen.push_back(String(""));
        current_screen_position.push_back(0);
        current_screen_string_length.push_back(0);
        updated_Screen.push_back(String(""));
    }
    current_screen[2] = "    236333";
    current_screen[3] = "   display";
    current_screen[4] = "       hub";
    xTaskCreatePinnedToCore(drawScreen, "drawScreen", 10000, nullptr, 1, &Task1, 0);
    delay(500);
    xTaskCreatePinnedToCore(serialListener, "serialListener", 10000, nullptr, 1, &Task2, 0);
    delay(500);
}

void loop() {
    while (true){
        delay(100);
    }
}

void serialListener(void *wpvParameters) {
    bool gotFirstModule = false;
    while (true) {
        String received = "";
        while (hs_2.available()) {
            received = hs_2.readStringUntil('\n');
            if (received.startsWith("^Config")) {
                bIsConfigModeEnabled = true;
                clearCurrentScreen();
                Serial.println("Changed to config mode");
                hs_2.write("^Ack\n");
            }
            if (received.startsWith("^Display")) {
                bIsConfigModeEnabled = false;
                Serial.println("Changed to display mode");
                hs_2.write("^Ack\n");
            }
            if (received.startsWith("^Settings")) {
                Serial.println("Received new config, updating");
                Serial.println(received);
                clearCurrentScreen();
                hs_2.write("^Ack\n");
            }
            if (received.startsWith("^Module")) {
                if(!gotFirstModule){
                    clearCurrentScreen();
                    gotFirstModule = true;
                }
                Serial.println("Received new module data");
                Serial.println(received);
                Serial.println("Updating");
                std::vector<String> split_conf;
                split(received, split_conf, '|', false, true);
                update_row(split_conf[0].toInt(), split_conf[1]);
                hs_2.write("^Ack\n");
            }
        }
        delay(1000);
    }
}

void clearCurrentScreen() {
    xSemaphoreTake(current_screen_mutex, portMAX_DELAY);
    for (auto & data : current_screen) {
        data = "";
    }
    xSemaphoreGive(current_screen_mutex);
}

void update_row(int row, String data) {
    xSemaphoreTake(updated_screen_mutex, portMAX_DELAY );
    updated_Screen[row] = std::move(data);
    xSemaphoreGive(updated_screen_mutex);
}

void drawScreen(void *pvParameters) {
    matrixColor s[] = {
            WHITE_COLOR,
            RED_COLOR,
            GREEN_COLOR,
            BLUE_COLOR,
            PURPLE_COLOR,
            BROWN_COLOR,
            CYAN_COLOR,
            WHITE_COLOR
    };
    bool firstDisplay = true;
    ledMatrix.clrScr();
    // Get string length on screen , if screen length < string length, rotate the string,
    // if \n skip enough to make it look like we skipped to next news,
    // create array of skip value for each displayed string!

    while (true) {
        check_for_updates();
        if (bIsConfigModeEnabled) {
            ledMatrix.clrScr();
            drawConfigUpdateScreen();
        } else {
            ledMatrix.clrScr();
            for (int row = 0; row <= 63; row += 8) {
                int row_index = row / 8;
                if (row_index <= current_screen.size() - 1) {
                    int width_to_skip = 0;
                    auto str = current_screen[row_index].c_str();
                    ledMatrix.setColor(s[row_index]);
                    for (int j = 0; j < strlen(str); ++j) {
                        width_to_skip += ledMatrix.drawEngChar(width_to_skip - current_screen_position[row_index], row, str[j]);
                    }
                    if(current_screen_string_length[row_index] - 44 - current_screen_position[row_index]  > 0 && current_screen_string_length[row_index] >= 64){
                        if(current_screen_position[row_index] == 0) {
                            delay(500);
                        }
                        current_screen_position[row_index]++;
                    }else if(current_screen_string_length[row_index] - 44 - current_screen_position[row_index]  <= 0 && current_screen_string_length[row_index] >= 64){
                        delay(200);
                        current_screen_position[row_index] = 0;
                    }
                }
            }

            ledMatrix.swapFrameBuffer();
            ledMatrix.setIntensity(20);
            if (firstDisplay) {
                delay(2000);
                firstDisplay = false;
            }else {
                delay(150);
            }
        }
    }
}

bool check_for_updates() {
    xSemaphoreTake(updated_screen_mutex, portMAX_DELAY );
    bool updateNeeded = false;
    for (auto & data : updated_Screen) {
        if (!data.isEmpty()) {
            updateNeeded = true;
        }
    }
    if(updateNeeded){
        xSemaphoreTake(current_screen_mutex, portMAX_DELAY );
        for (int i = 0 ; i < updated_Screen.size(); i++) {
            if (!updated_Screen[i].isEmpty()) {
                std::swap(updated_Screen[i],current_screen[i]);
                current_screen_position[i] = 0;
                current_screen_string_length[i] = ledMatrix.getStringOnDisplayLength(current_screen[i]);

            }
        }
        xSemaphoreGive(current_screen_mutex );
        for (auto & data : updated_Screen) {
            data = "";
        }
    }
    xSemaphoreGive(updated_screen_mutex);
}

void drawConfigUpdateScreen() {
    ledMatrix.setIntensity(21);
    static String conf_message = "  config";
    const char dot = '.';
    const char space = ' ';
    static int dots_to_write = 1;
    short width_to_skip = 0;
    for (char ch : conf_message) {
        width_to_skip += ledMatrix.drawEngChar(width_to_skip, 3 * 8, ch);
    }
    for (int i = 0; i < 4 - dots_to_write; i++) {
        width_to_skip += ledMatrix.drawEngChar(width_to_skip, 3 * 8, dot);
    }
    for (int i = 0; i < dots_to_write; i++) {
        width_to_skip += ledMatrix.drawEngChar(width_to_skip, 3 * 8, space);
    }
    ledMatrix.swapFrameBuffer();
    dots_to_write = ((dots_to_write + 1) % 3) + 1;
    delay(1000);
}


void split(const String &str, std::vector<String> &cont, char delim = ' ') {
    int from = 1;
    int to = str.indexOf(delim);
    while (to < str.length() && to > from) {
        auto seq = str.substring(from, to);
        cont.push_back(seq);
        from = to + 1;
        to = str.indexOf(delim, from);
        if (to == -1) {
            to = str.length() - 1;
        }
    }
}

#pragma clang diagnostic pop