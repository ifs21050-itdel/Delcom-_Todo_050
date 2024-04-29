package com.ifs21050.lostfound.presentation.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.ifs21050.lostfound.data.pref.UserModel
import com.ifs21050.lostfound.data.remote.MyResult
import com.ifs21050.lostfound.data.remote.response.DelcomLostFoundsResponse
import com.ifs21050.lostfound.data.remote.response.DelcomResponse
import com.ifs21050.lostfound.data.repository.AuthRepository
import com.ifs21050.lostfound.data.repository.LostFoundRepository
import com.ifs21050.lostfound.presentation.ViewModelFactory
import kotlinx.coroutines.launch

class MainViewModel(
    private val authRepository: AuthRepository,
    private val lostfoundRepository: LostFoundRepository
) : ViewModel() {

    fun getSession(): LiveData<UserModel> {
        return authRepository.getSession().asLiveData()
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun getLostFounds(): LiveData<MyResult<DelcomLostFoundsResponse>> {
        return lostfoundRepository.getLostFounds(null, 0, null).asLiveData()
    }

    fun getLostFound(): LiveData<MyResult<DelcomLostFoundsResponse>> {
        return lostfoundRepository.getLostFounds(null, 1, null).asLiveData()
    }

    fun putLostFound(
        lostfoundId: Int,
        title: String,
        description: String,
        status: String,
        isCompleted: Boolean,
    ): LiveData<MyResult<DelcomResponse>> {
        return lostfoundRepository.putLostFound(
            lostfoundId,
            title,
            description,
            status,
            isCompleted,
        ).asLiveData()
    }

    companion object {
        @Volatile
        private var INSTANCE: MainViewModel? = null

        fun getInstance(
            authRepository: AuthRepository,
            todoRepository: LostFoundRepository
        ): MainViewModel {
            synchronized(ViewModelFactory::class.java) {
                INSTANCE = MainViewModel(
                    authRepository,
                    todoRepository
                )
            }
            return INSTANCE as MainViewModel
        }
    }
}

