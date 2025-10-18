package mx.edu.utng.arg.security01.repository

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import mx.edu.utng.arg.security01.models.LoginRequest
import mx.edu.utng.arg.security01.models.User
import mx.edu.utng.arg.security01.network.RetrofitClient
import mx.edu.utng.arg.security01.security.SecureStorage

class AuthRepository(context: Context) {
    private val secureStorage = SecureStorage(context)
    private val apiService = RetrofitClient.apiService

    companion object {
        private const val TAG = "AuthRepository"
    }

    suspend fun login(email: String, password: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "=== MODO SIMULACIÓN ACTIVADO ===")
                Log.d(TAG, "Email recibido: $email")
                Log.d(TAG, "Password longitud: ${password.length}")

                // Simular delay de red
                delay(1500)

                // Validaciones básicas
                if (email.isBlank() || password.isBlank()) {
                    return@withContext Result.failure(
                        Exception("El email y la contraseña son obligatorios")
                    )
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    return@withContext Result.failure(
                        Exception("Formato de email inválido")
                    )
                }

                if (password.length < 6) {
                    return@withContext Result.failure(
                        Exception("La contraseña debe tener al menos 6 caracteres")
                    )
                }

                // SIMULACIÓN DE LOGIN EXITOSO
                val mockUser = User(
                    id = "user_${System.currentTimeMillis()}",
                    name = email.substringBefore("@").replace(".", " ").capitalize(),
                    email = email,
                    token = "mock_jwt_token_${System.currentTimeMillis()}"
                )

                secureStorage.saveUserSession(mockUser)
                Log.d(TAG, "✅ Login simulado exitoso: ${mockUser.name}")
                Result.success(mockUser)

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error en login simulado: ${e.message}")
                Result.failure(Exception("Error de conexión: ${e.localizedMessage}"))
            }
        }
    }

    fun isLoggedIn(): Boolean {
        return secureStorage.isLoggedIn()
    }

    fun getCurrentUser(): User? {
        return secureStorage.getUserData()
    }

    suspend fun validateToken(): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val token = secureStorage.getToken()

                if (token == null) {
                    return@withContext Result.success(false)
                }

                val response = apiService.validateToken("Bearer $token")

                if (response.isSuccessful && response.body()?.success == true) {
                    secureStorage.updateSessionTimestamp()
                    Result.success(true)
                } else {
                    logout()
                    Result.success(false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error validando token", e)
                Result.failure(e)
            }
        }
    }

    suspend fun logout(): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "=== INICIANDO LOGOUT ===")

                // Limpiamos datos locales SIEMPRE
                secureStorage.clearSession()
                Log.d(TAG, "✅ Sesión local limpiada")

                Result.success(true)

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error en logout", e)
                // Aún así limpiamos local
                secureStorage.clearSession()
                Result.failure(e)
            }
        }
    }

    fun updateActivity() {
        secureStorage.updateSessionTimestamp()
    }
}