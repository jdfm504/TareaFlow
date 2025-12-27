package com.campusdigitalfp.tareaflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusdigitalfp.tareaflow.data.model.UserProfile
import com.campusdigitalfp.tareaflow.data.UserProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserProfileViewModel : ViewModel() {

    private val repo = UserProfileRepository()

    private val _profile = MutableStateFlow(UserProfile())
    val profile: StateFlow<UserProfile> get() = _profile

    init {
        viewModelScope.launch {
            repo.listenUserProfile().collect { data ->
                _profile.value = data
            }
        }
    }

    fun loadProfile() {
        viewModelScope.launch {
            _profile.value = repo.loadUserProfile()
        }
    }

    fun updateName(newName: String) {
        viewModelScope.launch {
            repo.updateName(newName)
            _profile.value = _profile.value.copy(name = newName)
        }
    }

    fun reload() {
        viewModelScope.launch {
            val profile = repo.loadUserProfile()
            _profile.value = profile
        }
    }

    fun clear() {
        _profile.value = UserProfile()
    }
}
