package com.github.huymaster.server.api.utils

import kotlin.reflect.KClass

interface TypeHierarchyResolver {
    fun getDirectSuperclass(clazz: KClass<*>): KClass<*>?
    fun getDirectInterfaces(clazz: KClass<*>): Set<KClass<*>>

    fun getAllSupertypes(clazz: KClass<*>): Set<KClass<*>>
}

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class ReflectionTypeHierarchyResolver() : TypeHierarchyResolver {
    override fun getDirectSuperclass(clazz: KClass<*>): KClass<*>?
    override fun getDirectInterfaces(clazz: KClass<*>): Set<KClass<*>>
    override fun getAllSupertypes(clazz: KClass<*>): Set<KClass<*>>
}

class ClassHierarchyResolver(
    private val resolver: TypeHierarchyResolver = ReflectionTypeHierarchyResolver()
) : TypeHierarchyResolver by resolver {
}