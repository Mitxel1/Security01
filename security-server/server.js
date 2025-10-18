const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(cors({
    origin: '*', // Permitir todas las conexiones
    methods: ['GET', 'POST', 'PUT', 'DELETE'],
    allowedHeaders: ['Content-Type', 'Authorization']
}));
app.use(express.json());

// Conectar a MongoDB
mongoose.connect('mongodb://localhost:27017/security_app', {
    useNewUrlParser: true,
    useUnifiedTopology: true
})
.then(() => console.log('✅ Conectado a MongoDB'))
.catch(err => console.error('❌ Error conectando a MongoDB:', err));

// Modelos
const User = require('./models/User');

// Rutas
app.use('/api/auth', require('./routes/auth'));

// Ruta de prueba
app.get('/api/test', (req, res) => {
    res.json({ message: '¡Servidor funcionando! 🚀' });
});

// Iniciar servidor
app.listen(PORT, () => {
    console.log(`🚀 Servidor ejecutándose en http://localhost:${PORT}`);
});