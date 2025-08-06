// swift-tools-version: 5.9
import PackageDescription

let package = Package(
  name: "exportedNativeIosShared",
  platforms: [.iOS("14.0"), .macOS("10.13"), .tvOS("12.0"), .watchOS("4.0")],
  products: [
    .library(
      name: "exportedNativeIosShared",
      type: .static,
      targets: ["exportedNativeIosShared"])
  ],
  dependencies: [
    .package(url: "https://github.com/rive-app/rive-ios.git", exact: "6.11.1")
  ],
  targets: [
    .target(
      name: "exportedNativeIosShared",
      dependencies: [
        .product(name: "RiveRuntime", package: "rive-ios")
      ],
      path: "Sources"

    )

  ]
)
