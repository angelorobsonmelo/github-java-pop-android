package br.com.angelorobson.templatemvi.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.com.angelorobson.templatemvi.R
import br.com.angelorobson.templatemvi.model.services.PullRequestService
import br.com.angelorobson.templatemvi.model.services.RepositoryGitService
import br.com.angelorobson.templatemvi.view.pullrequest.PullRequestViewModel
import br.com.angelorobson.templatemvi.view.repositories.RepositoriesViewModel
import br.com.angelorobson.templatemvi.view.utils.ActivityService
import br.com.angelorobson.templatemvi.view.utils.IdlingResource
import br.com.angelorobson.templatemvi.view.utils.Navigator
import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import dagger.*
import dagger.multibindings.IntoMap
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Provider
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlin.reflect.KClass


@MustBeDocumented
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
internal annotation class ViewModelKey(val value: KClass<out ViewModel>)

@MustBeDocumented
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
internal annotation class ApiBaseUrl

interface ApplicationComponent {

    fun viewModelFactory(): ViewModelProvider.Factory

    fun activityService(): ActivityService

}

@Singleton
@Component(modules = [ApplicationModule::class, ViewModelModule::class, ApiModule::class, RealModule::class])
interface RealComponent : ApplicationComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun context(context: Context): Builder

        fun build(): RealComponent
    }
}

@Module
object ApplicationModule {

    @Provides
    @Singleton
    @JvmStatic
    fun viewModels(viewModels: MutableMap<Class<out ViewModel>, Provider<ViewModel>>):
            ViewModelProvider.Factory {
        return ViewModelFactory(viewModels)
    }

    @Provides
    @Singleton
    @JvmStatic
    fun activityService(): ActivityService = ActivityService()

    @Provides
    @Singleton
    @JvmStatic
    fun navigator(activityService: ActivityService): Navigator {
        return Navigator(R.id.navigationHostFragment, activityService)
    }

    @Provides
    @ApiBaseUrl
    @JvmStatic
    fun apiBaseUrl(context: Context): String = context.getString(R.string.api_base_url)
}

@Module
abstract class ViewModelModule {


    @Binds
    @IntoMap
    @ViewModelKey(RepositoriesViewModel::class)
    abstract fun repositoriesViewModel(viewModel: RepositoriesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PullRequestViewModel::class)
    abstract fun pullRequestViewModel(viewModel: PullRequestViewModel): ViewModel
}


@Module
object ApiModule {

    @Provides
    @Singleton
    @JvmStatic
    fun okHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build()
    }

    @Provides
    @Singleton
    @JvmStatic
    fun retrofit(@ApiBaseUrl apiBaseUrl: String, okHttpClient: OkHttpClient): Retrofit {

        val d = Moshi.Builder().add(customDateAdapter).build()

        return Retrofit.Builder()
                .baseUrl(apiBaseUrl)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(MoshiConverterFactory.create(d))
                .client(okHttpClient)
                .build()
    }


    @Provides
    @Singleton
    @JvmStatic
    fun repositoryService(retrofit: Retrofit): RepositoryGitService {
        return retrofit.create(RepositoryGitService::class.java)
    }


    @Provides
    @Singleton
    @JvmStatic
    fun pullRequestService(retrofit: Retrofit): PullRequestService {
        return retrofit.create(PullRequestService::class.java)
    }

}


@Module
object RealModule {

    @Provides
    @Singleton
    @JvmStatic
    fun idlingResource(): IdlingResource = object : IdlingResource {
        override fun increment() {}
        override fun decrement() {}
    }
}

var customDateAdapter: Any = object : Any() {
    var dateFormat: DateFormat? = null

    @ToJson
    @Synchronized
    fun dateToJson(d: Date?): String? {
        return dateFormat?.format(d)
    }

    @FromJson
    @Synchronized
    @Throws(ParseException::class)
    fun dateToJson(s: String?): Date? {
        return dateFormat?.parse(s)
    }

    init {
        dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm")
        (dateFormat as SimpleDateFormat).timeZone = TimeZone.getTimeZone("GMT")
    }
}
