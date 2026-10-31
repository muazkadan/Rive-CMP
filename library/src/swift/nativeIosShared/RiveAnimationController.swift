//
// Created by Muaz KADAN on 5.06.2025.
//

import Foundation
import RiveRuntime

@objcMembers public class RiveAnimationController: NSObject {
    private var viewModel: RiveViewModel?
    private var riveView: RiveView?
    private var pendingConfiguration: (url: String, autoPlay: Bool, artboardName: String?, stateMachineName: String?, fit: RiveFit, alignment: RiveAlignment)?

    /// Set before `setAnimationItem` to auto-bind the artboard's default view model instance.
    /// Called with each instance rive-ios binds.
    public var onViewModelInstance: ((RiveDataBindingViewModel.Instance) -> Void)?

    /// Keeps the bound instance alive; rive-ios hands it over without retaining it for callers.
    private var boundViewModelInstance: RiveDataBindingViewModel.Instance?

    /// The instance last handed to `onViewModelInstance`.
    private var deliveredViewModelInstance: RiveDataBindingViewModel.Instance?

    /// Called on the main thread with the artboard's width and height, in artboard units, once the
    /// file has loaded and again whenever they change. It's called on a later turn of the run loop,
    /// so it can be set right after `setAnimationItem` in the same turn.
    public var onArtboardSize: ((Double, Double) -> Void)?

    /// The artboard size last handed to `onArtboardSize`.
    private var reportedArtboardSize: CGSize?

    override init() {
        super.init()
    }

    public func setAnimationItem(
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
        let urlViewModel = ReportingRiveViewModel(
            webURL: url,
            stateMachineName: stateMachineName,
            fit: fit,
            alignment: alignment,
            autoPlay: autoPlay,
            loadCdn: false,
            artboardName: artboardName
        )
        // rive-ios builds a new RiveModel once the download finishes, without auto-binding.
        urlViewModel.onFileLoaded = { [weak self] in
            self?.enableAutoBindIfRequested()
            self?.reportArtboardSize()
        }
        urlViewModel.onAdvance = { [weak self] in self?.reportArtboardSize() }
        viewModel = urlViewModel

        enableAutoBindIfRequested()

        // If view was already requested, create it now
        if riveView == nil {
            createRiveViewIfNeeded()
        }
    }

    public func setAnimationItem(
        data: NSData,
        autoPlay: Bool,
        artboardName: String?,
        stateMachineName: String?,
        fit: RiveFit,
        alignment: RiveAlignment
    ) {
        // Clean up previous resources
        releaseAnimation()

        do {

            // Create RiveFile from NSData
            let riveFile = try RiveFile(data: data as Data, loadCdn: true)

            // Create RiveModel from RiveFile
            let riveModel = RiveModel(riveFile: riveFile)

            // Set artboard if specified
            if let artboardName = artboardName {
                try riveModel.setArtboard(artboardName)
            }

            // Create RiveViewModel from RiveModel
            let dataViewModel: ReportingRiveViewModel
            if let stateMachineName = stateMachineName {
                dataViewModel = ReportingRiveViewModel(
                    riveModel,
                    stateMachineName: stateMachineName,
                    fit: fit,
                    alignment: alignment,
                    autoPlay: autoPlay,
                    artboardName: artboardName
                )
            } else {
                dataViewModel = ReportingRiveViewModel(
                    riveModel,
                    animationName: nil,
                    fit: fit,
                    alignment: alignment,
                    autoPlay: autoPlay,
                    artboardName: artboardName
                )
            }
            dataViewModel.onAdvance = { [weak self] in self?.reportArtboardSize() }
            viewModel = dataViewModel

            enableAutoBindIfRequested()
            reportArtboardSize()

            // If view was already requested, create it now
            if riveView == nil {
                createRiveViewIfNeeded()
            }
        } catch {
            print("RiveAnimationController: ERROR - Failed to create RiveFile from data: \(error)")
        }
    }

    /// Hands the artboard's size to `onArtboardSize` when it differs from the last one reported, on
    /// the next turn of the run loop, which keeps the callback out of the caller's current work.
    /// It's checked on every frame as well as on load, as the bounds can change while playing, e.g.
    /// when reset() instances the artboard again.
    private func reportArtboardSize() {
        guard let size = viewModel?.riveModel?.artboard?.bounds().size, size != reportedArtboardSize else { return }
        reportedArtboardSize = size
        DispatchQueue.main.async { [weak self] in
            self?.onArtboardSize?(Double(size.width), Double(size.height))
        }
    }

    private func enableAutoBindIfRequested() {
        guard onViewModelInstance != nil else { return }
        viewModel?.riveModel?.enableAutoBind { [weak self] instance in
            self?.deliver(instance)
        }
    }

    /// rive-ios binds once for the artboard and once for the state machine, and again on reset and
    /// stop, so several instances can arrive in one turn of the run loop. Only the last stays bound;
    /// it is handed over on the next turn, which also keeps the callback out of the caller's
    /// current work.
    private func deliver(_ instance: RiveDataBindingViewModel.Instance) {
        boundViewModelInstance = instance
        DispatchQueue.main.async { [weak self] in
            guard let self,
                  self.boundViewModelInstance === instance,
                  self.deliveredViewModelInstance !== instance
            else { return }
            self.deliveredViewModelInstance = instance
            self.onViewModelInstance?(instance)
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
        viewModel?.riveModel?.disableAutoBind()
        boundViewModelInstance = nil
        deliveredViewModelInstance = nil
        reportedArtboardSize = nil
        viewModel = nil
        pendingConfiguration = nil
    }
    
    public func setNumberInput(_ name: String, _ value: Float) {
        viewModel?.setInput(name, value: value)
    }
    
    public func setBooleanInput(_ name: String, _ value: Bool) {
        viewModel?.setInput(name, value: value)
    }
    
    public func setTriggerInput(_ name: String) {
        viewModel?.triggerInput(name)
    }

    public func pause() {
        viewModel?.pause()
    }

    /// Restarts the render loop, which rive-ios stops once the state machine settles, so a view
    /// model property changed from code is applied.
    public func resumePlayback() {
        viewModel?.play()
    }

    public func reset() {
        viewModel?.reset()
    }

    public func stop() {
        viewModel?.stop()
    }
}

/// A RiveViewModel that reports when its downloaded file has loaded and when it has advanced a frame.
private final class ReportingRiveViewModel: RiveViewModel {
    var onFileLoaded: (() -> Void)?
    var onAdvance: (() -> Void)?

    override func riveFileDidLoad(_ riveFile: RiveFile) throws {
        try super.riveFileDidLoad(riveFile)
        onFileLoaded?()
    }

    override func player(didAdvanceby seconds: Double, riveModel: RiveModel?) {
        super.player(didAdvanceby: seconds, riveModel: riveModel)
        onAdvance?()
    }
}
