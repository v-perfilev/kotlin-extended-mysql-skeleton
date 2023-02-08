package com.persoff68.fatodo.config.aop.logging

import com.persoff68.fatodo.config.constant.AppConstants
import com.persoff68.fatodo.exception.AbstractException
import mu.KotlinLogging
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.AfterThrowing
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.springframework.stereotype.Component
import java.util.*

@Aspect
@Component
class LoggingAspect {
    companion object {
        private const val CONTROLLER_POINTCUT = "within(" + AppConstants.CONTROLLER_PATH + "..*)"
        private const val TASK_POINTCUT = "within(" + AppConstants.TASK_PATH + "..*)"

        private val logger = KotlinLogging.logger {}
    }

    @Pointcut("$CONTROLLER_POINTCUT || $TASK_POINTCUT")
    fun applicationPackagePointcut() {
        // pointcut for controllers and services
    }

    @AfterThrowing(pointcut = "applicationPackagePointcut()", throwing = "e")
    fun logAfterThrowing(joinPoint: JoinPoint, e: Throwable) {
        val declaringTypeName = joinPoint.signature.declaringTypeName
        val name = joinPoint.signature.name
        val message = if (e.message != null) e.message else "NULL"
        if (e is AbstractException) {
            logger.warn("Exception in {}.{}() with cause = {}", declaringTypeName, name, message)
        } else {
            logger.error("Exception in {}.{}() with cause = {}", declaringTypeName, name, message)
        }
    }

    @Around("applicationPackagePointcut()")
    @Throws(Throwable::class)
    fun logAround(joinPoint: ProceedingJoinPoint): Any {
        val declaringTypeName = joinPoint.signature.declaringTypeName
        val name = joinPoint.signature.name
        val args = Arrays.toString(joinPoint.args)
        logger.debug("Enter: {}.{}() with argument[s] = {}", declaringTypeName, name, args)
        return try {
            val result = joinPoint.proceed()
            logger.debug("Exit: {}.{}() with result = {}", declaringTypeName, name, result)
            result
        } catch (e: IllegalArgumentException) {
            logger.error("Illegal argument: {} in {}.{}()", args, declaringTypeName, name)
            throw e
        }
    }

}
