package com.github.huymaster.server.core.dto

import org.ktorm.entity.Entity

interface BaseDto<E : Entity<E>> {
    fun toEntity(): E
}