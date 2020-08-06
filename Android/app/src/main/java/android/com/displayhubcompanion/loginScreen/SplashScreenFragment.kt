package android.com.displayhubcompanion.loginScreen

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.com.displayhubcompanion.R
import android.com.displayhubcompanion.boardsScreen.BoardsActivity
import android.com.displayhubcompanion.repository.DatabaseRepository
import android.content.Intent
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import kotlinx.coroutines.delay

/**
 * A simple [Fragment] subclass.
 */
class SplashScreenFragment : Fragment() {
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_splash_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        DatabaseRepository.getLoggedInUser().addOnSuccessListener {
            Toast.makeText(context, "Hello " + DatabaseRepository.getFirebaseAuth().currentUser?.displayName, Toast.LENGTH_SHORT).show()
            val userHome = Intent(context, BoardsActivity::class.java)
            startActivity(userHome)
            activity?.finish()
        }.addOnFailureListener{
            navController.navigate(
                R.id.action_splashScreenFragment_to_loginFragment
            )
        }
    }
}
