package io.github.breninsul.servlet.caching.exception

open class InputStreamReadAlreadyStartedException :RuntimeException("Input stream already started, can't init cached content")