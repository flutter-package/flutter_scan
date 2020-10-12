#import "ScanPlugin.h"
#if __has_include(<scan/scan-Swift.h>)
#import <scan/scan-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "scan-Swift.h"
#endif

@implementation ScanPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftScanPlugin registerWithRegistrar:registrar];
}
@end
