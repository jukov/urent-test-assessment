package info.jukov.urenttestassestment.core.pagination

public data class Page<T>(val items: List<T>, val page: Int, val limit: Int, val total: Long?)

public sealed interface PaginationStatus {
    public data object Idle : PaginationStatus
    public data object Loading : PaginationStatus
    public data object EndReached : PaginationStatus
    public data class Error(val cause: Throwable) : PaginationStatus
}

public data class PaginationSnapshot<T>(
    val items: List<T> = emptyList(),
    val nextPage: Int = FIRST_PAGE,
    val status: PaginationStatus = PaginationStatus.Idle
) {
    public companion object {
        public const val FIRST_PAGE: Int = 1
    }
}

/** Small reusable state machine for cursor-free, 1-based page APIs. */
public class PaginationStateMachine<T, Id>(private val idOf: (T) -> Id) {
    public var snapshot: PaginationSnapshot<T> = PaginationSnapshot()
        private set

    public fun reset() {
        snapshot = PaginationSnapshot()
    }

    public fun start(): Boolean {
        if (snapshot.status is PaginationStatus.Loading || snapshot.status is PaginationStatus.EndReached) return false
        snapshot = snapshot.copy(status = PaginationStatus.Loading)
        return true
    }

    public fun append(result: Page<T>) {
        val merged = (snapshot.items + result.items).distinctBy(idOf)
        val validMetadata = result.total != null && result.total >= 0 && result.limit > 0 && result.page > 0
        val reachedEnd = result.items.isEmpty() || if (validMetadata) {
            result.page.toLong() * result.limit >= requireNotNull(result.total)
        } else {
            result.limit <= 0 || result.items.size < result.limit
        }
        snapshot = PaginationSnapshot(
            items = merged,
            nextPage = result.page.coerceAtLeast(snapshot.nextPage) + 1,
            status = if (reachedEnd) PaginationStatus.EndReached else PaginationStatus.Idle
        )
    }

    public fun fail(cause: Throwable) {
        snapshot = snapshot.copy(status = PaginationStatus.Error(cause))
    }
}
