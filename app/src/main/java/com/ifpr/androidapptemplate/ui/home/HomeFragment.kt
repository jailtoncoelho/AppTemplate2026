package com.ifpr.androidapptemplate.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        carregarItens()

        return binding.root
    }

    private fun carregarItens() {

        db.collection("itens")
            .get()
            .addOnSuccessListener { result ->

                binding.containerItens.removeAllViews()

                for (document in result) {

                    val nome = document.getString("nome") ?: ""

                    val preco = when (val valor = document.get("preco")) {
                        is Double -> valor
                        is String -> valor.toDoubleOrNull() ?: 0.0
                        else -> 0.0
                    }

                    // 🔥 cria o card dinamicamente
                    val view = layoutInflater.inflate(R.layout.item_produto, null)

                    val textNome = view.findViewById<TextView>(R.id.textNome)
                    val textPreco = view.findViewById<TextView>(R.id.textPreco)
                    val btnComprar = view.findViewById<Button>(R.id.btnComprar)

                    textNome.text = nome
                    textPreco.text = "R$ %.2f".format(preco)

                    btnComprar.setOnClickListener {
                        Toast.makeText(context, "Comprado: $nome", Toast.LENGTH_SHORT).show()
                    }

                    binding.containerItens.addView(view)
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Erro ao carregar itens", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}