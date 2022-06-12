package ar.com.develup.tateti.actividades

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ar.com.develup.tateti.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.actividad_registracion.*

class ActividadRegistracion : AppCompatActivity() {
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actividad_registracion)

        firebaseAnalytics = Firebase.analytics
        auth = Firebase.auth
        registrar.setOnClickListener {
            registrarse()
        }
    }

    fun registrarse() {
        val passwordIngresada = password.text.toString()
        val confirmarPasswordIngresada = confirmarPassword.text.toString()
        val email = email.text.toString()

        if (email.isEmpty()) {
            // Si no completo el email, muestro mensaje de error
            Snackbar.make(rootView, "Email requerido", Snackbar.LENGTH_SHORT).show()
        } else if (passwordIngresada == confirmarPasswordIngresada) {
            // Si completo el email y las contraseñas coinciden, registramos el usuario en Firebase
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP) {
                param("UsuarioCreado", "usuarioCreado")
            }
            registrarUsuarioEnFirebase(email, passwordIngresada)
        } else {
            // No coinciden las contraseñas, mostramos mensaje de error
            Snackbar.make(rootView, "Las contraseñas no coinciden", Snackbar.LENGTH_SHORT).show()
            sinContraseñaIguales(email)
        }
    }

    private fun sinContraseñaIguales(email: String) {
        firebaseAnalytics.logEvent("contraseña_sin_coincidiencia") {
            param("No coincide la contraseña", email)
        }
    }

    private fun registrarUsuarioEnFirebase(email: String, passwordIngresada: String) {
        // TODO-05-AUTHENTICATION
        // Crear el usuario con el email y passwordIngresada
        // Ademas, registrar en CompleteListener el listener registracionCompletaListener definido mas abajo
        auth.createUserWithEmailAndPassword(email, passwordIngresada)
            .addOnCompleteListener(registracionCompletaListener)
        firebaseAnalytics.logEvent("creacion_user") {
            param("Se puedo crear el usuario", email)
            param("con la contreseña", passwordIngresada)
        }
    }

    private val registracionCompletaListener: OnCompleteListener<AuthResult?> =
        OnCompleteListener { task ->
            if (task.isSuccessful) {
                // Si se registro OK, muestro mensaje y envio mail de verificacion
                Snackbar.make(rootView, "Registro exitoso", Snackbar.LENGTH_SHORT).show()
                enviarEmailDeVerificacion()
            } else if (task.exception is FirebaseAuthUserCollisionException) {
                // Si el usuario ya existe, mostramos error
                Snackbar.make(rootView, "El usuario ya existe", Snackbar.LENGTH_SHORT).show()
            } else {
                // Por cualquier otro error, mostramos un mensaje de error
                Snackbar.make(
                    rootView,
                    "El registro fallo: " + task.exception,
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }

    private fun enviarEmailDeVerificacion() {
        // TODO-05-AUTHENTICATION
        // Enviar mail de verificacion al usuario currentUser
        auth = Firebase.auth
        var user = auth.currentUser
        if (user != null) {
            user.sendEmailVerification()
        }
    }
}