package com.ifs21050.lostandfound.data.repository

import com.google.gson.Gson
import com.ifs21050.lostandfound.data.remote.MyResult
import com.ifs21050.lostandfound.data.remote.response.DelcomResponse
import com.ifs21050.lostandfound.data.remote.retrofit.IApiService
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException

class LostFoundRepository private constructor(
    private val apiService: IApiService,
) {
    fun postLostFound(
        title: String,
        description: String,
        status: String
    ) = flow {
        emit(MyResult.Loading)
        try {
            //get success message
            emit(
                MyResult.Success(
                    apiService.postLostFound(title, description, status).data
                )
            )
        } catch (e: HttpException) {
            //get error message
            val jsonInString = e.response()?.errorBody()?.string()
            emit(
                MyResult.Error(
                    Gson()
                        .fromJson(jsonInString, DelcomResponse::class.java)
                        .message
                )
            )
        }
    }


    fun putLostFound(
        lostandfoundId: Int,
        title: String,
        description: String,
        status: String,
        is_completed: Boolean,
    ) = flow {
        emit(MyResult.Loading)
        try {
            //get success message
            emit(
                MyResult.Success(
                    apiService.putLostFound(
                        lostandfoundId,
                        title,
                        description,
                        status,
                        if (is_completed) 1 else 0
                    )
                )
            )
        } catch (e: HttpException) {
            //get error message
            val jsonInString = e.response()?.errorBody()?.string()
            emit(
                MyResult.Error(
                    Gson()
                        .fromJson(jsonInString, DelcomResponse::class.java)
                        .message
                )
            )
        }
    }

    fun getLostFounds(
        isCompleted: Int?,
        isMe: Int?,
        status: String?,
    ) = flow {
        emit(MyResult.Loading)
        try {
            //get success message
            emit(
                MyResult.Success(
                    apiService.getLostFounds(isCompleted, isMe, status)
                )
            )
        } catch (e: HttpException) {
            //get error message
            val jsonInString = e.response()?.errorBody()?.string()
            emit(
                MyResult.Error(
                    Gson()
                        .fromJson(jsonInString, DelcomResponse::class.java)
                        .message
                )
            )
        }
    }

    fun getLostFound(
        lostfoundId: Int,
    ) = flow {
        emit(MyResult.Loading)
        try {
            //get success message
            emit(
                MyResult.Success(
                    apiService.getLostFound(lostfoundId)
                )
            )
        } catch (e: HttpException) {
            //get error message
            val jsonInString = e.response()?.errorBody()?.string()
            emit(
                MyResult.Error(
                    Gson()
                        .fromJson(jsonInString, DelcomResponse::class.java)
                        .message
                )
            )
        }
    }


    fun deleteLostFound(
        lostfoundId: Int,
    ) = flow {
        emit(MyResult.Loading)
        try {
            //get success message
            emit(
                MyResult.Success(
                    apiService.deleteLostFound(lostfoundId)
                )
            )
        } catch (e: HttpException) {
            //get error message
            val jsonInString = e.response()?.errorBody()?.string()
            emit(
                MyResult.Error(
                    Gson()
                        .fromJson(jsonInString, DelcomResponse::class.java)
                        .message
                )
            )
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: LostFoundRepository? = null
        fun getInstance(
            apiService: IApiService,
        ): LostFoundRepository {
            synchronized(LostFoundRepository::class.java) {
                INSTANCE = LostFoundRepository(
                    apiService
                )
            }
            return INSTANCE as LostFoundRepository
        }
    }
}
