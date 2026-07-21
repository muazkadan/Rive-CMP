package dev.muazkadan.rivecmp.native

/**
 * A custom exception signifying a problem with some Rive components.
 *
 * @param message A description of the issue.
 */
public sealed class RiveException(message: String) : Exception(message)

/**
 * A custom exception signifying a problem with the supplied animation name.
 *
 * @param message A description of the issue.
 */

public class AnimationException(message: String) : RiveException(message)

/**
 * A custom exception signifying a problem with the supplied artboard name.
 *
 * @param message A description of the issue.
 */
public class ArtboardException(message: String) : RiveException(message)

/**
 * A custom exception signifying a problem with the data in the file.
 *
 * @param message A description of the issue.
 */
public class MalformedFileException(message: String) : RiveException(message)

/**
 * A custom exception signifying a problem with the supplied state machine name.
 *
 * @param message A description of the issue.
 */
public class StateMachineException(message: String) : RiveException(message)

/**
 * A custom exception signifying a problem with a supplied state machine input name.
 *
 * @param message A description of the issue.
 */
public class StateMachineInputException(message: String) : RiveException(message)

/**
 * A custom exception signifying the current file does not support this runtime version.
 *
 * @param message A description of the issue.
 */
public class UnsupportedRuntimeVersionException(message: String) : RiveException(message)

/**
 * A custom exception signifying a problem with when working
 * with a [ViewModel] or
 * [ViewModelInstance][ViewModelInstance].
 *
 * @param message A description of the issue.
 */
public class ViewModelException(message: String) : RiveException(message)
