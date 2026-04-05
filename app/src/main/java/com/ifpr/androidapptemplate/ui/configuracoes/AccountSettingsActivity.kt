package com.ifpr.androidapptemplate.ui.configuracoes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.ui.login.LoginActivity

class AccountSettingsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var sairButton: LinearLayout
    private lateinit var apagarContaButton: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)

        auth = FirebaseAuth.getInstance()
        
        sairButton = findViewById(R.id.sairButton)
        apagarContaButton = findViewById(R.id.apagarContaButton)
        
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // O titulo "Conta" e a seta de voltar ja sao gerados pelo tema DarkActionBar

        val user = auth.currentUser
        if (user == null) {
            sairButton.visibility = View.GONE
            apagarContaButton.visibility = View.GONE
        }

        sairButton.setOnClickListener {
            signOut()
        }

        apagarContaButton.setOnClickListener {
            showDeleteAccountConfirmation()
        }
    }

    private fun signOut() {
        auth.signOut()
        Toast.makeText(this, "Logout realizado com sucesso!", Toast.LENGTH_SHORT).show()
        navigateToLogin()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showDeleteAccountConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Apagar conta")
            .setMessage("Tem certeza que deseja apagar sua conta? Todos os seus dados serão perdidos permanentemente. Esta ação não pode ser desfeita.")
            .setPositiveButton("Confirmar") { _, _ ->
                showDeleteAccountFinalConfirmation()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showDeleteAccountFinalConfirmation() {
        val editText = EditText(this)
        editText.hint = "APAGAR"

        AlertDialog.Builder(this)
            .setTitle("Confirmação final")
            .setMessage("Digite APAGAR para confirmar a exclusão da sua conta:")
            .setView(editText)
            .setPositiveButton("Confirmar") { _, _ ->
                val typedText = editText.text.toString().trim()
                if (typedText == "APAGAR") {
                    deleteAccount()
                } else {
                    Toast.makeText(this, "Texto incorreto. A conta não foi apagada.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteAccount() {
        val user = auth.currentUser ?: return
        val uid = user.uid

        // 1. Delete user data from Realtime Database
        FirebaseDatabase.getInstance().getReference("users").child(uid).removeValue()
            .addOnCompleteListener { dbTask ->
                if (dbTask.isSuccessful) {
                    Log.d("DeleteAccount", "User data deleted from database")
                } else {
                    Log.e("DeleteAccount", "Failed to delete user data from database", dbTask.exception)
                }

                // 2. Delete Firebase Auth account
                user.delete()
                    .addOnCompleteListener { authTask ->
                        if (authTask.isSuccessful) {
                            Toast.makeText(this, "Conta apagada com sucesso.", Toast.LENGTH_SHORT).show()
                            navigateToLogin()
                        } else {
                            Log.e("DeleteAccount", "Failed to delete auth account", authTask.exception)
                            Toast.makeText(this, "Erro ao apagar a conta. Tente fazer login novamente e repetir.", Toast.LENGTH_LONG).show()
                        }
                    }
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
