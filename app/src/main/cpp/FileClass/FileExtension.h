//
// Created by tah9 on 2023/3/19.
//

#ifndef DEMO2_23_FILEEXTENSION_H
#define DEMO2_23_FILEEXTENSION_H


namespace ets {
    const static std::string pic_extension[] = {".jpg", ".png", ".jpeg", ".webp"};
/*, ".gif"*/
    //判断文件是否是图片（根据文件名后缀）
    inline int isPicture(const std::string &name) {
        //文件名总长不足图片后缀长
        if (name.length() < 5)return 0;
        //对几个后缀进行循环判断
        for (int i = 0; i < 4; ++i) {
            if (name.rfind(pic_extension[i]) != std::string::npos)  return 1;
        }
        return 0;
    }
}
#endif //DEMO2_23_FILEEXTENSION_H
