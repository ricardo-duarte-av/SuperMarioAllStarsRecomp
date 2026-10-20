# Adds tests/smas_stats.c (a test-only counter printed when the game exits) to the test build made by
# tests/build_game.sh. It is never part of the shipped game. (src/smas_extras.c is listed in
# CMakeLists.txt itself.) The path is expanded now because CMAKE_CURRENT_LIST_DIR changes by the time
# the deferred call runs.
get_filename_component(_smas_root "${CMAKE_CURRENT_LIST_DIR}/.." ABSOLUTE)
cmake_language(EVAL CODE "
cmake_language(DEFER DIRECTORY \"${CMAKE_SOURCE_DIR}\" CALL target_sources SuperMarioAllStarsRecomp PRIVATE
    \"${_smas_root}/tests/smas_stats.c\")")
