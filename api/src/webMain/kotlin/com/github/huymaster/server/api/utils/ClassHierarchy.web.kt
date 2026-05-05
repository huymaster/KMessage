package com.github.huymaster.server.api.utils

import kotlin.reflect.KClass

actual class ReflectionTypeHierarchyResolver : TypeHierarchyResolver {
    actual override fun getDirectSuperclass(clazz: KClass<*>): KClass<*>? {
        TODO("Not yet implemented")
    }

    actual override fun getDirectInterfaces(clazz: KClass<*>): Set<KClass<*>> {
        TODO("Not yet implemented")
    }

    actual override fun getAllSupertypes(clazz: KClass<*>): Set<KClass<*>> {
        TODO("Not yet implemented")
    }
}