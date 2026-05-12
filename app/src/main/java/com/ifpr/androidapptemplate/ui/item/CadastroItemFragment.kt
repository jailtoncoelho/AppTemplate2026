package com.ifpr.androidapptemplate.ui.item

import com.bumptech.glide.Glide
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.ifpr.androidapptemplate.databinding.FragmentCadastroItemBinding

class CadastroItemFragment : Fragment() {

    private var _binding: FragmentCadastroItemBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCadastroItemBinding.inflate(inflater, container, false)

        binding.btnSalvarItem.setOnClickListener {
            salvarItem()
        }

        return binding.root
    }

    private fun salvarItem() {

        val nome = binding.editNomeItem.text.toString().trim()
        val descricao = binding.editDescricaoItem.text.toString().trim()
        val precoTexto = binding.editPrecoItem.text.toString().trim()

        if (nome.isEmpty() || descricao.isEmpty() || precoTexto.isEmpty()) {
            Toast.makeText(context, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            return
        }

        // 🔥 CONVERSÃO CORRETA
        val preco = precoTexto.toDoubleOrNull()

        if (preco == null) {
            Toast.makeText(context, "Preço inválido", Toast.LENGTH_SHORT).show()
            return
        }

        val item = hashMapOf(
            "nome" to nome,
            "descricao" to descricao,
            "preco" to preco // ✅ AGORA É DOUBLE
        )

        db.collection("itens")
            .add(item)
            .addOnSuccessListener {
                Toast.makeText(context, "Item salvo com sucesso!", Toast.LENGTH_SHORT).show()

                binding.editNomeItem.text.clear()
                binding.editDescricaoItem.text.clear()
                binding.editPrecoItem.text.clear()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Erro ao salvar item", Toast.LENGTH_SHORT).show()
            }
    }


}