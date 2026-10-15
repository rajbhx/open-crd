// open-crd host — Rust rebuild of the Chrome Remote Desktop host (Windows).
//
// Reference artifact (evidence): chromeremotedesktophost.msi 154.0.8037.40.
// Protocol facts are recorded in docs/PROTOCOL.md (FTL signaling, OAuth scope,
// directory endpoints, session transport).
use std::env;
use std::process::ExitCode;

const CRATE_VERSION: &str = env!("CARGO_PKG_VERSION");

fn print_usage() {
    println!(
        "crd-host {} — open-source Rust Chrome Remote Desktop host (rebuild)\n\
         \n\
         Usage:\n\
           crd-host status     show build/reference info and module status\n\
           crd-host --version  show version\n\
           crd-host --help     show this help",
        CRATE_VERSION
    );
}

fn print_status() {
    println!("crd-host {} (open-crd / host-rs)", CRATE_VERSION);
    println!("reference : chromeremotedesktophost.msi 154.0.8037.40 (Windows host)");
    println!("engine    : remoting_core.dll sha256 a0065892... (analyzed, not linked)");
    println!("scope     : https://www.googleapis.com/auth/chromoting.me2me.host");
    println!("signaling : FTL — /chromoting_ftl_ (from docs/PROTOCOL.md)");
    println!("modules   :");
    println!("  config      [ ] planned");
    println!("  auth        [ ] planned (OAuth token getter)");
    println!("  directory   [ ] planned (host registration / heartbeat)");
    println!("  ftl         [ ] planned (signaling)");
    println!("  session     [ ] planned (WebRTC transport)");
}

fn main() -> ExitCode {
    let args: Vec<String> = env::args().skip(1).collect();
    match args.first().map(String::as_str) {
        Some("status") => {
            print_status();
            ExitCode::SUCCESS
        }
        Some("--version") => {
            println!("{}", CRATE_VERSION);
            ExitCode::SUCCESS
        }
        Some("--help") | Some("-h") | Some("help") | None => {
            print_usage();
            ExitCode::SUCCESS
        }
        Some(other) => {
            println!("unknown command: {}\n", other);
            print_usage();
            ExitCode::FAILURE
        }
    }
}
