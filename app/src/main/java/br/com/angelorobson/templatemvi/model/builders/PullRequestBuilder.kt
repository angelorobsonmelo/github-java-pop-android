package br.com.angelorobson.templatemvi.model.builders

import br.com.angelorobson.templatemvi.model.domains.PullRequest
import br.com.angelorobson.templatemvi.model.domains.User

class PullRequestBuilder {

    data class Builder(
            private var title: String = "",
            private var description: String = "",
            private var user: User = User(),
            private var htmlUrl: String = ""
    ) {

        fun title(title: String) =
                apply { this.title = title }

        fun description(description: String) = apply { this.description = description }
        fun user(user: User) = apply { this.user = user }
        fun htmlUrl(htmlUrl: String) = apply { this.htmlUrl = htmlUrl }

        fun onePullRequest() = apply {
            title = "pull request title"
            description = "pull request description"
            user = UserBuilder.Builder().oneUser().build()
            htmlUrl = "https://pullrequest"
        }

        fun build() = PullRequest(
                title = title,
                description = description,
                user = user,
                htmlUrl = htmlUrl
        )
    }
}