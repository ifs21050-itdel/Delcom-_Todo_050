package com.ifs21050.lostfound.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.ifs21050.lostfound.data.local.entity.DelcomLostFoundEntity
import com.ifs21050.lostfound.data.local.room.DelcomLostFoundDatabase
import com.ifs21050.lostfound.data.local.room.IDelcomLostFoundDao
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class LocalLostFoundRepository(context: Context) {
    private val mDelcomLostFoundDao: IDelcomLostFoundDao
    private val executorService: ExecutorService = Executors.newSingleThreadExecutor()

    init {
        val db = DelcomLostFoundDatabase.getInstance(context)
        mDelcomLostFoundDao = db.delcomLostFoundDao()
    }

    fun getAllLostFounds(): LiveData<List<DelcomLostFoundEntity>?> = mDelcomLostFoundDao.getAllLostFounds()

    fun get(todoId: Int): LiveData<DelcomLostFoundEntity?> = mDelcomLostFoundDao.get(todoId)

    fun insert(todo: DelcomLostFoundEntity) {
        executorService.execute { mDelcomLostFoundDao.insert(todo) }
    }

    fun delete(todo: DelcomLostFoundEntity) {
        executorService.execute { mDelcomLostFoundDao.delete(todo) }
    }

    companion object {
        @Volatile
        private var INSTANCE: LocalLostFoundRepository? = null

        fun getInstance(
            context: Context
        ): LocalLostFoundRepository {
            synchronized(LocalLostFoundRepository::class.java) {
                INSTANCE = LocalLostFoundRepository(
                    context
                )
            }
            return INSTANCE as LocalLostFoundRepository
        }
    }
}
