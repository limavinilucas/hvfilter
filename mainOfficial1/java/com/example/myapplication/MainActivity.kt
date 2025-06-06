package com.example.myapplication

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    private lateinit var spinnerAlimentador: Spinner
    private lateinit var spinnerDirecao: Spinner
    private lateinit var editTextDistancia: EditText
    private lateinit var textViewResultado: TextView

    // Dados da planilha
    private val alimentadores = mutableSetOf<String>()  // Set para alimentadores únicos
    private val direcoesMap = mutableMapOf<String, List<String>>() // Direções por alimentador
    private val dadosMap = mutableMapOf<String, List<Data>>() // Dados completos por alimentador

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        spinnerAlimentador = findViewById(R.id.spinnerAlimentador)
        spinnerDirecao = findViewById(R.id.spinnerDirecao)
        editTextDistancia = findViewById(R.id.editTextDistancia)
        textViewResultado = findViewById(R.id.textViewResultado)

        carregarDados()

        val adapterAlimentador = ArrayAdapter(this, android.R.layout.simple_spinner_item, alimentadores.toList())
        adapterAlimentador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerAlimentador.adapter = adapterAlimentador

        spinnerAlimentador.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                atualizarDirecoes()
            }
            override fun onNothingSelected(parentView: android.widget.AdapterView<*>?) {}
        }

        findViewById<View>(R.id.buttonFiltrar).setOnClickListener {
            filtrarDados()
        }
    }

    private fun carregarDados() {
        try {
            val inputStream = resources.openRawResource(R.raw.dados)
            val reader = BufferedReader(InputStreamReader(inputStream))
            reader.forEachLine { line ->
                val columns = line.split(";")
                if (columns.size >= 5) {
                    val alimentador = columns[0]
                    val direcao = alimentador.split("/")[0]
                    val estrutura = columns[2]
                    val tipo = columns[3]
                    val kmLocal = columns[4].replace(",", ".").toDoubleOrNull() ?: 0.0

                    alimentadores.add(alimentador)

                    if (direcoesMap.containsKey(alimentador)) {
                        direcoesMap[alimentador] = direcoesMap[alimentador]!! + direcao
                    } else {
                        direcoesMap[alimentador] = listOf(direcao)
                    }

                    val data = Data(estrutura, tipo, kmLocal)
                    if (dadosMap.containsKey(alimentador)) {
                        dadosMap[alimentador] = dadosMap[alimentador]!! + data
                    } else {
                        dadosMap[alimentador] = listOf(data)
                    }
                }
            }
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun atualizarDirecoes() {
        val alimentadorSelecionado = spinnerAlimentador.selectedItem as String
        val direcoes = direcoesMap[alimentadorSelecionado]?.distinct() ?: emptyList()
        val adapterDirecao = ArrayAdapter(this, android.R.layout.simple_spinner_item, direcoes)
        adapterDirecao.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDirecao.adapter = adapterDirecao
    }

    private fun filtrarDados() {
        val alimentadorSelecionado = spinnerAlimentador.selectedItem as String
        val direcaoSelecionada = spinnerDirecao.selectedItem as String
        val distanciaInserida = editTextDistancia.text.toString().toDoubleOrNull()

        if (distanciaInserida == null) {
            Toast.makeText(this, "Por favor, insira um valor válido para a distância.", Toast.LENGTH_SHORT).show()
            return
        }

        val dadosDoAlimentador = dadosMap[alimentadorSelecionado]
        if (dadosDoAlimentador != null) {
            val resultadoFiltrado = mutableListOf<String>()
            val parteInteiraDistancia = distanciaInserida.toInt()

            for (data in dadosDoAlimentador) {
                val parteInteiraKm = data.kmLocal.toInt()

                if (direcaoSelecionada == alimentadorSelecionado.split(" ")[0]) {
                    if (parteInteiraKm == parteInteiraDistancia) {
                        resultadoFiltrado.add("Estrutura: ${data.estrutura}, Tipo: ${data.tipo}, KM Local: ${data.kmLocal}")
                    }
                } else {
                    val distanciaMaximaRestante = data.kmLocal - distanciaInserida
                    if (distanciaInserida <= distanciaMaximaRestante && parteInteiraKm == parteInteiraDistancia) {
                        resultadoFiltrado.add("Estrutura: ${data.estrutura}, Tipo: ${data.tipo}, KM Local: ${data.kmLocal}")
                    }
                }
            }

            textViewResultado.text = if (resultadoFiltrado.isEmpty()) "Nenhum resultado encontrado." else resultadoFiltrado.joinToString("\n")
        } else {
            textViewResultado.text = "Nenhum dado encontrado."
        }
    }

    data class Data(val estrutura: String, val tipo: String, val kmLocal: Double)
}
