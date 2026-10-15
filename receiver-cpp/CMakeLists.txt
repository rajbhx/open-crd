cmake_minimum_required(VERSION 3.16)
project(open_crd_receiver LANGUAGES C CXX)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_C_STANDARD 11)

add_executable(crd-receiver
  src/main.cpp
)

target_include_directories(crd-receiver PRIVATE src)

if(WIN32)
  target_compile_definitions(crd-receiver PRIVATE
    WIN32_LEAN_AND_MEAN
    NOMINMAX
    _CRT_SECURE_NO_WARNINGS
  )
endif()
