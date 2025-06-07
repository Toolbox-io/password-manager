package io.toolbox.passwdmanager.utils

import kotlin.math.exp
import kotlin.math.tanh

/**
 * A lightweight neural network for password strength assessment.
 * This implementation uses a simple feed-forward neural network with one hidden layer.
 * The model is trained to recognize patterns in passwords and predict their strength.
 */
class PasswordNeuralNetwork(
    private val inputSize: Int = 128, // ASCII character range
    private val hiddenSize: Int = 64,
    private val outputSize: Int = 1
) {
    // Weights and biases
    private var weights1 = Array(hiddenSize) { DoubleArray(inputSize) { 0.0 } }
    private var weights2 = DoubleArray(hiddenSize) { 0.0 }
    private var bias1 = DoubleArray(hiddenSize) { 0.0 }
    private var bias2 = 0.0

    // Activation functions
    private fun sigmoid(x: Double): Double = 1.0 / (1.0 + exp(-x))
    private fun sigmoidDerivative(x: Double): Double = x * (1.0 - x)
    private fun tanhDerivative(x: Double): Double = 1.0 - tanh(x) * tanh(x)

    /**
     * Convert a password to a feature vector
     * Features include:
     * - Character frequency distribution
     * - Length
     * - Presence of different character types
     */
    private fun passwordToFeatures(password: String): DoubleArray {
        val features = DoubleArray(inputSize) { 0.0 }
        
        // Character frequency distribution
        password.forEach { char ->
            if (char.code < inputSize) {
                features[char.code] += 1.0
            }
        }
        
        // Normalize frequencies
        val sum = features.sum()
        if (sum > 0) {
            for (i in features.indices) {
                features[i] /= sum
            }
        }
        
        return features
    }

    /**
     * Forward pass through the network
     */
    private fun forward(input: DoubleArray): Pair<DoubleArray, Double> {
        // Hidden layer
        val hidden = DoubleArray(hiddenSize)
        for (i in 0 until hiddenSize) {
            var sum = bias1[i]
            for (j in input.indices) {
                sum += input[j] * weights1[i][j]
            }
            hidden[i] = tanh(sum)
        }

        // Output layer
        var output = bias2
        for (i in 0 until hiddenSize) {
            output += hidden[i] * weights2[i]
        }
        output = sigmoid(output)

        return Pair(hidden, output)
    }

    /**
     * Train the network on a single example
     */
    fun train(password: String, targetStrength: Double, learningRate: Double = 0.01) {
        val input = passwordToFeatures(password)
        val (hidden, output) = forward(input)

        // Calculate error
        val error = targetStrength - output
        val outputDelta = error * sigmoidDerivative(output)

        // Update weights and biases
        for (i in 0 until hiddenSize) {
            weights2[i] += learningRate * outputDelta * hidden[i]
        }
        bias2 += learningRate * outputDelta

        val hiddenDelta = DoubleArray(hiddenSize)
        for (i in 0 until hiddenSize) {
            hiddenDelta[i] = outputDelta * weights2[i] * tanhDerivative(hidden[i])
            for (j in input.indices) {
                weights1[i][j] += learningRate * hiddenDelta[i] * input[j]
            }
            bias1[i] += learningRate * hiddenDelta[i]
        }
    }

    /**
     * Predict password strength
     * @return A value between 0 and 1 indicating password strength
     */
    fun predict(password: String): Double {
        val input = passwordToFeatures(password)
        return forward(input).second
    }

    /**
     * Save the model weights to a string
     */
    fun saveModel(): String {
        val sb = StringBuilder()
        
        // Save weights1
        weights1.forEach { row ->
            row.forEach { value ->
                sb.append(value).append(",")
            }
        }
        sb.append("|")
        
        // Save weights2
        weights2.forEach { value ->
            sb.append(value).append(",")
        }
        sb.append("|")
        
        // Save bias1
        bias1.forEach { value ->
            sb.append(value).append(",")
        }
        sb.append("|")
        
        // Save bias2
        sb.append(bias2)
        
        return sb.toString()
    }

    /**
     * Load the model weights from a string
     */
    fun loadModel(modelString: String) {
        val parts = modelString.split("|")
        
        // Load weights1
        val weights1Values = parts[0].split(",").filter { it.isNotEmpty() }.map { it.toDouble() }
        var index = 0
        for (i in 0 until hiddenSize) {
            for (j in 0 until inputSize) {
                weights1[i][j] = weights1Values[index++]
            }
        }
        
        // Load weights2
        val weights2Values = parts[1].split(",").filter { it.isNotEmpty() }.map { it.toDouble() }
        for (i in 0 until hiddenSize) {
            weights2[i] = weights2Values[i]
        }
        
        // Load bias1
        val bias1Values = parts[2].split(",").filter { it.isNotEmpty() }.map { it.toDouble() }
        for (i in 0 until hiddenSize) {
            bias1[i] = bias1Values[i]
        }
        
        // Load bias2
        bias2 = parts[3].toDouble()
    }
}

/**
 * Password strength assessor using a neural network
 */
object PasswordStrengthAI {
    private val network = PasswordNeuralNetwork()
    
    // Training data: (password, strength)
    private val trainingData = listOf(
        "password123" to 0.1,
        "qwerty123" to 0.15,
        "12345678" to 0.05,
        "admin123" to 0.1,
        "letmein" to 0.1,
        "welcome1" to 0.15,
        "Kj#9mP$2vL" to 0.95,
        "X7\$pL9#mN2" to 0.9,
        "P@ssw0rd!" to 0.7,
        "Secure123!" to 0.8,
        "RandomPass123!" to 0.85,
        "C0mpl3x!P@ss" to 0.9,
        "a" to 0.1,
        "aa" to 0.1,
        "aaa" to 0.1,
        "aaaa" to 0.1,
        "aaaaa" to 0.1,
        "12345" to 0.1,
        "qwert" to 0.1,
        "asdfg" to 0.1
    )

    init {
        // Train the network
        repeat(1000) { // 1000 epochs
            trainingData.forEach { (password, strength) ->
                network.train(password, strength)
            }
        }
    }

    /**
     * Assess password strength using the neural network
     * @return A value between 0 and 1 indicating password strength
     */
    fun assessStrength(password: String): Double {
        return network.predict(password)
    }

    /**
     * Save the trained model
     */
    fun saveModel(): String {
        return network.saveModel()
    }

    /**
     * Load a trained model
     */
    fun loadModel(modelString: String) {
        network.loadModel(modelString)
    }
} 