package dev.bokov.mondaydotcom.di

import com.apollographql.apollo3.ApolloClient
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.bokov.mondaydotcom.data.repository.MondayRepositoryImpl
import dev.bokov.mondaydotcom.domain.interactor.MondayInteractor
import dev.bokov.mondaydotcom.domain.interactor.MondayInteractorImpl
import dev.bokov.mondaydotcom.domain.repository.MondayRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindMondayRepository(
        impl: MondayRepositoryImpl
    ): MondayRepository

    @Binds
    @Singleton
    abstract fun bindMondayInteractor(
        impl: MondayInteractorImpl
    ): MondayInteractor

    companion object {
        /**
         * Replace this with your own Monday.com API token
         */
        private const val MONDAY_TOKEN = ""

        @Provides
        @Singleton
        fun provideApolloClient(): ApolloClient {
            return ApolloClient.Builder()
                .serverUrl("https://api.monday.com/v2")
                .addHttpHeader(
                    "Authorization",
                    "Bearer $MONDAY_TOKEN"
                )
                .build()
        }
    }
}
