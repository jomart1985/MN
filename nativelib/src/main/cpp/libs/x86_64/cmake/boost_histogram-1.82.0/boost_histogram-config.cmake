# Generated by BoostInstall.cmake for boost_histogram-1.82.0

include(CMakeFindDependencyMacro)

find_dependency(boost_config 1.82.0 EXACT)
find_dependency(boost_core 1.82.0 EXACT)
find_dependency(boost_mp11 1.82.0 EXACT)
find_dependency(boost_throw_exception 1.82.0 EXACT)
find_dependency(boost_variant2 1.82.0 EXACT)
find_dependency(boost_math 1.82.0 EXACT)

include("${CMAKE_CURRENT_LIST_DIR}/boost_histogram-targets.cmake")
