package com.ifpr.androidapptemplate.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.gms.location.*
import com.google.firebase.firestore.FirebaseFirestore
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.databinding.FragmentHomeBinding
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private var currentLat = 0.0
    private var currentLng = 0.0

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        inicializaLocalizacao()
        carregarItens()

        binding.btnMaps.setOnClickListener {
            abrirGoogleMaps()
        }

        return binding.root
    }

    // 📍 LOCALIZAÇÃO

    private fun inicializaLocalizacao() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            iniciarAtualizacaoLocalizacao()
        }
    }

    private fun iniciarAtualizacaoLocalizacao() {

        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    currentLat = location.latitude
                    currentLng = location.longitude
                    mostrarEndereco(location)
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun mostrarEndereco(location: Location) {
        try {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val lista = geocoder.getFromLocation(location.latitude, location.longitude, 1)

            val endereco = lista?.get(0)?.getAddressLine(0) ?: "Endereço não encontrado"
            binding.currentAddressTextView.text = endereco

        } catch (e: Exception) {
            binding.currentAddressTextView.text = "Erro ao obter endereço"
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                iniciarAtualizacaoLocalizacao()
            } else {
                Toast.makeText(context, "Permissão negada", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 🛒 FIREBASE

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

                    val view = layoutInflater.inflate(R.layout.item_produto, null)

                    val textNome = view.findViewById<TextView>(R.id.textNome)
                    val textPreco = view.findViewById<TextView>(R.id.textPreco)
                    val btnComprar = view.findViewById<Button>(R.id.btnComprar)
                    val imageView = view.findViewById<ImageView>(R.id.imageProduto)

                    textNome.text = nome
                    textPreco.text = "R$ %.2f".format(preco)

                    val imageUrl = document.getString("imagem")

                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(requireContext())
                            .load(imageUrl)
                            .into(imageView)
                    }

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

    // 🗺️ GOOGLE MAPS

    private fun abrirGoogleMaps() {

        if (currentLat == 0.0 && currentLng == 0.0) {
            Toast.makeText(context, "Aguardando localização...", Toast.LENGTH_SHORT).show()
            return
        }

        val uri = Uri.parse("geo:$currentLat,$currentLng?q=$currentLat,$currentLng")

        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Google Maps não instalado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}