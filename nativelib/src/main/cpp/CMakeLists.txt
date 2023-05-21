# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.18.1)

# Declares and names the project.

project("nativelib")

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.
file(GLOB SOURCES "./*.cpp")
file(GLOB COOL "./cool/*.cpp")
file(GLOB BOOST_LIB_SO "boost_libs/${ANDROID_ABI}/*.so")
file(GLOB BOOST_HEADER "./boost/")


add_library( # Sets the name of the library.
        nativelib

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        #nativelib.cpp
        ${SOURCES}
        ${COOL} )



# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.
#set(CMAKE_FIND_ROOT_PATH_MODE_LIBRARY BOTH)
#set(CMAKE_FIND_ROOT_PATH_MODE_INCLUDE BOTH)
#set(CMAKE_FIND_ROOT_PATH_MODE_PACKAGE BOTH)
#set_target_properties(boost PROPERTIES LINKER_LANGUAGE CXX)

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)


#add_executable (boost ${source_files})

# Assuming this is meant to be a public directory
#target_include_directories(HelloWorld PUBLIC "includes/")


#add_library( boost SHARED ${BOOST_LIB_SO} )

#set_target_properties( boost PROPERTIES IMPORTED_LOCATION ${BOOST_LIB_SO})




# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        nativelib

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})


#if (NOT nativelib)
#message(FATAL_ERROR "Foo lib not found! ${BOOST_LIB_SO}")
#endif()
#set(BOOST_ROOT "./")
#set( Boost_INCLUDE_DIR ./include )
#set( Boost_LIBRARY_DIR ./libs/${ANDROID_ABI} )
#set( Boost_Version 1_79 )


#message("cool ${CMAKE_SOURCE_DIR}")


include_directories(${CMAKE_SOURCE_DIR}/include)
include_directories(${CMAKE_SOURCE_DIR}/include/icu/common)
include_directories(${CMAKE_SOURCE_DIR}/include/icu/i18n)

file(GLOB ICU_LIBRARIES "${CMAKE_SOURCE_DIR}/icu_libs/${ANDROID_ABI}/lib/*.a")
target_link_libraries(nativelib ${ICU_LIBRARIES})

file(GLOB BOOST_LIBRARIES "${CMAKE_SOURCE_DIR}/boost_libs/${ANDROID_ABI}/*.so")
target_link_libraries(nativelib ${BOOST_LIBRARIES})







