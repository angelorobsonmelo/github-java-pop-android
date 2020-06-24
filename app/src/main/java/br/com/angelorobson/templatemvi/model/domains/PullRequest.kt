package br.com.angelorobson.templatemvi.model.domains

data class PullRequest(
        val title: String,
        val description: String,
        val user: User,
        val htmlUrl: String
)