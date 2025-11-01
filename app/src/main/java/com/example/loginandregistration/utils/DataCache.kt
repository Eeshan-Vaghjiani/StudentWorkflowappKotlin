package com.example.loginandregistration.utils

import com.example.loginandregistration.models.FirebaseGroup
import com.example.loginandregistration.models.UserInfo
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Simple in-memory cache for frequently accessed data. Uses LRU-like eviction based on access time
 * and TTL (Time To Live).
 */
object DataCache {
    private const val DEFAULT_TTL_MINUTES = 5L
    private const val MAX_CACHE_SIZE = 100

    private data class CacheEntry<T>(
            val data: T,
            val timestamp: Long = System.currentTimeMillis(),
            var lastAccessed: Long = System.currentTimeMillis()
    ) {
        fun isExpired(ttlMinutes: Long = DEFAULT_TTL_MINUTES): Boolean {
            val age = System.currentTimeMillis() - timestamp
            return age > TimeUnit.MINUTES.toMillis(ttlMinutes)
        }
    }

    // User profile cache
    private val userCache = ConcurrentHashMap<String, CacheEntry<UserInfo>>()

    // Group membership cache (userId -> list of group IDs)
    private val groupMembershipCache = ConcurrentHashMap<String, CacheEntry<List<String>>>()

    // Group details cache
    private val groupCache = ConcurrentHashMap<String, CacheEntry<FirebaseGroup>>()

    /** Cache user profile information */
    fun cacheUser(userId: String, userInfo: UserInfo) {
        evictIfNeeded(userCache)
        userCache[userId] = CacheEntry(userInfo)
    }

    /** Get cached user profile */
    fun getUser(userId: String): UserInfo? {
        val entry = userCache[userId] ?: return null

        if (entry.isExpired()) {
            userCache.remove(userId)
            return null
        }

        entry.lastAccessed = System.currentTimeMillis()
        return entry.data
    }

    /** Cache group membership for a user */
    fun cacheGroupMembership(userId: String, groupIds: List<String>) {
        evictIfNeeded(groupMembershipCache)
        groupMembershipCache[userId] = CacheEntry(groupIds)
    }

    /** Get cached group membership */
    fun getGroupMembership(userId: String): List<String>? {
        val entry = groupMembershipCache[userId] ?: return null

        if (entry.isExpired()) {
            groupMembershipCache.remove(userId)
            return null
        }

        entry.lastAccessed = System.currentTimeMillis()
        return entry.data
    }

    /** Cache group details */
    fun cacheGroup(groupId: String, group: FirebaseGroup) {
        evictIfNeeded(groupCache)
        groupCache[groupId] = CacheEntry(group)
    }

    /** Get cached group details */
    fun getGroup(groupId: String): FirebaseGroup? {
        val entry = groupCache[groupId] ?: return null

        if (entry.isExpired()) {
            groupCache.remove(groupId)
            return null
        }

        entry.lastAccessed = System.currentTimeMillis()
        return entry.data
    }

    /** Invalidate user cache entry */
    fun invalidateUser(userId: String) {
        userCache.remove(userId)
    }

    /** Invalidate group membership cache for a user */
    fun invalidateGroupMembership(userId: String) {
        groupMembershipCache.remove(userId)
    }

    /** Invalidate group cache entry */
    fun invalidateGroup(groupId: String) {
        groupCache.remove(groupId)
    }

    /** Clear all caches */
    fun clearAll() {
        userCache.clear()
        groupMembershipCache.clear()
        groupCache.clear()
    }

    /** Clear expired entries from all caches */
    fun clearExpired() {
        userCache.entries.removeIf { it.value.isExpired() }
        groupMembershipCache.entries.removeIf { it.value.isExpired() }
        groupCache.entries.removeIf { it.value.isExpired() }
    }

    /** Evict least recently used entries if cache is too large */
    private fun <T> evictIfNeeded(cache: ConcurrentHashMap<String, CacheEntry<T>>) {
        if (cache.size >= MAX_CACHE_SIZE) {
            // Remove expired entries first
            cache.entries.removeIf { it.value.isExpired() }

            // If still too large, remove least recently accessed
            if (cache.size >= MAX_CACHE_SIZE) {
                val lruKey = cache.entries.minByOrNull { it.value.lastAccessed }?.key

                lruKey?.let { cache.remove(it) }
            }
        }
    }

    /** Get cache statistics for debugging */
    fun getStats(): CacheStats {
        return CacheStats(
                userCacheSize = userCache.size,
                groupMembershipCacheSize = groupMembershipCache.size,
                groupCacheSize = groupCache.size
        )
    }

    data class CacheStats(
            val userCacheSize: Int,
            val groupMembershipCacheSize: Int,
            val groupCacheSize: Int
    )
}
