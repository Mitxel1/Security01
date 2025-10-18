package mx.edu.utng.arg.security01.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.edu.utng.arg.security01.models.AuthState
import mx.edu.utng.arg.security01.models.User
import mx.edu.utng.arg.security01.repository.AuthRepository

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AuthRepository(application)

    // CAMBIO: Usar StateFlow en lugar de LiveData
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        checkExistingSession()
    }

    private fun checkExistingSession() {
        viewModelScope.launch {
            if (repository.isLoggedIn()) {
                val user = repository.getCurrentUser()
                if (user != null) {
                    _currentUser.value = user
                    _authState.value = AuthState.Success(user)
                    validateToken()
                }
            }
        }
    }

    fun login(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = repository.login(email, password)
            result.onSuccess { user ->
                _currentUser.value = user
                _authState.value = AuthState.Success(user)
            }.onFailure { exception ->
                _authState.value = AuthState.Error(
                    exception.message ?: "Error desconocido en el login"
                )
            }
        }
    }

    fun validateToken() {
        viewModelScope.launch {
            val result = repository.validateToken()
            result.onSuccess { isValid ->
                if (!isValid) {
                    logout()
                }
            }.onFailure {
                // Mantenemos sesión local aunque falle la validación
            }
        }
    }

    fun logout() {
        Log.d("AuthViewModel", "=== LOGOUT LLAMADO ===")
        _authState.value = AuthState.Loading

        viewModelScope.launch {
            val result = repository.logout()

            result.onSuccess {
                Log.d("AuthViewModel", "✅ Logout exitoso")
                _currentUser.value = null
                _authState.value = AuthState.Logout
            }.onFailure { exception ->
                Log.e("AuthViewModel", "❌ Error en logout: ${exception.message}")
                // Aunque falle, forzamos el logout local
                _currentUser.value = null
                _authState.value = AuthState.Logout
            }
        }
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }

    fun updateUserActivity() {
        repository.updateActivity()
    }

    fun isLoggedIn(): Boolean {
        return repository.isLoggedIn()
    }
}