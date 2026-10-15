// open-crd receiver — C/C++ rebuild of the Chrome Remote Desktop receiver (client).
//
// Reference artifact (evidence only): com.google.chromeremotedesktop 79.0.3945.26
// (Android APK). This binary is a Windows/desktop rebuild target; no Android
// toolchain (NDK) is used or required anywhere in this project.
#include <cstdio>
#include <string>

#include "version.hpp"

namespace {

void print_usage() {
  std::printf(
      "crd-receiver %s — open-source C/C++ Chrome Remote Desktop receiver (rebuild)\n"
      "\n"
      "Usage:\n"
      "  crd-receiver status     show build/reference info and module status\n"
      "  crd-receiver --version  show version\n"
      "  crd-receiver --help     show this help\n",
      CRD_RECEIVER_VERSION);
}

void print_status() {
  std::printf("crd-receiver %s (open-crd / receiver-cpp)\n", CRD_RECEIVER_VERSION);
  std::printf("reference : com.google.chromeremotedesktop 79.0.3945.26 (APK — evidence only)\n");
  std::printf("engine    : libremoting_client_jni.so sha256 FDF043CD... (analyzed, not linked)\n");
  std::printf("modules   :\n");
  std::printf("  config      [ ] planned\n");
  std::printf("  auth        [ ] planned (OAuth — chromoting scope)\n");
  std::printf("  directory   [ ] planned (host list)\n");
  std::printf("  ftl         [ ] planned (signaling)\n");
  std::printf("  session     [ ] planned (WebRTC transport)\n");
}

}  // namespace

int main(int argc, char** argv) {
  if (argc < 2) {
    print_usage();
    return 0;
  }

  const std::string cmd = argv[1];
  if (cmd == "status") {
    print_status();
    return 0;
  }
  if (cmd == "--version") {
    std::printf("%s\n", CRD_RECEIVER_VERSION);
    return 0;
  }
  if (cmd == "--help" || cmd == "-h" || cmd == "help") {
    print_usage();
    return 0;
  }

  std::printf("unknown command: %s\n\n", cmd.c_str());
  print_usage();
  return 1;
}
