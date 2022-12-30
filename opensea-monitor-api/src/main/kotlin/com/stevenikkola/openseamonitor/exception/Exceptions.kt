package com.stevenikkola.openseamonitor.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.BAD_REQUEST)
class BadRequestException(override val message: String?) : RuntimeException(message)

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
class GoogleCloudFunctionException(override val message: String?) : RuntimeException(message)

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
class OpenSeaMonitorException(override val message: String?) : RuntimeException(message)