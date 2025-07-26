package invirt.mongodb.atlas

import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.search.SearchOperator
import com.mongodb.client.model.search.SearchOptions
import com.mongodb.client.model.search.SearchPath
import org.bson.conversions.Bson
import kotlin.reflect.KProperty

const val DEFAULT_MONGO_SEARCH_INDEX = "default"

/**
 * Converts a [SearchOperator] to a [Bson] aggregate stage.
 */
fun SearchOperator.toAggregate(indexName: String = DEFAULT_MONGO_SEARCH_INDEX): Bson =
    Aggregates.search(this, SearchOptions.searchOptions().index(indexName))

/**
 * Converts a list of [SearchOperator] to a [Bson] aggregate stage.
 */
fun List<SearchOperator>.toAggregate(indexName: String = DEFAULT_MONGO_SEARCH_INDEX): Bson =
    Aggregates.search(SearchOperator.compound().must(this), SearchOptions.searchOptions().index(indexName))

/**
 * Returns the field path for this property.
 */
fun <Value : Any?> KProperty<Value>.fieldPath() = SearchPath.fieldPath(this.name)

/**
 * Returns a text search operator for this property.
 */
fun <Value : Any?> KProperty<Value>.textSearch(query: String) = SearchOperator.text(this.fieldPath(), query)

/**
 * Returns an autocomplete search operator for this property.
 */
fun <Value : Any?> KProperty<Value>.autocomplete(query: String) = SearchOperator.autocomplete(this.fieldPath(), query)
