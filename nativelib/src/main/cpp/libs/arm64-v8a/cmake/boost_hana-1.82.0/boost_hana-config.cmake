# Generated by BoostInstall.cmake for boost_hana-1.82.0

include(CMakeFindDependencyMacro)

find_dependency(boost_config 1.82.0 EXACT)
find_dependency(boost_core 1.82.0 EXACT)
find_dependency(boost_fusion 1.82.0 EXACT)
find_dependency(boost_mpl 1.82.0 EXACT)
find_dependency(boost_tuple 1.82.0 EXACT)

include("${CMAKE_CURRENT_LIST_DIR}/boost_hana-targets.cmake")
