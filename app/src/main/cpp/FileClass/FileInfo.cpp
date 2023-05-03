#include <string>
#include <vector>

using namespace std;

//
// Created by tah9 on 2023/3/6.
//
class FileInfo {
public:
    string path;
    long time{};

    FileInfo() = default;

    FileInfo(string p, long t) : path(std::move(p)), time(t) {
    };
};

class Folder {
public:
    string name;//文件夹名
    string path;//完整路径
    string first_path;//第一张图片路径
    /*
     * 第一张图片路径获取到后，从后往前截取/字符即可获取文件夹路径
     */
//    long s_time;//排序时间
    long m_time;//目录修改时间
    int size;//图片数量
//    vector<FileInfo> *pics = nullptr;

    Folder() = default;

    Folder(string t_name, string t_first_path, long t_m_time, string t_path, int t_size)
            : name(std::move(t_name)),
              first_path(std::move(t_first_path)),
              path(std::move(t_path)),
              size(t_size),
              m_time(t_m_time) {

    };

    ~Folder() {
//        pics->shrink_to_fit();
//        name= nullptr;
//        first_path= nullptr;
    }
};