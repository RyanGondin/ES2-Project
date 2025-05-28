// Stub para simular API externa
export const fetchExternalApps = async () => {
  // Simular delay de rede
  await new Promise(resolve => setTimeout(resolve, 1000));
  
  // Simular dados de uma API externa
  return [
    {
      external_id: 'ext_app_1',
      name: 'External App 1',
      description: 'App importada da API externa',
      owner: 'external_user_1',
      created_at: '2025-01-01T00:00:00Z'
    },
    {
      external_id: 'ext_app_2', 
      name: 'External App 2',
      description: 'Outra app importada',
      owner: 'external_user_2',
      created_at: '2025-01-02T00:00:00Z'
    }
  ];
};

export const mockExternalApiFailure = () => {
  throw new Error('External API unavailable');
};