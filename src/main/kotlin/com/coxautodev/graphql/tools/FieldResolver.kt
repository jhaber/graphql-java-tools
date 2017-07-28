package com.coxautodev.graphql.tools

import graphql.language.FieldDefinition
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment

/**
 * @author Andrew Potter
 */
internal abstract class FieldResolver(val field: FieldDefinition, val search: FieldResolverScanner.Search, relativeTo: JavaType) {
    val resolverInfo: ResolverInfo = search.resolverInfo
    val genericType = GenericType(search.type).relativeToPotentialParent(relativeTo)

    abstract fun scanForMatches(): List<TypeClassMatcher.PotentialMatch>
    abstract fun createDataFetcher(): DataFetcher<*>

    /**
     * Add source resolver depending on whether or not this is a resolver method
     */
    protected fun getSourceResolver(): SourceResolver = if(this.search.source != null) {
        ({ this.search.source })
    } else {
        ({ environment ->
            val source = environment.getSource<Any>()

            if(!this.genericType.isAssignableFrom(source.javaClass)) {
                throw ResolverError("Expected source object to be an instance of '${this.genericType.getRawClass().name}' but instead got '${source.javaClass.name}'")
            }

            source
        })
    }
}

internal typealias SourceResolver = (DataFetchingEnvironment) -> Any
