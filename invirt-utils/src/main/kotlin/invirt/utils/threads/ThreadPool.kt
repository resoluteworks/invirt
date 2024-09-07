package invirt.utils.threads

import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future

class ThreadPool<T>(threadCount: Int) : AutoCloseable {

    private val threadPool = Executors.newFixedThreadPool(threadCount)
    private val futures = mutableListOf<Future<T>>()

    fun submit(task: () -> T): Future<T> {
        val future = threadPool.submit(Callable { task() })
        futures.add(future)
        return future
    }

    override fun close() {
        futures.forEach { it.get() }
        threadPool.shutdownNow()
    }

    fun shutdown(timeout: Long, unit: java.util.concurrent.TimeUnit) {
        threadPool.shutdown()
        threadPool.awaitTermination(timeout, unit)
    }
}
