/*
 Red LED Matrix driver (Using SPI interface) - derived from UTFT lib
 Support: 64x16 - one board, 128x16 - two board side by side, 64x32 - two boards, one under the other
 Using double buffer for flicker free operation, uses an interrupt service routine based on timer set for 10 frames per second
*/

// Choose only one!!!

#ifndef LEDMATRIX_h
#define LEDMATRIX_h

#include <SPI.h>
#include <Extensions/charMap.h>
#include <WString.h>

enum matrixColor {
    BLACK_COLOR,
    WHITE_COLOR,
    GREEN_COLOR,
    RED_COLOR,
    BLUE_COLOR,
    PURPLE_COLOR,
    BROWN_COLOR,
    CYAN_COLOR
};


class LEDmatrix {
protected:
    BoldCharsMap engCharMap;
    bool frameBufferFlag;
    char latchPin, en_74138, la_74138, lb_74138, lc_74138, ld_74138, le_74138;
    matrixColor color;
    short maxX, maxY, scanRow;
    unsigned char *currentFrameBuffer, *nowDisplayingFrameBuffer;
    unsigned char *frameBuffer[2];
    void swap(short *a, short *b);

public:
    LEDmatrix() = default;

    int getCharWidth(unsigned short c);
    int getMatrixXSize();
    int getMatrixYSize();
    int getStringWidth(const char *str);
    short drawEngChar(short x, short y, char c);
    unsigned char printUTF8char(unsigned short c, short x, short y);
    unsigned int getStringOnDisplayLength(const String &s);
    virtual void clrScr() = 0;
    virtual void drawPixel(short x, short y) = 0;
    virtual void fillScr() = 0;
    virtual void shiftOutRow() = 0;
    void drawCircle(short x, short y, int radius);
    void drawHLine(short x, short y, int l);
    void drawHebStringUTF8(short x, short y, const char *str, bool swapString = true);
    void drawLine(short x1, short y1, short x2, short y2);
    void drawRect(short x1, short y1, short x2, short y2);
    void drawRoundRect(short x1, short y1, short x2, short y2);
    void drawVLine(short x, short y, int l);
    void dumpFrameBuffer();
    void fillCircle(short x, short y, int radius);
    void fillRect(short x1, short y1, short x2, short y2);
    void fillRoundRect(short x1, short y1, short x2, short y2);
    void printNumF(double num, char dec, short x, short y, char divider = '.', int length = 0, char filler = ' ');
    void printNumI(long num, short x, short y, int length = 0, char filler = ' ');
    void reverseUTF8string(char *str);
    void setColor(matrixColor color);
    void swapFrameBuffer();
};

class RGBLEDmatrix : public LEDmatrix {
private:
    unsigned int intensity = 500;
public:
    RGBLEDmatrix(char latchPin, char en_74138, char la_74138, char lb_74138, char lc_74138, char ld_74138,
                 char le_74138, char spiInterface = 0, char ySize = 32);

    ~RGBLEDmatrix() {
        free(frameBuffer[0]);
        free(frameBuffer[1]);
    }

    void clrScr();
    void drawPixel(short x, short y);
    void fillScr();
    void setIntensity(unsigned char val);
    void shiftOutRow();

};

class RedLEDmatrix : public LEDmatrix {
public:
    RedLEDmatrix(char latchPin, char en_74138, char la_74138, char lb_74138, char lc_74138, char ld_74138,
                 char le_74138, char spiInterface = 0);

    ~RedLEDmatrix() {
        free(frameBuffer[0]);
        free(frameBuffer[1]);
    }

    void clrScr();
    void drawPixel(short x, short y);
    void fillScr();
    void shiftOutRow();
};


#endif
