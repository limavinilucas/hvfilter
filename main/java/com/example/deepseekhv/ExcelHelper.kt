package com.example.deepseekhv

import com.opencsv.CSVReader
import java.io.InputStream
import java.io.InputStreamReader

object CSVHelper {

    fun readEstruturasFromCSV(inputStream: InputStream): List<Estrutura> {
        val reader = CSVReader(InputStreamReader(inputStream, Charsets.UTF_8))
        val estruturas = mutableListOf<Estrutura>()
        var lineNumber = 0
        var headers: Array<String>? = null

        try {
            var nextLine: Array<String>? = reader.readNext()

            while (nextLine != null) {
                lineNumber++

                // Pula linha vazia
                if (!isLineEmpty(nextLine)) {
                    // Primeira linha não vazia é o cabeçalho
                    if (lineNumber == 1) {
                        headers = nextLine
                    } else {
                        // Parseia os valores para o objeto Estrutura
                        try {
                            val estrutura = parseEstrutura(nextLine, headers)
                            estruturas.add(estrutura)
                        } catch (e: Exception) {
                            println("Erro ao parsear linha $lineNumber: ${e.message}")
                        }
                    }
                }

                nextLine = reader.readNext()
            }
        } catch (e: Exception) {
            throw RuntimeException("Erro ao ler arquivo CSV: ${e.message}", e)
        } finally {
            reader.close()
        }

        return estruturas
    }

    private fun isLineEmpty(line: Array<String>): Boolean {
        if (line.isEmpty()) return true

        for (value in line) {
            if (value.isNotBlank()) {
                return false
            }
        }

        return true
    }

    private fun parseEstrutura(values: Array<String>, headers: Array<String>?): Estrutura {
        if (values.size < 5) {
            throw IllegalArgumentException("Linha CSV deve conter pelo menos 5 colunas")
        }

        val ldat = getValueOrEmpty(values, 0)
        val km = parseDouble(getValueOrEmpty(values, 1))
        val estrutura = getValueOrEmpty(values, 2)
        val tipo = getValueOrEmpty(values, 3)
        val kmLocal = parseDouble(getValueOrEmpty(values, 4))

        return Estrutura(
            ldat = ldat,
            km = km,
            estrutura = estrutura,
            tipo = tipo,
            kmLocal = kmLocal
        )
    }

    private fun getValueOrEmpty(values: Array<String>, index: Int): String {
        return if (index < values.size) values[index].trim() else ""
    }

    private fun parseDouble(value: String): Double {
        return try {
            value.toDouble()
        } catch (e: NumberFormatException) {
            0.0
        }
    }

    fun readEstruturasFromCSVWithHeaders(inputStream: InputStream): List<Estrutura> {
        val reader = CSVReader(InputStreamReader(inputStream, Charsets.UTF_8))
        val estruturas = mutableListOf<Estrutura>()

        try {
            val headers = reader.readNext() ?: return emptyList()
            var nextLine: Array<String>? = reader.readNext()

            while (nextLine != null) {
                try {
                    val map = mutableMapOf<String, String>()
                    for (i in headers.indices) {
                        if (i < nextLine.size) {
                            map[headers[i]] = nextLine[i].trim()
                        }
                    }

                    val estrutura = Estrutura(
                        ldat = map.getOrDefault("LDAT", ""),
                        km = parseDouble(map.getOrDefault("KM", "0.0")),
                        estrutura = map.getOrDefault("Estrutura", ""),
                        tipo = map.getOrDefault("Tipo", ""),
                        kmLocal = parseDouble(map.getOrDefault("KM Local", "0.0"))
                    )

                    estruturas.add(estrutura)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                nextLine = reader.readNext()
            }
        } finally {
            reader.close()
        }

        return estruturas
    }
}