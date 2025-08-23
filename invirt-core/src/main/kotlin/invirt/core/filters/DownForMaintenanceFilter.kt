package invirt.core.filters

import invirt.core.UriPatternMatcher
import invirt.core.views.renderTemplate
import org.http4k.core.Filter
import org.http4k.core.Status

/**
 * A filter that can be used to put the site into "down for maintenance" mode.
 * When enabled, all requests (except for health checks and static assets) will receive a 503 Service Unavailable response.
 * This can be useful during deployments or maintenance windows to prevent users from encountering errors.
 */
object DownForMaintenanceFilter {

    /**
     * Creates a filter that puts the site into "down for maintenance" mode when [isDownForMaintenance] returns true.
     *
     * @param isDownForMaintenance A function that returns true if the site is currently down for maintenance.
     * @param view The view (template) to render when the site is down for maintenance.
     * @param excludePathPatterns Patterns to exclude from the maintenance mode. Requests matching these patterns
     * will be allowed to proceed as normal.
     */
    operator fun invoke(
        view: String,
        excludePathPatterns: Set<String>,
        isDownForMaintenance: () -> Boolean
    ): Filter = invoke(view, UriPatternMatcher(excludePathPatterns), isDownForMaintenance)

    /**
     * Creates a filter that puts the site into "down for maintenance" mode when [isDownForMaintenance] returns true.
     *
     * @param isDownForMaintenance A function that returns true if the site is currently down for maintenance.
     * @param view The view (template) to render when the site is down for maintenance.
     * @param excludedPathsMatcher A [UriPatternMatcher] to determine which requests should be excluded from maintenance mode.
     * Requests matching the patterns in the matcher will be allowed to proceed as normal.
     */
    operator fun invoke(
        view: String,
        excludedPathsMatcher: UriPatternMatcher,
        isDownForMaintenance: () -> Boolean
    ): Filter = Filter { next ->
        { request ->
            if (!isDownForMaintenance() || excludedPathsMatcher.matches(request)) {
                next(request)
            } else {
                renderTemplate(view).status(Status.SERVICE_UNAVAILABLE)
            }
        }
    }
}
