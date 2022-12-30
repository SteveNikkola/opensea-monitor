package com.stevenikkola.openseamonitor.config

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class AppConfig {
    @Bean
    fun getRestTemplate(): RestTemplate {
        val restTemplate: RestTemplate = RestTemplate()
        val converter = MappingJackson2HttpMessageConverter()
        converter.objectMapper = getObjectMapper()
        converter.supportedMediaTypes = mutableListOf(MediaType.ALL)
        restTemplate.messageConverters = listOf(converter)
        return restTemplate
    }

    fun getObjectMapper(): ObjectMapper {
        val objectMapper: ObjectMapper = jacksonObjectMapper()
        objectMapper.findAndRegisterModules()
        objectMapper.propertyNamingStrategy = PropertyNamingStrategy.SNAKE_CASE
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        return objectMapper
    }
}