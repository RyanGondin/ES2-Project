// This file contains utility functions that assist with various tasks in the application, such as logging operations and managing exceptions related to authorization failures.

const logger = (message) => {
    console.log(`[LOG] ${new Date().toISOString()}: ${message}`);
};

const handleError = (error) => {
    console.error(`[ERROR] ${new Date().toISOString()}: ${error.message}`);
    return {
        status: 'error',
        message: error.message,
    };
};

module.exports = {
    logger,
    handleError,
};