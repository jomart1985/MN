#include <jni.h>
#include <string>
#include <boost/regex.hpp>
#include <boost/regex/icu.hpp>
#include <regex>
#include <unicode/utypes.h>
#include <boost/config.hpp>


extern "C" JNIEXPORT jstring JNICALL
Java_com_example_nativelib_NativeLib_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";

   // boost::u32regex expr = boost::make_u32regex("[[:Mn:]]");

    std::string html = hello;
    std::regex tags("<[^>]*>");
    std::string remove{};

    html = std::regex_replace(html, tags, remove);

    static const boost::regex e("<(From )?([A-Za-z0-9_]+)>(.*?)");
    std::string chat_input("<Darker> Hello");
    boost::match_results<std::string::const_iterator> results;
    if (boost::regex_match(chat_input, results, e))
    {
        std::string playername = results[2];  //Get contents of the second (...)
    }

   /* boost::u32regex expr3 = boost::make_u32regex(u8"[^[:w:][:digit:]\\s]");
    std::string fmt3{ " " };
    html = boost::u32regex_replace(html, expr3, fmt3);*/


    return env->NewStringUTF(hello.c_str());
}