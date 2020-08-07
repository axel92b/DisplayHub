//
// Created by vov4ik on 5/23/20.
//

#ifndef UNTITLED7_SRC_EXTENSIONS_LETTER_H_
#define UNTITLED7_SRC_EXTENSIONS_LETTER_H_
#include <vector>


class Letter {
 public:
    short width = 0;
    short height = 0;
    using BitMap = std::vector<std::vector<char>>;
    BitMap pixels_rows;

    explicit Letter(BitMap&& bitMap): pixels_rows(bitMap) {
        if (!pixels_rows.empty()) {
            width = pixels_rows[0].size();
            height = pixels_rows.size();
        }
    }
    Letter& operator=(const Letter& lt)= default;
};

#endif //UNTITLED7_SRC_EXTENSIONS_LETTER_H_
