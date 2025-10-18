const express = require('express');
const jwt = require('jsonwebtoken');
const User = require('../models/User');

const router = express.Router();
const JWT_SECRET = 'tu_clave_secreta_muy_segura_aqui'; // En producción usa variable de entorno

// Generar token JWT
const generateToken = (userId) => {
    return jwt.sign({ userId }, JWT_SECRET, { expiresIn: '24h' });
};

// REGISTRO de usuario
router.post('/register', async (req, res) => {
    try {
        const { name, email, password } = req.body;

        // Validar campos
        if (!name || !email || !password) {
            return res.status(400).json({
                success: false,
                message: 'Todos los campos son obligatorios'
            });
        }

        // Verificar si el usuario ya existe
        const existingUser = await User.findOne({ email });
        if (existingUser) {
            return res.status(400).json({
                success: false,
                message: 'El usuario ya existe'
            });
        }

        // Crear nuevo usuario
        const user = new User({ name, email, password });
        await user.save();

        // Generar token
        const token = generateToken(user._id);

        res.status(201).json({
            success: true,
            message: 'Usuario registrado exitosamente',
            user: {
                id: user._id,
                name: user.name,
                email: user.email,
                token: token
            }
        });

    } catch (error) {
        console.error('Error en registro:', error);
        res.status(500).json({
            success: false,
            message: 'Error interno del servidor'
        });
    }
});

// LOGIN de usuario
router.post('/login', async (req, res) => {
    try {
        const { email, password } = req.body;

        // Validar campos
        if (!email || !password) {
            return res.status(400).json({
                success: false,
                message: 'Email y contraseña son obligatorios'
            });
        }

        // Buscar usuario
        const user = await User.findOne({ email });
        if (!user) {
            return res.status(401).json({
                success: false,
                message: 'Credenciales incorrectas'
            });
        }

        // Verificar contraseña
        const isPasswordValid = await user.comparePassword(password);
        if (!isPasswordValid) {
            return res.status(401).json({
                success: false,
                message: 'Credenciales incorrectas'
            });
        }

        // Generar token
        const token = generateToken(user._id);

        res.json({
            success: true,
            message: 'Login exitoso',
            user: {
                id: user._id,
                name: user.name,
                email: user.email,
                token: token
            }
        });

    } catch (error) {
        console.error('Error en login:', error);
        res.status(500).json({
            success: false,
            message: 'Error interno del servidor'
        });
    }
});

// VALIDAR token
router.get('/validate', async (req, res) => {
    try {
        const token = req.header('Authorization')?.replace('Bearer ', '');
        
        if (!token) {
            return res.status(401).json({
                success: false,
                message: 'Token no proporcionado'
            });
        }

        // Verificar token
        const decoded = jwt.verify(token, JWT_SECRET);
        const user = await User.findById(decoded.userId).select('-password');
        
        if (!user) {
            return res.status(401).json({
                success: false,
                message: 'Token inválido'
            });
        }

        res.json({
            success: true,
            message: 'Token válido',
            user: {
                id: user._id,
                name: user.name,
                email: user.email,
                token: token // Devolver el mismo token o generar uno nuevo
            }
        });

    } catch (error) {
        console.error('Error validando token:', error);
        res.status(401).json({
            success: false,
            message: 'Token inválido'
        });
    }
});

// LOGOUT
router.post('/logout', (req, res) => {
    // En un sistema real, podrías invalidar el token en una blacklist
    // Para este ejemplo, simplemente confirmamos el logout
    res.json({
        success: true,
        message: 'Logout exitoso'
    });
});

module.exports = router;