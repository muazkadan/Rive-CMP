//
// Created by Muaz KADAN on 5.06.2025.
//

import Foundation
import RiveRuntime

@objcMembers public class RiveAnimationController: NSObject {
    private var viewModel: RiveViewModel?
    private var riveView: RiveView?
    private var pendingConfiguration: (url: String, autoPlay: Bool, artboardName: String?, stateMachineName: String?, fit: RiveFit, alignment: RiveAlignment)?
    
    override init() {
        super.init()
    }
    
    public func setAnimationItemWithUrl(
        url: String,
        autoPlay: Bool,
        artboardName: String?,
        stateMachineName: String?,
        fit: RiveFit,
        alignment: RiveAlignment
    ) {
        // Store configuration for deferred creation
        pendingConfiguration = (url, autoPlay, artboardName, stateMachineName, fit, alignment)
        
        // Clean up previous resources
        releaseAnimation()

        // Create view model with proper configuration
        viewModel = RiveViewModel(
            webURL: url,
            stateMachineName: stateMachineName,
            fit: fit,
            alignment: alignment,
            autoPlay: autoPlay,
            loadCdn: false,
            artboardName: artboardName
        )

        // If view was already requested, create it now
        if riveView == nil {
            createRiveViewIfNeeded()
        }
    }
    
    private func createRiveViewIfNeeded() {
        guard let vm = viewModel else {
            return
        }
        
        riveView = vm.createRiveView()
        riveView?.autoresizingMask = [.flexibleWidth, .flexibleHeight]
    }

    public func createAnimationView() -> UIView {
        // If we have a viewModel but no view, create the view
        if viewModel != nil && riveView == nil {
            createRiveViewIfNeeded()
        }
        
        // If we have a view, return it
        if let view = riveView {
            return view
        }
        
        // If no viewModel yet, return a placeholder and wait for configuration
        let placeholderView = RiveView()
        placeholderView.backgroundColor = UIColor.clear
        return placeholderView
    }

    public func updateView(_ view: UIView) {
        // Try to create view if we have viewModel but no riveView
        if viewModel != nil && riveView == nil {
            createRiveViewIfNeeded()
        }
        
        guard let vm = viewModel, let storedView = riveView else {
            return
        }
        vm.update(view: storedView)
    }

    public func releaseAnimation() {
        if riveView != nil {
            riveView?.removeFromSuperview()
        }
        riveView = nil
        viewModel = nil
        pendingConfiguration = nil
    }
}
