package com.tujahelper

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class TujaHelperApplication

fun main(args: Array<String>) {
    runApplication<TujaHelperApplication>(*args)
}
