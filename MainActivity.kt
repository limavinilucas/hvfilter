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

    // Vamos carregar os dados da planilha de exemplo (em CSV)
    private val alimentadores = mutableSetOf<String>()  // Usando um Set para garantir alimentadores únicos
    private val direcoesMap = mutableMapOf<String, List<String>>() // Direções por Alimentador
    private val dadosMap = mutableMapOf<String, List<Data>>() // Map para armazenar os dados completos (Estrutura, Tipo, KM Local)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        spinnerAlimentador = findViewById(R.id.spinnerAlimentador)
        spinnerDirecao = findViewById(R.id.spinnerDirecao)
        editTextDistancia = findViewById(R.id.editTextDistancia)
        textViewResultado = findViewById(R.id.textViewResultado)

        // Carregar os dados do CSV
        carregarDados()

        // Configurar o Spinner para Alimentador
        val adapterAlimentador = ArrayAdapter(this, android.R.layout.simple_spinner_item, alimentadores.toList())
        adapterAlimentador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerAlimentador.adapter = adapterAlimentador

        // Configurar o Listener para quando um alimentador for selecionado
        spinnerAlimentador.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                atualizarDirecoes()
            }

            override fun onNothingSelected(parentView: android.widget.AdapterView<*>?) {
                // Não faz nada se nada for selecionado
            }
        }

        // Configurar o Listener para o botão de filtrar
        findViewById<View>(R.id.buttonFiltrar).setOnClickListener {
            filtrarDados()
        }
    }

    // Função para carregar dados do CSV
    private fun carregarDados() {
        try {
            val inputStream = resources.openRawResource(R.raw.dados) // Carregar o arquivo "dados.csv" na pasta res/raw
            val reader = BufferedReader(InputStreamReader(inputStream))
            reader.forEachLine { line ->
                val columns = line.split(";")
                val alimentador = columns[0]
                val direcao = alimentador.split(" ")[0] // Extraindo direcao do alimentador
                val estrutura = columns[2]
                val tipo = columns[3]
                val kmLocal = columns[4].replace(",", ".").toDoubleOrNull() ?: 0.0

                // Adicionar alimentadores únicos
                alimentadores.add(alimentador)

                // Armazenar as direções no mapa (direcoesMap)
                if (direcoesMap.containsKey(alimentador)) {
                    direcoesMap[alimentador] = direcoesMap[alimentador]!! + direcao
                } else {
                    direcoesMap[alimentador] = listOf(direcao)
                }

                // Adicionar dados completos no mapa
                val data = Data(estrutura, tipo, kmLocal)
                if (dadosMap.containsKey(alimentador)) {
                    dadosMap[alimentador] = dadosMap[alimentador]!! + data
                } else {
                    dadosMap[alimentador] = listOf(data)
                }
            }

            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Função para atualizar as direções no spinner
    private fun atualizarDirecoes() {
        val alimentadorSelecionado = spinnerAlimentador.selectedItem as String
        val direcoes = direcoesMap[alimentadorSelecionado] ?: emptyList()
        val adapterDirecao = ArrayAdapter(this, android.R.layout.simple_spinner_item, direcoes)
        adapterDirecao.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDirecao.adapter = adapterDirecao
    }

    // Função para filtrar os dados com base na seleção
    private fun filtrarDados() {
        val alimentadorSelecionado = spinnerAlimentador.selectedItem as String
        val direcaoSelecionada = spinnerDirecao.selectedItem as String
        val distanciaInserida = editTextDistancia.text.toString().toDoubleOrNull()

        if (distanciaInserida == null) {
            Toast.makeText(this, "Por favor, insira um valor válido para a distância.", Toast.LENGTH_SHORT).show()
            return
        }

        // Obter os dados completos do alimentador selecionado
        val dadosDoAlimentador = dadosMap[alimentadorSelecionado]
        if (dadosDoAlimentador != null) {
            // Filtrar os dados com base na direção e na distância
            val resultadoFiltrado = mutableListOf<String>()

            // Iterar sobre os dados para aplicar o filtro
            for (data in dadosDoAlimentador) {
                val kmMaximo = data.kmLocal

                // Verificar se a direção selecionada corresponde ao alimentador e ajustar a distância
                if (direcaoSelecionada == alimentadorSelecionado.split(" ")[0]) {
                    if (distanciaInserida <= kmMaximo) {
                        resultadoFiltrado.add("Estrutura: ${data.estrutura}, Tipo: ${data.tipo}, KM Local: ${data.kmLocal}")
                    }
                } else {
                    val distanciaMaximaRestante = kmMaximo - distanciaInserida
                    if (distanciaInserida <= distanciaMaximaRestante) {
                        resultadoFiltrado.add("Estrutura: ${data.estrutura}, Tipo: ${data.tipo}, KM Local: ${data.kmLocal}")
                    }
                }
            }

            // Exibir o resultado filtrado
            if (resultadoFiltrado.isEmpty()) {
                textViewResultado.text = "Nenhum resultado encontrado."
            } else {
                textViewResultado.text = resultadoFiltrado.joinToString("\n")
            }
        } else {
            textViewResultado.text = "Nenhum dado encontrado."
        }
    }

    // Classe de dados para armazenar Estrutura, Tipo e KM Local
    data class Data(val estrutura: String, val tipo: String, val kmLocal: Double)
}
