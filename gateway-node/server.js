const express = require('express');
const axios = require('axios');
const cors = require('cors');
const path = require('path');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 3000;
const SPRING_BACKEND_URL = process.env.SPRING_BACKEND_URL || 'http://localhost:8080';

app.use(cors());
app.use(express.json());

// Log all requests to gateway
app.use((req, res, next) => {
    console.log(`[Gateway] ${req.method} ${req.url}`);
    next();
});

// Serve static frontend files
app.use(express.static(path.join(__dirname, '../frontend')));

// Proxy: Start ETL
app.post('/api/etl/run', async (req, res) => {
    try {
        const response = await axios.post(`${SPRING_BACKEND_URL}/api/etl/run`, req.body);
        res.status(response.status).json(response.data);
    } catch (error) {
        console.error('Error triggering ETL:', error.message);
        res.status(500).json({ error: 'Failed to trigger ETL' });
    }
});

// Proxy: Get Status
app.get('/api/etl/status', async (req, res) => {
    try {
        const response = await axios.get(`${SPRING_BACKEND_URL}/api/etl/status`);
        res.status(response.status).json(response.data);
    } catch (error) {
        console.error('Error fetching status:', error.message);
        res.status(500).json({ error: 'Failed to fetch status' });
    }
});

// Proxy: Get Summary
app.get('/api/etl/summary', async (req, res) => {
    try {
        const response = await axios.get(`${SPRING_BACKEND_URL}/api/etl/summary`);
        res.status(response.status).json(response.data);
    } catch (error) {
        console.error('Error fetching summary:', error.message);
        res.status(500).json({ error: 'Failed to fetch summary' });
    }
});

// Proxy: Schedule
app.post('/api/etl/schedule', async (req, res) => {
    try {
        const response = await axios.post(`${SPRING_BACKEND_URL}/api/etl/schedule`, req.body);
        res.status(response.status).json(response.data);
    } catch (error) {
        console.error('Error saving schedule:', error.message);
        res.status(500).json({ error: 'Failed to save schedule' });
    }
});

app.delete('/api/etl/schedule', async (req, res) => {
    try {
        const response = await axios.delete(`${SPRING_BACKEND_URL}/api/etl/schedule`);
        res.status(response.status).json(response.data);
    } catch (error) {
        console.error('Error cancelling schedule:', error.message);
        res.status(500).json({ error: 'Failed to cancel schedule' });
    }
});

app.listen(PORT, () => {
    console.log(`Gateway server running on port ${PORT}`);
});
