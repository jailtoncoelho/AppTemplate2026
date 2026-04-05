package com.ifpr.androidapptemplate.ui.usuario

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.baseclasses.Usuario
import com.ifpr.androidapptemplate.ui.login.LoginActivity

class PerfilUsuarioFragment : Fragment() {

    private lateinit var userProfileImageView: ImageView
    private lateinit var textDisplayName: TextView
    private lateinit var textDisplayEmail: TextView
    private lateinit var registerNameEditText: EditText
    private lateinit var registerEmailEditText: EditText
    private lateinit var registerEnderecoEditText: EditText
    private lateinit var registerPasswordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var btnSettings: ImageView
    private lateinit var sectionSenha: LinearLayout
    private lateinit var usersReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_perfil_usuario, container, false)

        // Inicializa o Firebase Auth
        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        userProfileImageView = view.findViewById(R.id.userProfileImageView)
        textDisplayName = view.findViewById(R.id.textDisplayName)
        textDisplayEmail = view.findViewById(R.id.textDisplayEmail)
        registerNameEditText = view.findViewById(R.id.registerNameEditText)
        registerEmailEditText = view.findViewById(R.id.registerEmailEditText)
        registerEnderecoEditText = view.findViewById(R.id.registerEnderecoEditText)
        registerButton = view.findViewById(R.id.salvarButton)
        btnSettings = view.findViewById(R.id.btnSettings)
        sectionSenha = view.findViewById(R.id.sectionSenha)

        try {
            usersReference = FirebaseDatabase.getInstance().getReference("users")
        } catch (e: Exception) {
            Log.e("DatabaseReference", "Erro ao obter referência para o Firebase DatabaseReference", e)
            Toast.makeText(context, "Erro ao acessar o Firebase DatabaseReference", Toast.LENGTH_SHORT).show()
        }

        if (user != null) {
            sectionSenha.visibility = View.GONE
            registerEmailEditText.isEnabled = false
        }

        user?.let {
            val photoUrl = it.photoUrl

            if (photoUrl != null && photoUrl.toString().isNotEmpty()) {
                Glide.with(this)
                    .load(photoUrl)
                    .placeholder(R.mipmap.ic_default_user)
                    .error(R.mipmap.ic_default_user)
                    .into(userProfileImageView)
            } else {
                userProfileImageView.setImageResource(R.mipmap.ic_default_user)
            }
        }

        registerButton.setOnClickListener {
            updateUser()
        }

        btnSettings.setOnClickListener {
            val bottomSheetDialog = BottomSheetDialog(requireContext())
            bottomSheetDialog.setContentView(R.layout.dialog_profile_menu)

            // Remove o fundo branco padrão para a borda arredondada aparecer
            val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.setBackgroundResource(android.R.color.transparent)

            val btnContaMenu = bottomSheetDialog.findViewById<TextView>(R.id.btnContaMenu)
            val btnSairMenu = bottomSheetDialog.findViewById<TextView>(R.id.btnSairMenu)
            val btnCancelarMenu = bottomSheetDialog.findViewById<TextView>(R.id.btnCancelarMenu)

            btnContaMenu?.setOnClickListener {
                bottomSheetDialog.dismiss()
                startActivity(Intent(requireContext(), com.ifpr.androidapptemplate.ui.configuracoes.AccountSettingsActivity::class.java))
            }

            btnSairMenu?.setOnClickListener {
                bottomSheetDialog.dismiss()
                signOut()
            }

            btnCancelarMenu?.setOnClickListener {
                bottomSheetDialog.dismiss()
            }

            bottomSheetDialog.show()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Exibe os dados do usuario logado, se disponivel
        val userFirebase = auth.currentUser
        if (userFirebase != null) {
            registerNameEditText.setText(userFirebase.displayName)
            registerEmailEditText.setText(userFirebase.email)
            textDisplayName.text = userFirebase.displayName ?: "Usuário"
            textDisplayEmail.text = userFirebase.email ?: ""

            recuperarDadosUsuario(userFirebase.uid)
        }
    }

    fun recuperarDadosUsuario(usuarioKey: String) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("users")

        databaseReference.child(usuarioKey).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val usuario = snapshot.getValue(Usuario::class.java)
                    usuario?.let {
                        registerEnderecoEditText.setText(it.endereco ?: "")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Erro ao recuperar dados: ${error.message}")
            }
        })
    }

    private fun updateUser() {
        val name = registerNameEditText.text.toString().trim()
        val endereco = registerEnderecoEditText.text.toString().trim()

        // Acessar currentUser
        val user = auth.currentUser

        // Verifica se o usuário atual já está definido
        if (user != null) {
            // Se o usuário já existe, atualiza os dados
            updateProfile(user, name, endereco)
        } else {
            Toast.makeText(context, "Não foi possível encontrar o usuário logado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateProfile(user: FirebaseUser?, displayName: String, endereco: String) {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .build()

        val usuario = Usuario(user?.uid.toString(), displayName, user?.email, endereco)

        user?.updateProfile(profileUpdates)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    saveUserToDatabase(usuario)
                    Toast.makeText(context, "Nome do usuario alterado com sucesso.",
                        Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Não foi possivel alterar o nome do usuario.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUserToDatabase(usuario: Usuario) {
        if (usuario.key != null) {
            usersReference.child(usuario.key.toString()).setValue(usuario)
                .addOnSuccessListener {
                    Toast.makeText(context, "Usuario atualizado com sucesso!", Toast.LENGTH_SHORT)
                        .show()
                    requireActivity().supportFragmentManager.popBackStack()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Falha ao atualizar o usuario", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "ID invalido", Toast.LENGTH_SHORT).show()
        }
    }

    private fun signOut() {
        auth.signOut()
        Toast.makeText(context, "Logout realizado com sucesso!", Toast.LENGTH_SHORT).show()
        navigateToLogin()
    }

    private fun navigateToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
}