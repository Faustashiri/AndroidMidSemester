package io.github.dekabrsky.androidMidSemester.data.repository

import android.content.Context
import io.github.dekabrsky.androidMidSemester.data.api.SStatsApi
import io.github.dekabrsky.androidMidSemester.data.api.dto.SStatsTeamDto
import io.github.dekabrsky.androidMidSemester.domain.model.Team
import io.github.dekabrsky.androidMidSemester.domain.repository.TeamRepository

class TeamRepositoryImpl(
    private val api: SStatsApi,
    context: Context,
) : TeamRepository {
    private val preferences = context.getSharedPreferences("team_selection", Context.MODE_PRIVATE)

    override suspend fun russianTeams(): List<Team> {
        val teams = mutableListOf<SStatsTeamDto>()
        var offset = 0
        var totalCount: Int
        do {
            val response = api.teams(offset = offset)
            if (response.status != "OK") error("SStats вернул ошибку: ${response.status}")
            teams += response.data
            totalCount = response.totalCount
            offset += response.count
            if (response.count == 0) break
        } while (offset < totalCount)
        return teams.map { Team(it.id, it.name, it.logoUrl) }
    }

    override fun selectedTeamIds(): Set<Long> = preferences.getStringSet("selected_ids", emptySet())
        .orEmpty().mapNotNull(String::toLongOrNull).toSet()

    override fun saveSelectedTeamIds(ids: Set<Long>) {
        preferences.edit().putStringSet("selected_ids", ids.map(Long::toString).toSet()).apply()
    }
}
