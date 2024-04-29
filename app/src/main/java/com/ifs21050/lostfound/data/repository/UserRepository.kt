package com.ifs21050.lostfound.data.repository

import com.google.gson.Gson
import com.ifs21050.lostfound.data.remote.MyResult
import com.ifs21050.lostfound.data.remote.retrofit.IApiService
import com.ifs21050.lostfound.data.remote.response.DelcomResponse
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import retrofit2.HttpException


class UserRepository private constructor(
    private val apiService: IApiService,
) {

    fun getMe() = flow {
        emit(MyResult.Loading)
        try {
            //get success message
            emit(MyResult.Success(apiService.getMe().data))
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

    fun addPhotoProfile(
        cover: MultipartBody.Part,
    ) = flow {
        emit(MyResult.Loading)
        try {
            //get success message
            emit(MyResult.Success(apiService.addPhotoProfile( cover)))
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
        private var INSTANCE: UserRepository? = null
        fun getInstance(
            apiService: IApiService,
        ): UserRepository {
            synchronized(UserRepository::class.java) {
                INSTANCE = UserRepository(
                    apiService
                )
            }
            return INSTANCE as UserRepository
        }
    }
}