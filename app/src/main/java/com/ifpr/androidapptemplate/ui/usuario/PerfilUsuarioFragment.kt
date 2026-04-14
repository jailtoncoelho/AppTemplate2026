package com.ifpr.androidapptemplate.ui.usuario

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ifpr.androidapptemplate.databinding.FragmentPerfilUsuarioBinding

class PerfilUsuarioFragment : Fragment() {

    private var _binding: FragmentPerfilUsuarioBinding? = null
    private val binding get() = _binding!!

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilUsuarioBinding.inflate(inflater, container, false)

        carregarUsuario()

        return binding.root
    }

    private fun carregarUsuario() {
        val user = auth.currentUser

        if (user != null) {
            val uid = user.uid

            db.collection("usuarios")
                .document(uid)
                .get()
                .addOnSuccessListener { doc ->

                    val nome = doc.getString("nome") ?: ""
                    val email = doc.getString("email") ?: ""
                    val endereco = doc.getString("endereco") ?: ""

                    binding.registerNameEditText.setText(nome)
                    binding.registerEmailEditText.setText(email)
                    binding.registerEnderecoEditText.setText(endereco)
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}