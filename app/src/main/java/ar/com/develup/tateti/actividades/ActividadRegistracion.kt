package ar.com.develup.tateti.actividades

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ar.com.develup.tateti.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.actividad_registracion.*

class ActividadRegistracion : AppCompatActivity() {
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    val bundle = Bundle()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actividad_registracion)

        firebaseAnalytics = Firebase.analytics
        registrar.setOnClickListener { registrarse()
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

        firebaseAnalytics.logEvent("creacion_user") {
            param("Se puedo crear el usuario", email)
            param("con la contreseña", passwordIngresada)
        }
    }

//    private val registracionCompletaListener: OnCompleteListener<AuthResult?> = OnCompleteListener { task ->
//        if (task.isSuccessful) {
//            // Si se registro OK, muestro mensaje y envio mail de verificacion
//            Snackbar.make(rootView, "Registro exitoso", Snackbar.LENGTH_SHORT).show()
//            enviarEmailDeVerificacion()
//        } else if (task.exception is FirebaseAuthUserCollisionException) {
//            // Si el usuario ya existe, mostramos error
//            Snackbar.make(rootView, "El usuario ya existe", Snackbar.LENGTH_SHORT).show()
//        } else {
//            // Por cualquier otro error, mostramos un mensaje de error
//            Snackbar.make(rootView, "El registro fallo: " + task.exception, Snackbar.LENGTH_LONG).show()
//        }
//    }

    private fun enviarEmailDeVerificacion() {
        var contador: Double = 0.0;
        // TODO-05-AUTHENTICATION
        // Enviar mail de verificacion al usuario currentUser

        firebaseAnalytics.logEvent("email_enviados") {
            param("Se puedo crear el usuario", contador++ )

        }
    }
}