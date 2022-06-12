package ar.com.develup.tateti.actividades

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Gravity.apply
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat.apply
import ar.com.develup.tateti.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlinx.android.synthetic.main.actividad_inicial.*
import java.lang.Exception

enum class ProviderType {
    GOOGLE
}

class ActividadInicial : AppCompatActivity() {
    val crashlytics = Firebase.crashlytics
    private val GOOGLE_SIGN_IN = 100
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actividad_inicial)
        val bundle = Bundle()
        val provider = bundle?.getString("provider")
        val prefs =
            getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs.putString("email", email.text.toString())
        prefs.putString("provider", provider)
        prefs.apply()

        firebaseAnalytics = Firebase.analytics
        auth = Firebase.auth



        firebaseAnalytics.logEvent("InitInicial", bundle)

        iniciarSesion.setOnClickListener { iniciarSesion() }
        registrate.setOnClickListener { registrate() }
        olvideMiContrasena.setOnClickListener { olvideMiContrasena() }

        if (usuarioEstaLogueado()) {
            // Si el usuario esta logueado, se redirige a la pantalla
            // de partidas
            verPartidas()
            finish()
        }
        actualizarRemoteConfig()
    }

    override fun onResume() {
        super.onResume()

    }

    private fun usuarioEstaLogueado(): Boolean {
        // TODO-05-AUTHENTICATION
        // Validar que currentUser sea != null
        val user = auth.currentUser
        var userLogueado = false
        if (user != null) {
            userLogueado = true
        }
        return userLogueado

    }

    private fun verPartidas() {
        val intent = Intent(this, ActividadPartidas::class.java)
        startActivity(intent)
    }

    private fun registrate() {
        val intent = Intent(this, ActividadRegistracion::class.java)
        startActivity(intent)
    }

    private fun actualizarRemoteConfig() {
        configurarDefaultsRemoteConfig()
        configurarOlvideMiContrasena()

    }

    private fun configurarDefaultsRemoteConfig() {
        // TODO-04-REMOTECONFIG
        // Configurar los valores por default para remote config,
        // ya sea por codigo o por XML

        val settings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
            fetchTimeoutInSeconds = 10
        }
        Firebase.remoteConfig.setConfigSettingsAsync(settings)
        Firebase.remoteConfig.setDefaultsAsync(R.xml.firebase_config_defaults)//agarro el xml

    }

    private fun configurarOlvideMiContrasena() {
        // TODO-04-REMOTECONFIG
        // Obtener el valor de la configuracion para saber si mostrar
        // o no el boton de olvide mi contraseña
        Firebase.remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                val botonOlvideHabilitado = false
                if (botonOlvideHabilitado) {
                    olvideMiContrasena.visibility = View.VISIBLE
                } else {
                    olvideMiContrasena.visibility = View.GONE
                }
            }
    }

    private fun olvideMiContrasena() {
        // Obtengo el mail
        val email = email.text.toString()
        // Si no completo el email, muestro mensaje de error
        if (email.isEmpty()) {
            Snackbar.make(rootView!!, "Completa el email", Snackbar.LENGTH_SHORT).show()
        } else {
            // TODO-05-AUTHENTICATION
            // Si completo el mail debo enviar un mail de reset
            // Para ello, utilizamos sendPasswordResetEmail con el email como parametro
            // Agregar el siguiente fragmento de codigo como CompleteListener, que notifica al usuario
            // el resultado de la operacion
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Snackbar.make(rootView, "Email enviado", Snackbar.LENGTH_SHORT).show()
                    } else {
                        Snackbar.make(rootView, "Error " + task.exception, Snackbar.LENGTH_SHORT)
                            .show()
                    }
                }
        }
    }

    private fun iniciarSesion() {
        val email = email.text.toString()
        val password = password.text.toString()
        if (email.isEmpty() || password.isEmpty()) {
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN) {
                param("LoginIncorrecto", "Inicio de sesion incorrecto")
            }
            Toast.makeText(applicationContext, "Por favor llene los campos", Toast.LENGTH_LONG)
                .show()
        } else {
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN) {
                param("LoginCorrecto", "Inicio de sesion correcto")
            }
            // TODO-05-AUTHENTICATION
            // hacer signInWithEmailAndPassword con los valores ingresados de email y password
            // Agregar en addOnCompleteListener el campo authenticationListener definido mas abajo
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(authenticationListener)
        }
        registrarGoogle.setOnClickListener {
            val googleConf = GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()

            val googleClient = GoogleSignIn.getClient(this, googleConf)
            googleClient.signOut()

            startActivityForResult(googleClient.signInIntent, GOOGLE_SIGN_IN)
        }

    }

    private val authenticationListener: OnCompleteListener<AuthResult?> =
        OnCompleteListener<AuthResult?> { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                if (usuarioVerificoEmail(user)) {
                    verPartidas()
                } else {
                    desloguearse()
                    Snackbar.make(
                        rootView!!,
                        "Verifica tu email para continuar",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            } else {
                if (task.exception is FirebaseAuthInvalidUserException) {
                    Snackbar.make(rootView!!, "El usuario no existe", Snackbar.LENGTH_SHORT).show()
                } else if (task.exception is FirebaseAuthInvalidCredentialsException) {
                    Snackbar.make(rootView!!, "Credenciales inválidas", Snackbar.LENGTH_SHORT)
                        .show()
                }
            }
        }

    private fun usuarioVerificoEmail(user: FirebaseUser?): Boolean {
        // TODO-05-AUTHENTICATION
        // Preguntar al currentUser si verifico email
        var email = false
        if (user!!.isEmailVerified) {
            email = true
        } else {
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                user.email?.let { param(FirebaseAnalytics.Param.ITEM_NAME, it) }
            }
        }
        return email
    }

    private fun desloguearse() {
        // TODO-05-AUTHENTICATION
        // Hacer signOut de Firebase
        auth.signOut()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(credential)
                }

            }catch (e:ApiException){
                Snackbar.make(rootView!!, "No existe la cuenta", Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}