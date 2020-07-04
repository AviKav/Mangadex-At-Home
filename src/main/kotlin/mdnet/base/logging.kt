package mdnet.base

import org.slf4j.Logger

inline fun Logger.error(msg: () -> String) {
    if (this.isErrorEnabled) {
        this.error(msg())
    }
}
inline fun Logger.warn(msg: () -> String) {
    if (this.isWarnEnabled) {
        this.warn(msg())
    }
}
inline fun Logger.info(msg: () -> String) {
    if (this.isInfoEnabled) {
        this.info(msg())
    }
}
inline fun Logger.debug(msg: () -> String) {
    if (this.isDebugEnabled) {
        this.debug(msg())
    }
}
inline fun Logger.trace(msg: () -> String) {
    if (this.isTraceEnabled) {
        this.trace(msg())
    }
}
inline fun Logger.error(e: Throwable, msg: () -> String) {
    if (this.isErrorEnabled) {
        this.error(msg(), e)
    }
}
inline fun Logger.warn(e: Throwable, msg: () -> String) {
    if (this.isWarnEnabled) {
        this.warn(msg(), e)
    }
}
inline fun Logger.info(e: Throwable, msg: () -> String) {
    if (this.isInfoEnabled) {
        this.info(msg(), e)
    }
}
inline fun Logger.debug(e: Throwable, msg: () -> String) {
    if (this.isDebugEnabled) {
        this.debug(msg(), e)
    }
}
inline fun Logger.trace(e: Throwable, msg: () -> String) {
    if (this.isTraceEnabled) {
        this.trace(msg(), e)
    }
}
