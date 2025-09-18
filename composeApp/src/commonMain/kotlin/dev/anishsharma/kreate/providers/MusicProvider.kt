package dev.anishsharma.kreate.providers

import dev.anishsharma.kreate.core.music.Track

interface MusicProvider {
    suspend fun search(query: String, limit: Int = 20): List<Track>
    suspend fun getByUrl(url: String): Track?
    suspend fun getById(id: String): Track?
    
    val isAvailable: Boolean get() = true
}
