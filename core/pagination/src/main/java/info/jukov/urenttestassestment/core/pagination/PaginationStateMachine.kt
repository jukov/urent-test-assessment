package info.jukov.urenttestassestment.core.pagination

public data class Page<T>(val items: List<T>, val page: Int, val limit: Int, val total: Long?)

public sealed interface PaginationState<out T> {
    public data object EmptyProgress : PaginationState<Nothing>
    public data object Empty : PaginationState<Nothing>
    public data class EmptyFailure(val cause: Throwable) : PaginationState<Nothing>
    public data class Content<T>(val items: List<T>) : PaginationState<T>
    public data class ContentProgress<T>(val items: List<T>) : PaginationState<T>
    public data class ContentFailure<T>(val items: List<T>, val cause: Throwable) : PaginationState<T>
    public data class ContentRefreshing<T>(val items: List<T>) : PaginationState<T>
    public data class ContentEndReached<T>(val items: List<T>) : PaginationState<T>
}

public sealed interface PaginationEffect {
    public data class RefreshFailure(val cause: Throwable) : PaginationEffect
}

/** Reusable state machine for cursor-free, 1-based page APIs. */
public class PaginationStateMachine<T, Id>(private val idOf: (T) -> Id) {
    public var state: PaginationState<T> = PaginationState.EmptyProgress
        private set

    public var nextPage: Int = FIRST_PAGE
        private set
    private var pageBeforeRefresh: Int? = null
    private var stateBeforeRefresh: PaginationState<T>? = null

    public fun startInitial() {
        pageBeforeRefresh = null
        stateBeforeRefresh = null
        nextPage = FIRST_PAGE
        state = PaginationState.EmptyProgress
    }

    public fun startAppend(): Boolean {
        state = when (val current = state) {
            is PaginationState.Content -> PaginationState.ContentProgress(current.items)
            is PaginationState.ContentFailure -> PaginationState.ContentProgress(current.items)
            else -> return false
        }
        return true
    }

    public fun startRefresh(): Boolean {
        val previousNextPage = nextPage
        state = when (val current = state) {
            is PaginationState.Content -> {
                stateBeforeRefresh = current
                PaginationState.ContentRefreshing(current.items)
            }

            is PaginationState.ContentProgress -> {
                stateBeforeRefresh = PaginationState.Content(current.items)
                PaginationState.ContentRefreshing(current.items)
            }

            is PaginationState.ContentEndReached -> {
                stateBeforeRefresh = current
                PaginationState.ContentRefreshing(current.items)
            }

            is PaginationState.ContentFailure -> {
                stateBeforeRefresh = current
                PaginationState.ContentRefreshing(current.items)
            }

            is PaginationState.ContentRefreshing -> return false

            else -> {
                stateBeforeRefresh = null
                PaginationState.EmptyProgress
            }
        }
        pageBeforeRefresh = previousNextPage
        nextPage = FIRST_PAGE
        return true
    }

    public fun append(result: Page<T>) {
        val previousItems = state.itemsOrEmpty()
        val replacing = state is PaginationState.EmptyProgress || state is PaginationState.ContentRefreshing
        val items = (if (replacing) result.items else previousItems + result.items).distinctBy(idOf)
        val endReached = reachedEnd(result, items.size)
        nextPage = result.page.coerceAtLeast(if (replacing) FIRST_PAGE else nextPage) + 1
        pageBeforeRefresh = null
        stateBeforeRefresh = null
        state = when {
            items.isEmpty() -> PaginationState.Empty
            endReached -> PaginationState.ContentEndReached(items)
            else -> PaginationState.Content(items)
        }
    }

    public fun fail(cause: Throwable): PaginationEffect? {
        when (val current = state) {
            PaginationState.EmptyProgress -> state = PaginationState.EmptyFailure(cause)

            is PaginationState.ContentProgress -> state = PaginationState.ContentFailure(current.items, cause)

            is PaginationState.ContentRefreshing -> {
                nextPage = pageBeforeRefresh ?: nextPage
                pageBeforeRefresh = null
                val restored = stateBeforeRefresh ?: PaginationState.Content(current.items)
                stateBeforeRefresh = null
                state = restored
                return PaginationEffect.RefreshFailure(cause)
            }

            else -> Unit
        }
        return null
    }

    private fun reachedEnd(result: Page<T>, accumulatedSize: Int): Boolean {
        val validMetadata = result.total != null &&
            result.total >= accumulatedSize &&
            result.limit > 0 &&
            result.page > 0
        return result.items.isEmpty() || if (validMetadata) {
            result.page.toLong() * result.limit >= requireNotNull(result.total)
        } else {
            result.limit <= 0 || result.items.size < result.limit
        }
    }

    private fun PaginationState<T>.itemsOrEmpty(): List<T> = when (this) {
        is PaginationState.Content -> items

        is PaginationState.ContentProgress -> items

        is PaginationState.ContentFailure -> items

        is PaginationState.ContentRefreshing -> items

        is PaginationState.ContentEndReached -> items

        PaginationState.EmptyProgress,
        PaginationState.Empty,
        is PaginationState.EmptyFailure -> emptyList()
    }

    public companion object {
        public const val FIRST_PAGE: Int = 1
    }
}
