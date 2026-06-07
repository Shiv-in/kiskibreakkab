package com.example.kiskibreakkab.di

import android.content.Context
import androidx.room.Room
import com.example.kiskibreakkab.data.repository.*
import com.example.kiskibreakkab.domain.repository.*
import com.example.kiskibreakkab.data.local.AppDatabase
import com.example.kiskibreakkab.data.local.dao.TimetableDao
import com.example.kiskibreakkab.data.local.dao.UserDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "kiski_break_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideTimetableDao(database: AppDatabase): TimetableDao {
        return database.timetableDao()
    }

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore,
        userDao: UserDao,
        @ApplicationContext context: Context
    ): AuthRepository = AuthRepositoryImpl(auth, firestore, userDao, context)

    @Provides
    @Singleton
    fun provideDashboardRepository(
        firestore: FirebaseFirestore,
        userDao: UserDao
    ): DashboardRepository = DashboardRepositoryImpl(firestore, userDao)

    @Provides
    @Singleton
    fun provideTimetableRepository(
        firestore: FirebaseFirestore,
        timetableDao: TimetableDao
    ): TimetableRepository = TimetableRepositoryImpl(firestore, timetableDao)

    @Provides
    @Singleton
    fun provideFriendRepository(
        firestore: FirebaseFirestore
    ): FriendRepository = FriendRepositoryImpl(firestore)

    @Provides
    @Singleton
    fun provideSquadRepository(
        firestore: FirebaseFirestore
    ): SquadRepository = SquadRepositoryImpl(firestore)

    @Provides
    @Singleton
    fun provideRoomRepository(
        firestore: FirebaseFirestore
    ): RoomRepository = RoomRepositoryImpl(firestore)

    @Provides
    @Singleton
    fun provideProfileRepository(
        firestore: FirebaseFirestore,
        userDao: UserDao
    ): ProfileRepository = ProfileRepositoryImpl(firestore, userDao)
}
