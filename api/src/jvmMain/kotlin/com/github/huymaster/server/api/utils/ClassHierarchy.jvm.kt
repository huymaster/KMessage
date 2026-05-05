package com.github.huymaster.server.api.utils

import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class ReflectionTypeHierarchyResolver : TypeHierarchyResolver {
    companion object {
        private val cache = ConcurrentHashMap<KClass<*>, Set<KClass<*>>>()
    }

    actual override fun getDirectSuperclass(clazz: KClass<*>): KClass<*>? =
        clazz.java.superclass?.kotlin

    actual override fun getDirectInterfaces(clazz: KClass<*>): Set<KClass<*>> =
        clazz.java.interfaces.map { it.kotlin }.toSet()

    actual override fun getAllSupertypes(clazz: KClass<*>): Set<KClass<*>> {
        cache[clazz]?.let { return it }

        val result = mutableSetOf<KClass<*>>()
        collectAllSupertypes(clazz, result)

        val finalSet = result.toSet()
        cache[clazz] = finalSet
        return finalSet
    }

    private fun collectAllSupertypes(clazz: KClass<*>, result: MutableSet<KClass<*>>) {
        val cached = cache[clazz]
        if (cached != null) {
            result.addAll(cached)
            return
        }

        val superclass = getDirectSuperclass(clazz)
        if (superclass != null && result.add(superclass))
            collectAllSupertypes(superclass, result)

        getDirectInterfaces(clazz).forEach { interf ->
            if (result.add(interf)) collectAllSupertypes(interf, result)
        }
    }
}