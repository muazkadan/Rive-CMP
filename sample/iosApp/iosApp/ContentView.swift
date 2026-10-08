import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        // Edge to edge, as on Android: Compose pads its content clear of the safe area and the
        // keyboard itself.
        ComposeView()
                .ignoresSafeArea()
    }
}



