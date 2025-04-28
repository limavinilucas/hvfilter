package com.example.deepseekhv

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var estruturas: List<Estrutura>
    private lateinit var spinnerAlimentadores: Spinner
    private lateinit var spinnerDirecao: Spinner
    private lateinit var editTextDistancia: EditText
    private lateinit var textViewResultado: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        spinnerAlimentadores = findViewById(R.id.spinnerAlimentadores)
        spinnerDirecao = findViewById(R.id.spinnerDirecao)
        editTextDistancia = findViewById(R.id.editTextDistancia)
        textViewResultado = findViewById(R.id.textViewResultado)
        progressBar = findViewById(R.id.progressBar)

        findViewById<Button>(R.id.buttonBuscar).setOnClickListener {
            buscarEstruturas()
        }

        loadCSVData()
    }

//    private fun loadCSVData() {
//        progressBar.visibility = View.VISIBLE
//
//        val handler = Handler(Looper.getMainLooper())
//
//        Thread(object : Runnable {
//            override fun run() {
//                try {
//                    val inputStream = assets.open("planilhaTeste.csv")
//                    val loadedEstruturas = CSVHelper.readEstruturasFromCSV(inputStream)
//                    inputStream.close()
//
//                    estruturas = loadedEstruturas
//
//                    handler.post(object : Runnable {
//                        override fun run() {
//                            updateUIWithData()
//                            progressBar.visibility = View.GONE
//                        }
//                    })
//                } catch (e: Exception) {
//                    handler.post(object : Runnable {
//                        override fun run() {
//                            Toast.makeText(
//                                this@MainActivity,
//                                "Erro ao carregar arquivo: ${e.message}",
//                                Toast.LENGTH_LONG
//                            ).show()
//                            progressBar.visibility = View.GONE
//                        }
//                    })
//                }
//            }
//        }).start()
//    }
    private fun loadCSVData() {
        progressBar.visibility = View.VISIBLE
        Log.d("DEBUG", "Iniciando carregamento do CSV")
        val files = assets.list("")
        Log.d("DEBUG", "Arquivos em assets: ${files?.joinToString()}")
        Thread {
            try {
                // DEBUG: Verifique se o arquivo existe
                val files = assets.list("")
                Log.d("ASSETS", "Arquivos disponíveis: ${files?.joinToString()}")

                assets.open("planilhaTeste.csv").use { inputStream ->
                    val loadedEstruturas = CSVHelper.readEstruturasFromCSV(inputStream)
                    Log.d("CSV_LOAD", "Carregadas ${loadedEstruturas.size} estruturas")

                    runOnUiThread {
                        estruturas = loadedEstruturas
                        updateUIWithData()
                        progressBar.visibility = View.GONE

                        // DEBUG: Verifique os alimentadores carregados
                        if (spinnerAlimentadores.adapter == null || spinnerAlimentadores.adapter.count == 0) {
                            Log.e("SPINNER", "Nenhum alimentador carregado")
                            Toast.makeText(
                                this@MainActivity,
                                "Nenhum alimentador encontrado no arquivo CSV",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("CSV_LOAD", "Erro ao carregar CSV", e)
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "Erro ao carregar arquivo: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    progressBar.visibility = View.GONE
                }
            }
        }.start()
    }

    private fun updateUIWithData() {
        // Extrai alimentadores únicos
        val alimentadores = estruturas
            .map { it.ldat }
            .distinct()
            .sorted()

        if (alimentadores.isEmpty()) {
            Toast.makeText(this, "Nenhum alimentador encontrado no arquivo", Toast.LENGTH_LONG).show()
            return
        }

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            alimentadores
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        spinnerAlimentadores.adapter = adapter

        // Atualiza direções quando um alimentador é selecionado
        spinnerAlimentadores.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                updateDirectionSpinner()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                spinnerDirecao.adapter = null
            }
        }

        // Seleciona o primeiro item por padrão
        if (alimentadores.isNotEmpty()) {
            spinnerAlimentadores.setSelection(0)
        }
    }

    private fun updateDirectionSpinner() {
        val selected = spinnerAlimentadores.selectedItem?.toString() ?: return

        // Extrai direções no formato "XX YYY/ZZZ"
        val parts = selected.split("/")
        if (parts.size != 2) {
            spinnerDirecao.adapter = null
            return
        }

        val dir1 = parts[0].takeLast(3).trim()
        val dir2 = parts[1].take(3).trim()

        val directions = listOf("$dir1 para $dir2", "$dir2 para $dir1")

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            directions
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        spinnerDirecao.adapter = adapter
        spinnerDirecao.setSelection(0)
    }

    private fun atualizarDirecoes() {
        val alimentadorSelecionado = spinnerAlimentadores.selectedItem.toString()
        val partes = alimentadorSelecionado.split("/")

        if (partes.size == 2) {
            val dir1 = partes[0].takeLast(3).trim()
            val dir2 = partes[1].take(3).trim()

            val direcoes = listOf("$dir1 para $dir2", "$dir2 para $dir1")

            spinnerDirecao.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                direcoes
            )
        }
    }

    private fun buscarEstruturas() {
        try {
            // 1. Verificação segura dos componentes de UI
            val alimentador = spinnerAlimentadores.selectedItem?.toString() ?: run {
                textViewResultado.text = "Selecione um alimentador"
                return
            }

            val direcao = spinnerDirecao.selectedItem?.toString() ?: run {
                textViewResultado.text = "Selecione uma direção"
                return
            }

            // 2. Validação da distância
            val distanciaInput = editTextDistancia.text.toString().toIntOrNull() ?: 0
            if (distanciaInput < 0) {
                textViewResultado.text = "Distância deve ser um valor positivo"
                return
            }

            // 3. Filtragem segura
            val estruturasFiltradas = estruturas.filter { it.ldat == alimentador }
            if (estruturasFiltradas.isEmpty()) {
                textViewResultado.text = "Nenhum dado encontrado para '$alimentador'"
                return
            }

            // 4. Determinação da direção de forma mais robusta
            val partesDirecao = direcao.split(" para ")
            val isDirecaoInversa = when {
                partesDirecao.size != 2 -> false
                direcao.contains("$alimentador.substringAfter('/') para $alimentador.substringBefore('/')") -> true
                else -> false
            }

            // 5. Cálculo com tratamento de null safety
            val estruturaEncontrada = try {
                if (isDirecaoInversa) {
                    val kmMaximo = estruturasFiltradas.maxOfOrNull { it.kmLocal } ?: 0.0
                    estruturasFiltradas.minByOrNull { Math.abs(it.km - (kmMaximo - distanciaInput)) }
                } else {
                    estruturasFiltradas.minByOrNull { Math.abs(it.km - distanciaInput) }
                }
            } catch (e: Exception) {
                null
            }

            // 6. Exibição do resultado
            estruturaEncontrada?.let {
                textViewResultado.text = """
                Estrutura: ${it.estrutura}
                Tipo: ${it.tipo}
                KM Local: ${it.kmLocal}
                Distância: ${it.km}
            """.trimIndent()
            } ?: run {
                textViewResultado.text = "Nenhuma estrutura encontrada para os critérios informados"
            }

        } catch (e: Exception) {
            // Log detalhado do erro
            Log.e("BUSCA_ESTRUTURAS", "Erro na busca", e)
            Toast.makeText(
                this,
                "Erro na busca: ${e.message ?: "erro desconhecido"}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}