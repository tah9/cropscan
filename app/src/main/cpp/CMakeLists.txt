
cmake_minimum_required(VERSION 3.18.1)

# Declares and names the project.

# CMAKE_CXX_FLAGS是一个预定义的CMake变量，用于存储编译C++源文件所需的所有编译器标志
# 设置编译器标志，指定c++11  （将-std=c++11附加到${CMAKE_CXX_FLAGS}变量的末尾）
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")


# 定义一个变量来设置so库的名称，这里改完记得在java引用处改掉引用的so库名称
set(GALLERY_LIBRARY_NAME native-gallery)


#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fexceptions -frtti")
## 去除未使用函数与变量
#set(CMAKE_CXX_FLAGS "${CMAKE_C_FLAGS} -ffunction-sections -fdata-sections")

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
        ${GALLERY_LIBRARY_NAME}

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        JniMethod.cpp)

#添加子目录,将会调用子目录中的CMakeLists.txt
#ADD_SUBDIRECTORY(FileClass)

include_directories(src/main/cpp/FileClass)

#find_library( # Sets the name of the path variable.
#        log-lib
#
#        # Specifies the name of the NDK library that
#        # you want CMake to locate.
#        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

# 将log-lib库文件连接到自己的库
#target_link_libraries(
#        ${GALLERY_LIBRARY_NAME}
#        # included in the NDK.
#        ${log-lib})


## 设置去除未使用代码的链接flag
#SET_TARGET_PROPERTIES(${GALLERY_LIBRARY_NAME} PROPERTIES LINK_FLAGS "-Wl,--gc-sections")
#SET_TARGET_PROPERTIES(${GALLERY_LIBRARY_NAME} PROPERTIES LINK_FLAGS "-Wl,--gc-sections,--icf=safe,-s")


#
## 将库文件打包和压缩到APK文件中
#set_target_properties(${GALLERY_LIBRARY_NAME}
#        PROPERTIES
#        LIBRARY_OUTPUT_DIRECTORY "${CMAKE_CURRENT_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}"
#        LIBRARY_OUTPUT_DIRECTORY_DEBUG "${CMAKE_CURRENT_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}"
#        LIBRARY_OUTPUT_DIRECTORY_RELEASE "${CMAKE_CURRENT_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}")
#
