const appIds = {}; // In-memory storage for app IDs

// Handle GET request for a specific app ID
exports.getAppId = (req, res) => {
    const { appid } = req.params;
    if (appIds[appid]) {
        return res.status(200).json({ appid, data: appIds[appid] });
    }
    return res.status(404).json({ message: 'App ID not found' });
};

// Handle POST request to create or update an app ID
exports.createOrUpdateAppId = (req, res) => {
    const { appid } = req.params;
    const { data } = req.body;

    appIds[appid] = data;
    return res.status(200).json({ message: 'App ID saved successfully', appid });
};

// Handle PUT request to update a specific app ID
exports.updateAppId = (req, res) => {
    const { appid } = req.params;
    const { data } = req.body;

    if (appIds[appid]) {
        appIds[appid] = data;
        return res.status(200).json({ message: 'App ID updated successfully', appid });
    }
    return res.status(404).json({ message: 'App ID not found' });
};