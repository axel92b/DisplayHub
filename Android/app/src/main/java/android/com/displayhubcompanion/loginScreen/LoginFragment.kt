package android.com.displayhubcompanion.loginScreen

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.com.displayhubcompanion.R
import android.com.displayhubcompanion.boardsScreen.BoardsActivity
import android.com.displayhubcompanion.models.User
import android.com.displayhubcompanion.repository.DatabaseRepository
import android.content.Intent
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.GoogleAuthProvider

/**
 * A simple [Fragment] subclass.
 */
class LoginFragment : Fragment() {
    private val clientIDToken = "789006884889-0j6kfm5jjje9ij9t78v0qckgumv4u757.apps.googleusercontent.com"
    private val clientSecret = "NFkKP88WjpjPHp6_5rNwtYRg"
    private lateinit var signInGoogleButton: SignInButton
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    //Auth id
    private val RC_SIGN_IN = 9001

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        signInGoogleButton = view.findViewById(R.id.login_fragment_sign_in_with_google)
        signInGoogleButton.setOnClickListener{
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(clientIDToken)
                .requestEmail()
                .build()
            // Build a GoogleSignInClient with the options specified by gso.
            mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
            signInGoogleButton.isEnabled = false
            val signInIntent = mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
            signInGoogleButton.isEnabled = true
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account!!)
        } catch (e: ApiException) {
            signInGoogleButton.isEnabled = true
            Toast.makeText(context, getString(R.string.authentication_failed), Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val auth = DatabaseRepository.getFirebaseAuth()
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user =
                        User(
                            name = auth.currentUser?.displayName,
                            photoURL = auth.currentUser?.photoUrl.toString()
                        )
                    DatabaseRepository.getLoggedInUser().addOnCompleteListener {
                        val doc = it.result
                        if (doc?.exists()!!) {
                            signInGoogleButton.isEnabled = true
                            startUserActivity()
                        } else {
                            createNewUser(user)
                            startUserActivity()
                        }
                    }.addOnFailureListener {
                        createNewUser(user)
                    }
                } else {
                    signInGoogleButton.isEnabled = true
                    Toast.makeText(
                        context,
                        getString(R.string.authentication_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun createNewUser(user: User) {
        DatabaseRepository.createNewUserEntryInDB(user).addOnSuccessListener {
            signInGoogleButton.isEnabled = true
            startUserActivity()
        }.addOnFailureListener {
            signInGoogleButton.isEnabled = true
            Toast.makeText(context, getString(R.string.authentication_failed), Toast.LENGTH_SHORT)
                .show()
        }
        signInGoogleButton.isEnabled = true
    }


    private fun startUserActivity() {
        Toast.makeText(context, "Hello " + DatabaseRepository.getFirebaseAuth().currentUser?.displayName, Toast.LENGTH_SHORT).show()
        val userHome = Intent(context, BoardsActivity::class.java)
        startActivity(userHome)
        activity?.finish()
    }
}
