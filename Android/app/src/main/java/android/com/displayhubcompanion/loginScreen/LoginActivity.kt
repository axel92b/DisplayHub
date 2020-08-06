package android.com.displayhubcompanion.loginScreen

import android.com.displayhubcompanion.R
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import android.os.Bundle
import android.view.View


class LoginActivity : AppCompatActivity() {
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val view = findViewById<View>(R.id.login_fragment_host)
        navController = Navigation.findNavController(view)
    }

    override fun onBackPressed() {
        if (navController.currentDestination?.label == "fragment_login") {
            super.onBackPressed()
        }
        navController.popBackStack()
        navController.navigate(R.id.loginFragment)
    }
}
